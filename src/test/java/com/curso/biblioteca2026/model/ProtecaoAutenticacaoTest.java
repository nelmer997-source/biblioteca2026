package com.curso.biblioteca2026.model;

import com.curso.biblioteca2026.config.ProtecaoAutenticacaoProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ProtecaoAutenticacaoTest {

    private static final Instant INICIO = Instant.parse("2026-10-29T12:00:00Z");
    private static final String HASH_VALIDO = "a".repeat(64);

    @Test
    void deveAtivarBloqueioExatamenteNaQuintaFalhaELiberarNoFimDoPrazo() {
        ProtecaoAutenticacao protecao = new ProtecaoAutenticacao(HASH_VALIDO);
        Duration janela = Duration.ofMinutes(15);
        Duration bloqueio = Duration.ofMinutes(15);

        for (int tentativa = 1; tentativa <= 4; tentativa++) {
            boolean ativou = protecao.registrarFalha(
                    INICIO.plusSeconds(tentativa * 10L), 5, janela, bloqueio);
            assertFalse(ativou);
            assertFalse(protecao.estaBloqueada(INICIO.plusSeconds(tentativa * 10L)));
        }

        Instant instanteQuintaFalha = INICIO.plus(Duration.ofMinutes(2));
        boolean ativouNaQuinta = protecao.registrarFalha(instanteQuintaFalha, 5, janela, bloqueio);

        assertTrue(ativouNaQuinta);
        assertEquals(5, protecao.getFalhasConsecutivas());
        assertEquals(instanteQuintaFalha.plus(bloqueio), protecao.getBloqueadaAte());

        assertTrue(protecao.estaBloqueada(instanteQuintaFalha.plus(bloqueio).minusMillis(1)));
        assertFalse(protecao.estaBloqueada(instanteQuintaFalha.plus(bloqueio)));
    }

    @Test
    void deveLimparFalhasAposSucessoEReiniciarJanelaAposExpiracao() {
        ProtecaoAutenticacao protecao = new ProtecaoAutenticacao(HASH_VALIDO);
        Duration janela = Duration.ofMinutes(15);
        Duration bloqueio = Duration.ofMinutes(15);

        protecao.registrarFalha(INICIO, 5, janela, bloqueio);
        protecao.registrarFalha(INICIO.plus(Duration.ofMinutes(1)), 5, janela, bloqueio);
        assertEquals(2, protecao.getFalhasConsecutivas());

        protecao.registrarSucesso();
        assertEquals(0, protecao.getFalhasConsecutivas());
        assertNull(protecao.getJanelaIniciadaEm());
        assertNull(protecao.getBloqueadaAte());

        protecao.registrarFalha(INICIO, 5, janela, bloqueio);
        protecao.registrarFalha(INICIO.plus(Duration.ofMinutes(5)), 5, janela, bloqueio);
        assertEquals(2, protecao.getFalhasConsecutivas());

        // Exatamente ao expirar a janela de 15 minutos, a nova falha inicia nova contagem
        protecao.registrarFalha(INICIO.plus(Duration.ofMinutes(15)), 5, janela, bloqueio);
        assertEquals(1, protecao.getFalhasConsecutivas());
        assertEquals(INICIO.plus(Duration.ofMinutes(15)), protecao.getJanelaIniciadaEm());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "127.0.0.1\r\nFAKE: line",
            "127.0.0.1\nFAKE",
            "127.0.0.1\rFAKE",
            "1234567890123456789012345678901234567890123456"
    })
    void deveRejeitarEnderecoOrigemComQuebraDeLinhaOuAcimaDe45Caracteres(String origemInvalida) {
        assertThrows(
                IllegalArgumentException.class,
                () -> new EventoSeguranca(
                        TipoEventoSeguranca.LOGIN_FALHA,
                        INICIO,
                        null,
                        null,
                        HASH_VALIDO,
                        origemInvalida
                )
        );
    }

    @Test
    void deveNormalizarEnderecoEmBrancoERejeitarIdentificadorHashInvalido() {
        EventoSeguranca evento = new EventoSeguranca(
                TipoEventoSeguranca.LOGIN_SUCESSO,
                INICIO,
                1L,
                1L,
                HASH_VALIDO,
                "   "
        );
        assertNull(evento.getEnderecoOrigem());

        assertThrows(
                IllegalArgumentException.class,
                () -> new EventoSeguranca(
                        TipoEventoSeguranca.LOGIN_FALHA,
                        INICIO,
                        null,
                        null,
                        "login-em-texto-aberto",
                        "127.0.0.1"
                )
        );
    }

    @Test
    void deveValidarParametrosDeProtecaoAutenticacaoProperties() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProtecaoAutenticacaoProperties(0, Duration.ofMinutes(15), Duration.ofMinutes(15)));
        assertThrows(IllegalArgumentException.class,
                () -> new ProtecaoAutenticacaoProperties(5, Duration.ZERO, Duration.ofMinutes(15)));
        assertThrows(IllegalArgumentException.class,
                () -> new ProtecaoAutenticacaoProperties(5, Duration.ofMinutes(15), Duration.ofMinutes(-1)));
    }
}