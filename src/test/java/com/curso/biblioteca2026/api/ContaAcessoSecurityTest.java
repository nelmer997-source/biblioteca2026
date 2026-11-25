package com.curso.biblioteca2026.api;

import com.curso.biblioteca2026.config.AdministradorInicialProperties;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.service.AdministradorInicialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContaAcessoSecurityTest {

    private static final String SENHA_ADMIN = "senha-admin-segura-2026";
    private static final String SENHA_INICIAL = "uma-frase-senha-2026";
    private static final String NOVA_SENHA = "outra-frase-senha-2026";
    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(500_000_000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorInicialService administradorInicialService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void devePermitirHealthPublicoERejeitarRotasProtegidasSemBearerValido() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Sem Token",
                                  "email": "semtoken@example.com",
                                  "cpf": "%s"
                                }
                                """.formatted(gerarCpfUnico())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/contas-acesso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": 1,
                                  "login": "semtoken",
                                  "senha": "%s"
                                }
                                """.formatted(SENHA_INICIAL)))
                .andExpect(status().isUnauthorized());

        String basicHeader = "Basic " + Base64.getEncoder()
                .encodeToString(("operadora:" + SENHA_INICIAL).getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(get("/api/livros")
                        .header(HttpHeaders.AUTHORIZATION, basicHeader))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/livros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token.malformado.invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAplicarAutorizacaoPorPerfisInvariantesAdministrativasETrocaDeSenha() throws Exception {
        String adminLogin = "admin." + System.nanoTime();
        ContaAcesso adminConta = criarAdministradorInicial(adminLogin);
        String tokenAdmin = autenticar(adminLogin, SENHA_ADMIN);

        Jwt jwtAdmin = jwtDecoder.decode(tokenAdmin);
        assertEquals("https://biblioteca2026.local", jwtAdmin.getIssuer().toString());
        assertEquals(adminLogin, jwtAdmin.getSubject());
        assertTrue(jwtAdmin.getClaimAsStringList("perfis").contains("ADMINISTRADOR"));

        // Administrador cadastra pessoa e conta OPERADOR
        Long pessoaOperadoraId = cadastrarPessoa(tokenAdmin, "Operadora Biblioteca");
        String loginOperadora = "operadora." + System.nanoTime();

        MvcResult contaResult = mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": %d,
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(pessoaOperadoraId, loginOperadora, SENHA_INICIAL)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value(loginOperadora))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
                .andExpect(content().string(not(containsString(SENHA_INICIAL))))
                .andExpect(content().string(not(containsString("{bcrypt}"))))
                .andReturn();

        Long contaOperadoraId = ((Number) JsonParserFactory.getJsonParser()
                .parseMap(contaResult.getResponse().getContentAsString())
                .get("id")).longValue();

        // Login com senha incorreta retorna 401
        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "senha-incorreta-2026"
                                }
                                """.formatted(loginOperadora)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Login ou senha inválidos"));

        String tokenOperadora = autenticar(loginOperadora, SENHA_INICIAL);

        // OPERADOR executa operacoes cotidianas (200 / 201)
        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(loginOperadora));

        mockMvc.perform(get("/api/livros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora))
                .andExpect(status().isOk());

        MvcResult categoriaResult = mockMvc.perform(post("/api/categorias")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Categoria Teste %d"
                                }
                                """.formatted(System.nanoTime())))
                .andExpect(status().isCreated())
                .andReturn();

        Long categoriaId = ((Number) JsonParserFactory.getJsonParser()
                .parseMap(categoriaResult.getResponse().getContentAsString())
                .get("id")).longValue();

        // OPERADOR recebe 403 em rotas administrativas e exclusoes
        Long outraPessoaId = cadastrarPessoa(tokenOperadora, "Terceira Pessoa");
        mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": %d,
                                  "login": "nao.autorizado.%d",
                                  "senha": "%s"
                                }
                                """.formatted(outraPessoaId, System.nanoTime(), SENHA_INICIAL)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", contaOperadoraId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "perfis": ["OPERADOR", "ADMINISTRADOR"]
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/contas-acesso/{id}/status", contaOperadoraId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "BLOQUEADA"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/categorias/{id}", categoriaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora))
                .andExpect(status().isForbidden());

        // ADMINISTRADOR nao pode bloquear nem remover perfil admin de si mesmo (400)
        mockMvc.perform(put("/api/contas-acesso/{id}/status", adminConta.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "BLOQUEADA"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", adminConta.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "perfis": ["OPERADOR"]
                                }
                                """))
                .andExpect(status().isBadRequest());

        // ADMINISTRADOR promove operadora; token antigo continua sem privilegio ate novo login
        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", contaOperadoraId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "perfis": ["OPERADOR", "ADMINISTRADOR"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfis", hasItems("OPERADOR", "ADMINISTRADOR")));

        mockMvc.perform(delete("/api/categorias/{id}", categoriaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadora))
                .andExpect(status().isForbidden());

        String tokenOperadoraPromovida = autenticar(loginOperadora, SENHA_INICIAL);
        mockMvc.perform(delete("/api/categorias/{id}", categoriaId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadoraPromovida))
                .andExpect(status().isNoContent());

        // Troca de senha da propria conta autenticada
        mockMvc.perform(put("/api/contas-acesso/me/senha")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenOperadoraPromovida)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "%s",
                                  "novaSenha": "%s"
                                }
                                """.formatted(SENHA_INICIAL, NOVA_SENHA)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(loginOperadora, SENHA_INICIAL)))
                .andExpect(status().isUnauthorized());

        String tokenAposTrocaSenha = autenticar(loginOperadora, NOVA_SENHA);
        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAposTrocaSenha))
                .andExpect(status().isOk());

        // ADMINISTRADOR bloqueia outra conta (200) e novos logins da conta bloqueada falham (401)
        mockMvc.perform(put("/api/contas-acesso/{id}/status", contaOperadoraId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "BLOQUEADA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOQUEADA"));

        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(loginOperadora, NOVA_SENHA)))
                .andExpect(status().isUnauthorized());
    }

    private ContaAcesso criarAdministradorInicial(String login) {
        AdministradorInicialProperties props = new AdministradorInicialProperties(
                true,
                "Administrador Teste",
                "admin." + System.nanoTime() + "@example.com",
                gerarCpfUnico(),
                login,
                SENHA_ADMIN
        );
        return administradorInicialService.criarSeNecessario(props).orElseThrow();
    }

    private Long cadastrarPessoa(String bearerToken, String nome) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/pessoas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "email": "pessoa.%d@example.com",
                                  "cpf": "%s"
                                }
                                """.formatted(nome, System.nanoTime(), gerarCpfUnico())))
                .andExpect(status().isCreated())
                .andReturn();

        return ((Number) JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString())
                .get("id")).longValue();
    }

    private String autenticar(String login, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(login, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Map<String, Object> body = JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString());
        return body.get("accessToken").toString();
    }

    private static String gerarCpfUnico() {
        long semente = Math.abs(System.nanoTime() + SEQUENCIA_CPF.incrementAndGet()) % 899_999_999L + 100_000_000L;
        String base = String.format("%09d", semente);
        final char primeiroDigito = base.charAt(0);
        if (base.chars().allMatch(c -> c == primeiroDigito)) {
            base = "123456789";
        }
        int d1 = calcularDigitoCpf(base, 10);
        int d2 = calcularDigitoCpf(base + d1, 11);
        return base + d1 + d2;
    }

    private static int calcularDigitoCpf(String base, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}