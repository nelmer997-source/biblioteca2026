package com.curso.biblioteca2026.model;

import com.curso.biblioteca2026.config.RefreshTokenProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SessaoRefreshTokenTest {

    private static final Instant CRIACAO = Instant.parse("2026-10-29T12:00:00Z");

    @Test
    void deveExpirarAoAtingirOLimiteDeInatividade() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));

        assertFalse(sessao.expirouPorInatividade(
                CRIACAO.plus(Duration.ofMinutes(29)).plusSeconds(59),
                Duration.ofMinutes(30)));

        assertTrue(sessao.expirouPorInatividade(
                CRIACAO.plus(Duration.ofMinutes(30)),
                Duration.ofMinutes(30)));
    }

    @Test
    void deveExpirarAoAtingirOLimiteAbsoluto() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));

        assertFalse(sessao.atingiuExpiracaoAbsoluta(
                CRIACAO.plus(Duration.ofHours(7)).plus(Duration.ofMinutes(59)).plusSeconds(59)));

        assertTrue(sessao.atingiuExpiracaoAbsoluta(
                CRIACAO.plus(Duration.ofHours(8))));
    }

    @Test
    void deveMarcarUtilizadaEImpedirReutilizacaoOuUsoAposRevogacao() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));
        Instant instanteUso = CRIACAO.plus(Duration.ofMinutes(10));

        assertTrue(sessao.estaAtiva(instanteUso, Duration.ofMinutes(30)));
        sessao.marcarUtilizada(instanteUso);

        assertTrue(sessao.foiUtilizada());
        assertEquals(instanteUso, sessao.getUtilizadaEm());
        assertEquals(instanteUso, sessao.getUltimaAtividadeEm());
        assertFalse(sessao.estaAtiva(instanteUso.plusSeconds(1), Duration.ofMinutes(30)));

        assertThrows(IllegalStateException.class, () -> sessao.marcarUtilizada(instanteUso.plusSeconds(5)));

        SessaoRefreshToken sessaoRevogada = novaSessao(CRIACAO.plus(Duration.ofHours(8)));
        sessaoRevogada.revogar(instanteUso, MotivoRevogacaoRefreshToken.LOGOUT);
        sessaoRevogada.revogar(instanteUso.plusSeconds(10), MotivoRevogacaoRefreshToken.REUTILIZACAO);

        assertTrue(sessaoRevogada.foiRevogada());
        assertEquals(MotivoRevogacaoRefreshToken.LOGOUT, sessaoRevogada.getMotivoRevogacao());
        assertThrows(IllegalStateException.class, () -> sessaoRevogada.marcarUtilizada(instanteUso.plusSeconds(20)));
    }

    @Test
    void naoDeveExporHashNoToStringEValidarPropriedades() {
        SessaoRefreshToken sessao = novaSessao(CRIACAO.plus(Duration.ofHours(8)));
        assertFalse(sessao.toString().contains("hash-secreto-2026"));

        assertThrows(IllegalArgumentException.class,
                () -> new RefreshTokenProperties(Duration.ZERO, Duration.ofHours(8)));
        assertThrows(IllegalArgumentException.class,
                () -> new RefreshTokenProperties(Duration.ofMinutes(30), Duration.ofMinutes(30)));
    }

    private SessaoRefreshToken novaSessao(Instant expiraEm) {
        Pessoa pessoa = new Pessoa(
                "Ana Souza",
                "ana.sessao@example.com",
                "52998224725",
                LocalDate.of(2026, 11, 25));
        ContaAcesso conta = new ContaAcesso(pessoa, "ana.sessao", "{bcrypt}hash");
        return new SessaoRefreshToken(
                conta,
                "familia-teste-1",
                "hash-secreto-2026",
                CRIACAO,
                expiraEm);
    }
}