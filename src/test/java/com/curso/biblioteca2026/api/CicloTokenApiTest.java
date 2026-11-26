package com.curso.biblioteca2026.api;

import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.MotivoRevogacaoRefreshToken;
import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.SessaoRefreshToken;
import com.curso.biblioteca2026.repository.SessaoRefreshTokenRepository;
import com.curso.biblioteca2026.service.ContaAcessoService;
import com.curso.biblioteca2026.service.PessoaService;
import com.curso.biblioteca2026.service.SessaoAutenticacaoService;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CicloTokenApiTest {

    private static final String SENHA_ADMIN = "senha-admin-ciclo-2026";
    private static final String SENHA_OPERADOR = "senha-operador-ciclo-2026";
    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(800_000_000);

    private final ConcurrentLinkedQueue<Long> pessoasCriadas = new ConcurrentLinkedQueue<>();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private ContaAcessoService contaAcessoService;

    @Autowired
    private SessaoRefreshTokenRepository sessaoRepository;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void limparDadosCriados() {
        SecurityContextHolder.clearContext();
        Long pessoaId;
        while ((pessoaId = pessoasCriadas.poll()) != null) {
            jdbcTemplate.update(
                    "delete from sessao_refresh_token where conta_acesso_id in (select id from conta_acesso where pessoa_id = ?)",
                    pessoaId);
            jdbcTemplate.update(
                    "delete from conta_acesso_perfil where conta_acesso_id in (select id from conta_acesso where pessoa_id = ?)",
                    pessoaId);
            jdbcTemplate.update("delete from conta_acesso where pessoa_id = ?", pessoaId);
            jdbcTemplate.update("delete from pessoa where id = ?", pessoaId);
        }
        jdbcTemplate.update("delete from sessao_refresh_token where conta_acesso_id in (select id from conta_acesso where login like 'admin.ciclo.%')");
        jdbcTemplate.update("delete from conta_acesso_perfil where conta_acesso_id in (select id from conta_acesso where login like 'admin.ciclo.%')");
        jdbcTemplate.update("delete from conta_acesso where login like 'admin.ciclo.%'");
        jdbcTemplate.update("delete from pessoa where nome = 'Admin Ciclo'");
    }

    @Test
    void deveEmitirParDeTokensNoLoginComClaimSidENaoPersistirSegredoPuro() throws Exception {
        String login = criarOperadorUnico("emissao");
        TokensHttp tokens = autenticar(login, SENHA_OPERADOR);

        assertNotNull(tokens.accessToken());
        assertNotNull(tokens.refreshToken());
        assertNotNull(tokens.refreshTokenExpiresAt());

        Jwt jwt = jwtDecoder.decode(tokens.accessToken());
        String sid = jwt.getClaimAsString("sid");
        assertNotNull(sid);
        assertFalse(sid.isBlank());

        assertTrue(sessaoRepository.findByTokenHash(tokens.refreshToken()).isEmpty());

        String hashEsperado = SessaoAutenticacaoService.gerarHashSha256(tokens.refreshToken());
        SessaoRefreshToken sessaoSalva = sessaoRepository.findByTokenHash(hashEsperado).orElseThrow();
        assertEquals(sid, sessaoSalva.getFamiliaId());
        assertNull(sessaoSalva.getUtilizadaEm());
        assertNull(sessaoSalva.getRevogadaEm());
    }

    @Test
    void deveRotacionarRefreshTokenERevogarTodaAFamiliaEmCasoDeReutilizacao() throws Exception {
        String login = criarOperadorUnico("rotacao");
        TokensHttp primeiroPar = autenticar(login, SENHA_OPERADOR);
        TokensHttp segundoPar = renovar(primeiroPar.refreshToken());

        assertNotEquals(primeiroPar.refreshToken(), segundoPar.refreshToken());

        renovarEsperandoNaoAutorizado(primeiroPar.refreshToken());
        renovarEsperandoNaoAutorizado(segundoPar.refreshToken());

        Jwt jwt = jwtDecoder.decode(primeiroPar.accessToken());
        String familiaId = jwt.getClaimAsString("sid");
        List<SessaoRefreshToken> familia = sessaoRepository.findByFamiliaIdOrderByIdAsc(familiaId);
        assertEquals(2, familia.size());
        assertTrue(familia.stream().allMatch(SessaoRefreshToken::foiRevogada));
        assertTrue(familia.stream()
                .allMatch(s -> s.getMotivoRevogacao() == MotivoRevogacaoRefreshToken.REUTILIZACAO));
    }

    @Test
    void deveEncerrarSessaoNoLogoutLocalEManterAccessTokenValidoAteExpirar() throws Exception {
        String login = criarOperadorUnico("logout.local");
        TokensHttp tokens = autenticar(login, SENHA_OPERADOR);

        logout(tokens.refreshToken());
        renovarEsperandoNaoAutorizado(tokens.refreshToken());

        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login));
    }

    @Test
    void deveEncerrarTodasAsSessoesNoLogoutGlobal() throws Exception {
        String login = criarOperadorUnico("logout.global");
        TokensHttp sessao1 = autenticar(login, SENHA_OPERADOR);
        TokensHttp sessao2 = autenticar(login, SENHA_OPERADOR);

        mockMvc.perform(post("/api/autenticacao/logout-todos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + sessao1.accessToken()))
                .andExpect(status().isNoContent());

        renovarEsperandoNaoAutorizado(sessao1.refreshToken());
        renovarEsperandoNaoAutorizado(sessao2.refreshToken());
    }

    @Test
    void deveBloquearRenovacaoQuandoContaForBloqueadaAposLoginComMotivoContaInativa() throws Exception {
        String tokenAdmin = criarAdminUnicoEToken();
        String login = criarOperadorUnico("bloqueio");
        ContaAcesso conta = contaAcessoService.buscarPorLogin(login);

        TokensHttp tokens = autenticar(login, SENHA_OPERADOR);

        mockMvc.perform(put("/api/contas-acesso/{id}/status", conta.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "BLOQUEADA"
                                }
                                """))
                .andExpect(status().isOk());

        renovarEsperandoNaoAutorizado(tokens.refreshToken());

        String hash = SessaoAutenticacaoService.gerarHashSha256(tokens.refreshToken());
        SessaoRefreshToken sessao = sessaoRepository.findByTokenHash(hash).orElseThrow();
        assertTrue(sessao.foiRevogada());
        assertEquals(MotivoRevogacaoRefreshToken.CONTA_INATIVA, sessao.getMotivoRevogacao());

        mockMvc.perform(get("/api/contas-acesso/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void deveImpedirDoisUsosConcorrentesDoMesmoRefreshToken() throws Exception {
        String login = criarOperadorUnico("concorrencia");
        TokensHttp tokens = autenticar(login, SENHA_OPERADOR);

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> tentativa1 = executor.submit(() -> tentarRenovarConcorrente(tokens.refreshToken(), inicioSimultaneo));
            Future<Integer> tentativa2 = executor.submit(() -> tentarRenovarConcorrente(tokens.refreshToken(), inicioSimultaneo));

            inicioSimultaneo.countDown();

            List<Integer> statusCodes = List.of(
                    tentativa1.get(10, TimeUnit.SECONDS),
                    tentativa2.get(10, TimeUnit.SECONDS));

            assertEquals(1, statusCodes.stream().filter(code -> code == 200).count());
            assertEquals(1, statusCodes.stream().filter(code -> code == 401).count());
        }
    }

    private int tentarRenovarConcorrente(String refreshToken, CountDownLatch inicioSimultaneo) throws Exception {
        inicioSimultaneo.await();
        MvcResult result = mockMvc.perform(post("/api/autenticacao/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andReturn();
        return result.getResponse().getStatus();
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
            contaAcessoService.cadastrar(pessoa.getId(), login, SENHA_OPERADOR);
            return login;
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String criarAdminUnicoEToken() throws Exception {
        String loginAdmin = "admin.ciclo." + System.nanoTime();
        autenticarContextoAdmin();
        try {
            Pessoa pessoa = pessoaService.cadastrar(
                    "Admin Ciclo",
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
        return autenticar(loginAdmin, SENHA_ADMIN).accessToken();
    }

    private static void autenticarContextoAdmin() {
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "sistema.bootstrap",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
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
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        Map<String, Object> body = JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString());
        return new TokensHttp(
                body.get("accessToken").toString(),
                body.get("refreshToken").toString(),
                body.get("refreshTokenExpiresAt").toString()
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
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        Map<String, Object> body = JsonParserFactory.getJsonParser()
                .parseMap(result.getResponse().getContentAsString());
        return new TokensHttp(
                body.get("accessToken").toString(),
                body.get("refreshToken").toString(),
                body.get("refreshTokenExpiresAt").toString()
        );
    }

    private void renovarEsperandoNaoAutorizado(String refreshToken) throws Exception {
        mockMvc.perform(post("/api/autenticacao/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Refresh token inválido ou expirado"));
    }

    private void logout(String refreshToken) throws Exception {
        mockMvc.perform(post("/api/autenticacao/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "%s"
                                }
                                """.formatted(refreshToken)))
                .andExpect(status().isNoContent());
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

    private record TokensHttp(String accessToken, String refreshToken, String refreshTokenExpiresAt) {
    }
}