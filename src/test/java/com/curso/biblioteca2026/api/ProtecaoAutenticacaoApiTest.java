package com.curso.biblioteca2026.api;

import com.curso.biblioteca2026.model.*;
import com.curso.biblioteca2026.repository.EventoSegurancaRepository;
import com.curso.biblioteca2026.repository.ProtecaoAutenticacaoRepository;
import com.curso.biblioteca2026.service.ContaAcessoService;
import com.curso.biblioteca2026.service.PessoaService;
import com.curso.biblioteca2026.service.ProtecaoAutenticacaoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProtecaoAutenticacaoApiTest {

    private static final String SENHA_ADMIN = "senha-admin-auditoria-2026";
    private static final String SENHA_CORRETA = "senha-operador-auditoria-2026";
    private static final String NOVA_SENHA = "nova-senha-auditoria-2026";
    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(900_000_000);

    private final ConcurrentLinkedQueue<Long> pessoasCriadas = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<String> chavesCriadas = new ConcurrentLinkedQueue<>();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private ContaAcessoService contaAcessoService;

    @Autowired
    private ProtecaoAutenticacaoRepository protecaoRepository;

    @Autowired
    private EventoSegurancaRepository eventoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDados() {
        SecurityContextHolder.clearContext();
        String chave;
        while ((chave = chavesCriadas.poll()) != null) {
            jdbcTemplate.update("delete from protecao_autenticacao where chave_login = ?", chave);
            jdbcTemplate.update("delete from evento_seguranca where identificador_hash = ?", chave);
        }
        Long pessoaId;
        while ((pessoaId = pessoasCriadas.poll()) != null) {
            jdbcTemplate.update(
                    "delete from evento_seguranca where ator_conta_id in (select id from conta_acesso where pessoa_id = ?) or alvo_conta_id in (select id from conta_acesso where pessoa_id = ?)",
                    pessoaId, pessoaId);
            jdbcTemplate.update(
                    "delete from sessao_refresh_token where conta_acesso_id in (select id from conta_acesso where pessoa_id = ?)",
                    pessoaId);
            jdbcTemplate.update(
                    "delete from conta_acesso_perfil where conta_acesso_id in (select id from conta_acesso where pessoa_id = ?)",
                    pessoaId);
            jdbcTemplate.update("delete from conta_acesso where pessoa_id = ?", pessoaId);
            jdbcTemplate.update("delete from pessoa where id = ?", pessoaId);
        }
        jdbcTemplate.update("delete from evento_seguranca where ator_conta_id is null and alvo_conta_id is null and identificador_hash is null");
    }

    @Test
    void deveBloquearAposCincoFalhasConsecutivasERecusarSenhaCorretaCom401Generico() throws Exception {
        String login = criarOperadorUnico("bloqueio.temp");
        String chave = registrarChave(login);

        for (int tentativa = 1; tentativa <= 5; tentativa++) {
            loginEsperandoNaoAutorizado(login, "senha-incorreta-2026");
        }

        // 6a tentativa com a senha CORRETA deve ser recusada com o mesmo 401 genérico
        loginEsperandoNaoAutorizado(login, SENHA_CORRETA);

        ProtecaoAutenticacao protecao = protecaoRepository.findByChaveLogin(chave).orElseThrow();
        assertEquals(5, protecao.getFalhasConsecutivas());
        assertNotNull(protecao.getBloqueadaAte());

        List<EventoSeguranca> eventos = eventoRepository.findAll().stream()
                .filter(e -> chave.equals(e.getIdentificadorHash()))
                .toList();

        assertEquals(5, eventos.stream().filter(e -> e.getTipo() == TipoEventoSeguranca.LOGIN_FALHA).count());
        assertEquals(1, eventos.stream().filter(e -> e.getTipo() == TipoEventoSeguranca.BLOQUEIO_TEMPORARIO_ATIVADO).count());
        assertEquals(1, eventos.stream().filter(e -> e.getTipo() == TipoEventoSeguranca.LOGIN_RECUSADO_BLOQUEIO).count());
    }

    @Test
    void deveLimparContadorAposLoginValidoAntesDoLimite() throws Exception {
        String login = criarOperadorUnico("limpa.falhas");
        String chave = registrarChave(login);

        loginEsperandoNaoAutorizado(login, "senha-errada-1");
        loginEsperandoNaoAutorizado(login, "senha-errada-2");

        ProtecaoAutenticacao antes = protecaoRepository.findByChaveLogin(chave).orElseThrow();
        assertEquals(2, antes.getFalhasConsecutivas());

        autenticar(login, SENHA_CORRETA);

        ProtecaoAutenticacao depois = protecaoRepository.findByChaveLogin(chave).orElseThrow();
        assertEquals(0, depois.getFalhasConsecutivas());
        assertNull(depois.getJanelaIniciadaEm());
        assertNull(depois.getBloqueadaAte());

        long sucessos = eventoRepository.findAll().stream()
                .filter(e -> chave.equals(e.getIdentificadorHash()) && e.getTipo() == TipoEventoSeguranca.LOGIN_SUCESSO)
                .count();
        assertEquals(1, sucessos);
    }

    @Test
    void deveManterRespostaGenericaParaContaInexistenteSemGravarLoginOriginal() throws Exception {
        String loginInexistente = "usuario.inexistente." + System.nanoTime();
        String chave = registrarChave(loginInexistente);

        loginEsperandoNaoAutorizado(loginInexistente, "qualquer-senha-2026");

        Integer ocorrenciasLoginTexto = jdbcTemplate.queryForObject(
                "select count(*) from evento_seguranca where identificador_hash = ? or endereco_origem = ?",
                Integer.class,
                loginInexistente,
                loginInexistente
        );
        assertEquals(0, ocorrenciasLoginTexto);
        assertTrue(protecaoRepository.findByChaveLogin(chave).isPresent());
    }

    @Test
    void deveGarantirConsistenciaEmFalhasConcorrentesComLockPessimista() throws Exception {
        String login = criarOperadorUnico("concorrencia.falhas");
        String chave = registrarChave(login);

        for (int i = 1; i <= 3; i++) {
            loginEsperandoNaoAutorizado(login, "senha-errada-" + i);
        }

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> f1 = executor.submit(() -> tentarLoginConcorrente(login, inicioSimultaneo));
            Future<Integer> f2 = executor.submit(() -> tentarLoginConcorrente(login, inicioSimultaneo));

            inicioSimultaneo.countDown();

            assertEquals(401, f1.get(10, TimeUnit.SECONDS));
            assertEquals(401, f2.get(10, TimeUnit.SECONDS));
        }

        ProtecaoAutenticacao protecao = protecaoRepository.findByChaveLogin(chave).orElseThrow();
        assertEquals(5, protecao.getFalhasConsecutivas());
        assertNotNull(protecao.getBloqueadaAte());

        List<EventoSeguranca> eventos = eventoRepository.findAll().stream()
                .filter(e -> chave.equals(e.getIdentificadorHash()))
                .toList();
        assertEquals(5, eventos.stream().filter(e -> e.getTipo() == TipoEventoSeguranca.LOGIN_FALHA).count());
        assertEquals(1, eventos.stream().filter(e -> e.getTipo() == TipoEventoSeguranca.BLOQUEIO_TEMPORARIO_ATIVADO).count());
    }

    @Test
    void deveAuditarAcoesAdministrativasESessaoERestringirConsultaAoAdministrador() throws Exception {
        TokensHttp tokensAdmin = criarAdminUnicoETokens();
        String tokenAdmin = tokensAdmin.accessToken();

        // Cria operador via endpoint administrativo -> audita CONTA_CRIADA
        Long pessoaOperadorId = cadastrarPessoa(tokenAdmin, "Operador Auditado");
        String loginOperador = "op.audit." + System.nanoTime();
        registrarChave(loginOperador);

        MvcResult contaResult = mockMvc.perform(post("/api/contas-acesso")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pessoaId": %d,
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(pessoaOperadorId, loginOperador, SENHA_CORRETA)))
                .andExpect(status().isCreated())
                .andReturn();

        Long contaOperadorId = ((Number) JsonParserFactory.getJsonParser()
                .parseMap(contaResult.getResponse().getContentAsString())
                .get("id")).longValue();

        // Altera perfis -> audita PERFIS_ALTERADOS
        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", contaOperadorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "perfis": ["OPERADOR", "ADMINISTRADOR"]
                                }
                                """))
                .andExpect(status().isOk());

        // Volta para OPERADOR
        mockMvc.perform(put("/api/contas-acesso/{id}/perfis", contaOperadorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "perfis": ["OPERADOR"]
                                }
                                """))
                .andExpect(status().isOk());

        // Login, troca senha (SENHA_ALTERADA), refresh (REFRESH_SUCESSO + REFRESH_FALHA), logout e logout-todos
        TokensHttp tokensOperador = autenticar(loginOperador, SENHA_CORRETA);

        mockMvc.perform(put("/api/contas-acesso/me/senha")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokensOperador.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "senhaAtual": "%s",
                                  "novaSenha": "%s"
                                }
                                """.formatted(SENHA_CORRETA, NOVA_SENHA)))
                .andExpect(status().isNoContent());

        TokensHttp renovados = renovar(tokensOperador.refreshToken());
        // Reutilização provoca REFRESH_FALHA
        mockMvc.perform(post("/api/autenticacao/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(tokensOperador.refreshToken())))
                .andExpect(status().isUnauthorized());

        TokensHttp segundaSessao = autenticar(loginOperador, NOVA_SENHA);
        mockMvc.perform(post("/api/autenticacao/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(segundaSessao.refreshToken())))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/autenticacao/logout-todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + renovados.accessToken()))
                .andExpect(status().isNoContent());

        // Altera status -> audita STATUS_ALTERADO
        mockMvc.perform(put("/api/contas-acesso/{id}/status", contaOperadorId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "BLOQUEADA"
                                }
                                """))
                .andExpect(status().isOk());

        // Autorização do endpoint de auditoria: 401 sem token, 403 para OPERADOR, 200 para ADMINISTRADOR
        mockMvc.perform(get("/api/auditoria-seguranca"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auditoria-seguranca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + renovados.accessToken()))
                .andExpect(status().isForbidden());

        MvcResult auditoriaResult = mockMvc.perform(get("/api/auditoria-seguranca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo").isArray())
                .andExpect(jsonPath("$.conteudo[0].identificadorHash").doesNotExist())
                .andExpect(content().string(not(containsString("identificadorHash"))))
                .andExpect(content().string(not(containsString(SENHA_CORRETA))))
                .andExpect(content().string(not(containsString(NOVA_SENHA))))
                .andExpect(content().string(not(containsString(renovados.refreshToken()))))
                .andReturn();

        String jsonResposta = auditoriaResult.getResponse().getContentAsString();
        assertTrue(jsonResposta.contains("CONTA_CRIADA"));
        assertTrue(jsonResposta.contains("PERFIS_ALTERADOS"));
        assertTrue(jsonResposta.contains("STATUS_ALTERADO"));
        assertTrue(jsonResposta.contains("SENHA_ALTERADA"));
        assertTrue(jsonResposta.contains("REFRESH_SUCESSO"));
        assertTrue(jsonResposta.contains("REFRESH_FALHA"));
        assertTrue(jsonResposta.contains("LOGOUT"));
        assertTrue(jsonResposta.contains("LOGOUT_GLOBAL"));

        // Filtro por tipo
        mockMvc.perform(get("/api/auditoria-seguranca")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .param("tipo", "SENHA_ALTERADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[*].tipo", everyItem(equalTo("SENHA_ALTERADA"))));
    }

    private int tentarLoginConcorrente(String login, CountDownLatch inicioSimultaneo) throws Exception {
        inicioSimultaneo.await();
        MvcResult result = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "senha-errada-concorrente"
                                }
                                """.formatted(login)))
                .andReturn();
        return result.getResponse().getStatus();
    }

    private void loginEsperandoNaoAutorizado(String login, String senha) throws Exception {
        mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(login, senha)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Login ou senha inválidos"));
    }

    private String criarOperadorUnico(String prefixo) {
        autenticarContextoAdmin();
        try {
            Pessoa pessoa = pessoaService.cadastrar(
                    "Operador " + prefixo,
                    prefixo + "." + System.nanoTime() + "@example.com",
                    gerarCpfUnico());
            pessoasCriadas.add(pessoa.getId());
            String login = prefixo + "." + System.nanoTime();
            contaAcessoService.cadastrar(pessoa.getId(), login, SENHA_CORRETA);
            return login;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private TokensHttp criarAdminUnicoETokens() throws Exception {
        String loginAdmin = "admin.audit." + System.nanoTime();
        registrarChave(loginAdmin);
        autenticarContextoAdmin();
        try {
            Pessoa pessoa = pessoaService.cadastrar(
                    "Admin Auditoria",
                    loginAdmin + "@example.com",
                    gerarCpfUnico());
            pessoasCriadas.add(pessoa.getId());
            ContaAcesso conta = contaAcessoService.cadastrar(pessoa.getId(), loginAdmin, SENHA_ADMIN);
            contaAcessoService.alterarPerfis(
                    conta.getId(),
                    Set.of(PerfilAcesso.OPERADOR, PerfilAcesso.ADMINISTRADOR),
                    "sistema.bootstrap");
        } finally {
            SecurityContextHolder.clearContext();
        }
        return autenticar(loginAdmin, SENHA_ADMIN);
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

        Long pessoaId = ((Number) JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString())
                .get("id")).longValue();
        pessoasCriadas.add(pessoaId);
        return pessoaId;
    }

    private TokensHttp autenticar(String login, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "login": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(login, senha)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> body = JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString());
        return new TokensHttp(
                body.get("accessToken").toString(),
                body.get("refreshToken").toString()
        );
    }

    private TokensHttp renovar(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/autenticacao/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Object> body = JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString());
        return new TokensHttp(
                body.get("accessToken").toString(),
                body.get("refreshToken").toString()
        );
    }

    private String registrarChave(String login) {
        String chave = ProtecaoAutenticacaoService.gerarChaveLogin(login);
        chavesCriadas.add(chave);
        return chave;
    }

    private static void autenticarContextoAdmin() {
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "sistema.bootstrap",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
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

    private record TokensHttp(String accessToken, String refreshToken) {
    }
}