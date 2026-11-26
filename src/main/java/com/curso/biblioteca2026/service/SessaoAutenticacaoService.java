package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.config.RefreshTokenProperties;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.exception.RefreshTokenInvalidoException;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.MotivoRevogacaoRefreshToken;
import com.curso.biblioteca2026.model.SessaoRefreshToken;
import com.curso.biblioteca2026.model.TipoEventoSeguranca;
import com.curso.biblioteca2026.repository.ContaAcessoRepository;
import com.curso.biblioteca2026.repository.SessaoRefreshTokenRepository;
import com.curso.biblioteca2026.security.TokenAcesso;
import com.curso.biblioteca2026.security.TokenService;
import com.curso.biblioteca2026.security.TokensAutenticacao;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessaoAutenticacaoService {

    private static final int TAMANHO_REFRESH_TOKEN_BYTES = 32;

    private final SessaoRefreshTokenRepository sessaoRepository;
    private final ContaAcessoRepository contaRepository;
    private final TokenService tokenService;
    private final RefreshTokenProperties properties;
    private final Clock clock;
    private final AuditoriaSegurancaService auditoriaService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessaoAutenticacaoService(
            SessaoRefreshTokenRepository sessaoRepository,
            ContaAcessoRepository contaRepository,
            TokenService tokenService,
            RefreshTokenProperties properties,
            Clock clock,
            AuditoriaSegurancaService auditoriaService) {
        this.sessaoRepository = sessaoRepository;
        this.contaRepository = contaRepository;
        this.tokenService = tokenService;
        this.properties = properties;
        this.clock = clock;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public TokensAutenticacao iniciar(Authentication authentication) {
        ContaAcesso conta = contaRepository
                .findByLoginIgnoreCase(authentication.getName())
                .orElseThrow(RefreshTokenInvalidoException::new);

        if (!conta.podeAutenticar()) {
            throw new RefreshTokenInvalidoException();
        }

        Instant agora = clock.instant();
        String familiaId = UUID.randomUUID().toString();
        Instant expiraEm = agora.plus(properties.absoluteExpiration());

        return emitirPar(conta, authentication, familiaId, agora, expiraEm);
    }

    @Transactional(noRollbackFor = RefreshTokenInvalidoException.class)
    public TokensAutenticacao renovar(String refreshToken) {
        String hash;
        try {
            hash = calcularHash(refreshToken);
        } catch (RefreshTokenInvalidoException exception) {
            auditoriaService.registrar(TipoEventoSeguranca.REFRESH_FALHA, null, null, null, null);
            throw exception;
        }

        Optional<SessaoRefreshToken> sessaoOpt = sessaoRepository.buscarPorHashParaAtualizacao(hash);
        if (sessaoOpt.isEmpty()) {
            auditoriaService.registrar(TipoEventoSeguranca.REFRESH_FALHA, null, null, null, null);
            throw new RefreshTokenInvalidoException();
        }

        SessaoRefreshToken sessao = sessaoOpt.get();
        Long contaId = sessao.getConta().getId();
        Instant agora = clock.instant();

        try {
            validarSessaoParaRenovacao(sessao, agora);
        } catch (RefreshTokenInvalidoException exception) {
            auditoriaService.registrar(TipoEventoSeguranca.REFRESH_FALHA, contaId, contaId, null, null);
            throw exception;
        }

        sessao.marcarUtilizada(agora);

        Authentication authentication = criarAuthentication(sessao.getConta());
        TokensAutenticacao tokens = emitirPar(
                sessao.getConta(),
                authentication,
                sessao.getFamiliaId(),
                agora,
                sessao.getExpiraEm());

        auditoriaService.registrar(TipoEventoSeguranca.REFRESH_SUCESSO, contaId, contaId, null, null);
        return tokens;
    }

    @Transactional
    public void encerrar(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        String hash = calcularHash(refreshToken);
        sessaoRepository.buscarPorHashParaAtualizacao(hash)
                .ifPresent(sessao -> {
                    Instant agora = clock.instant();
                    sessaoRepository.revogarFamilia(
                            sessao.getFamiliaId(),
                            agora,
                            MotivoRevogacaoRefreshToken.LOGOUT);
                    Long contaId = sessao.getConta().getId();
                    auditoriaService.registrar(
                            TipoEventoSeguranca.LOGOUT,
                            contaId,
                            contaId,
                            null,
                            null
                    );
                });
    }

    @Transactional
    public void encerrarTodas(String login) {
        String loginLimpo = login != null ? login.trim() : "";
        ContaAcesso conta = contaRepository.findByLoginIgnoreCase(loginLimpo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de acesso não encontrada"));

        Instant agora = clock.instant();
        sessaoRepository.revogarTodasPorConta(
                conta.getId(),
                agora,
                MotivoRevogacaoRefreshToken.LOGOUT_GLOBAL);
        auditoriaService.registrar(
                TipoEventoSeguranca.LOGOUT_GLOBAL,
                conta.getId(),
                conta.getId(),
                null,
                null
        );
    }

    public static String gerarHashSha256(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new RefreshTokenInvalidoException();
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(valor.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível", exception);
        }
    }

    private void validarSessaoParaRenovacao(SessaoRefreshToken sessao, Instant agora) {
        if (sessao.foiRevogada()) {
            throw new RefreshTokenInvalidoException();
        }
        if (sessao.foiUtilizada()) {
            sessaoRepository.revogarFamilia(
                    sessao.getFamiliaId(),
                    agora,
                    MotivoRevogacaoRefreshToken.REUTILIZACAO);
            throw new RefreshTokenInvalidoException();
        }
        if (sessao.atingiuExpiracaoAbsoluta(agora)) {
            sessaoRepository.revogarFamilia(
                    sessao.getFamiliaId(),
                    agora,
                    MotivoRevogacaoRefreshToken.EXPIRACAO_ABSOLUTA);
            throw new RefreshTokenInvalidoException();
        }
        if (sessao.expirouPorInatividade(agora, properties.idleTimeout())) {
            sessaoRepository.revogarFamilia(
                    sessao.getFamiliaId(),
                    agora,
                    MotivoRevogacaoRefreshToken.INATIVIDADE);
            throw new RefreshTokenInvalidoException();
        }
        if (!sessao.getConta().podeAutenticar()) {
            sessaoRepository.revogarFamilia(
                    sessao.getFamiliaId(),
                    agora,
                    MotivoRevogacaoRefreshToken.CONTA_INATIVA);
            throw new RefreshTokenInvalidoException();
        }
    }

    private TokensAutenticacao emitirPar(
            ContaAcesso conta,
            Authentication authentication,
            String familiaId,
            Instant agora,
            Instant expiraEm) {
        String refreshToken = gerarRefreshToken();
        String tokenHash = calcularHash(refreshToken);

        SessaoRefreshToken sessao = new SessaoRefreshToken(
                conta,
                familiaId,
                tokenHash,
                agora,
                agora,
                expiraEm);
        sessaoRepository.save(sessao);

        TokenAcesso accessToken = tokenService.emitir(authentication, familiaId);
        return new TokensAutenticacao(accessToken, refreshToken, expiraEm);
    }

    private Authentication criarAuthentication(ContaAcesso conta) {
        List<SimpleGrantedAuthority> autoridades = conta.getPerfis().stream()
                .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.name()))
                .toList();

        return UsernamePasswordAuthenticationToken.authenticated(
                conta.getLogin(),
                null,
                autoridades);
    }

    private String gerarRefreshToken() {
        byte[] bytes = new byte[TAMANHO_REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String calcularHash(String refreshToken) {
        return gerarHashSha256(refreshToken);
    }
}