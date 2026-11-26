package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "protecao_autenticacao")
public class ProtecaoAutenticacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chave_login", nullable = false, unique = true, length = 64)
    private String chaveLogin;

    @Column(name = "falhas_consecutivas", nullable = false)
    private int falhasConsecutivas;

    @Column(name = "janela_iniciada_em")
    private Instant janelaIniciadaEm;

    @Column(name = "bloqueada_ate")
    private Instant bloqueadaAte;

    protected ProtecaoAutenticacao() {
    }

    public ProtecaoAutenticacao(String chaveLogin) {
        this.chaveLogin = validarChaveLogin(chaveLogin);
        this.falhasConsecutivas = 0;
    }

    public Long getId() {
        return id;
    }

    public String getChaveLogin() {
        return chaveLogin;
    }

    public int getFalhasConsecutivas() {
        return falhasConsecutivas;
    }

    public Instant getJanelaIniciadaEm() {
        return janelaIniciadaEm;
    }

    public Instant getBloqueadaAte() {
        return bloqueadaAte;
    }

    public boolean estaBloqueada(Instant agora) {
        Objects.requireNonNull(agora, "Instante atual é obrigatório");
        return bloqueadaAte != null && agora.isBefore(bloqueadaAte);
    }

    public boolean registrarFalha(
            Instant agora,
            int maximoTentativas,
            Duration janela,
            Duration duracaoBloqueio) {
        Objects.requireNonNull(agora, "Instante atual é obrigatório");
        if (maximoTentativas < 1) {
            throw new IllegalArgumentException("Máximo de falhas deve ser positivo");
        }
        if (janela == null || janela.isZero() || janela.isNegative()) {
            throw new IllegalArgumentException("Janela de falhas deve ser positiva");
        }
        if (duracaoBloqueio == null || duracaoBloqueio.isZero() || duracaoBloqueio.isNegative()) {
            throw new IllegalArgumentException("Duração do bloqueio deve ser positiva");
        }

        if (janelaIniciadaEm == null || !agora.isBefore(janelaIniciadaEm.plus(janela))) {
            falhasConsecutivas = 0;
            janelaIniciadaEm = agora;
        }

        falhasConsecutivas++;
        if (falhasConsecutivas >= maximoTentativas) {
            bloqueadaAte = agora.plus(duracaoBloqueio);
            return true;
        }
        return false;
    }

    public void registrarSucesso() {
        falhasConsecutivas = 0;
        janelaIniciadaEm = null;
        bloqueadaAte = null;
    }

    private static String validarChaveLogin(String chaveLogin) {
        if (chaveLogin == null || !chaveLogin.matches("^[0-9a-f]{64}$")) {
            throw new IllegalArgumentException("Chave de login inválida");
        }
        return chaveLogin;
    }
}