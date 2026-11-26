package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.config.ProtecaoAutenticacaoProperties;
import com.curso.biblioteca2026.model.ProtecaoAutenticacao;
import com.curso.biblioteca2026.model.TipoEventoSeguranca;
import com.curso.biblioteca2026.repository.ProtecaoAutenticacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class ProtecaoAutenticacaoService {

    private final ProtecaoAutenticacaoRepository repository;
    private final AuditoriaSegurancaService auditoriaService;
    private final ProtecaoAutenticacaoProperties properties;
    private final Clock clock;

    public ProtecaoAutenticacaoService(
            ProtecaoAutenticacaoRepository repository,
            AuditoriaSegurancaService auditoriaService,
            ProtecaoAutenticacaoProperties properties,
            Clock clock) {
        this.repository = repository;
        this.auditoriaService = auditoriaService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public boolean permitirTentativa(String login, String enderecoOrigem) {
        String chaveLogin = gerarChaveLogin(login);
        Instant agora = clock.instant();
        boolean bloqueada = repository.findByChaveLogin(chaveLogin)
                .map(protecao -> protecao.estaBloqueada(agora))
                .orElse(false);

        if (bloqueada) {
            auditoriaService.registrar(
                    TipoEventoSeguranca.LOGIN_RECUSADO_BLOQUEIO,
                    null,
                    null,
                    chaveLogin,
                    enderecoOrigem
            );
            return false;
        }
        return true;
    }

    @Transactional
    public void registrarFalha(String login, String enderecoOrigem) {
        String chaveLogin = gerarChaveLogin(login);
        Instant agora = clock.instant();

        repository.inserirSeAusente(chaveLogin);
        ProtecaoAutenticacao protecao = repository.buscarParaAtualizacao(chaveLogin)
                .orElseThrow(() -> new IllegalStateException("Registro de proteção não encontrado após upsert"));

        boolean ativouBloqueio = protecao.registrarFalha(
                agora,
                properties.maxFailedAttempts(),
                properties.failureWindow(),
                properties.lockDuration()
        );

        auditoriaService.registrar(
                TipoEventoSeguranca.LOGIN_FALHA,
                null,
                null,
                chaveLogin,
                enderecoOrigem
        );

        if (ativouBloqueio) {
            auditoriaService.registrar(
                    TipoEventoSeguranca.BLOQUEIO_TEMPORARIO_ATIVADO,
                    null,
                    null,
                    chaveLogin,
                    enderecoOrigem
            );
        }
    }

    @Transactional
    public void registrarSucesso(String login, Long contaId, String enderecoOrigem) {
        String chaveLogin = gerarChaveLogin(login);
        repository.buscarParaAtualizacao(chaveLogin)
                .ifPresent(ProtecaoAutenticacao::registrarSucesso);

        auditoriaService.registrar(
                TipoEventoSeguranca.LOGIN_SUCESSO,
                contaId,
                contaId,
                chaveLogin,
                enderecoOrigem
        );
    }

    public static String gerarChaveLogin(String login) {
        String normalizado = login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalizado.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível", exception);
        }
    }
}