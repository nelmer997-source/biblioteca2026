package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sessao_refresh_token")
public class SessaoRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "conta_acesso_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessao_refresh_token_conta")
    )
    private ContaAcesso conta;

    @Column(name = "familia_id", nullable = false, length = 36)
    private String familiaId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    @Column(name = "ultima_atividade_em", nullable = false)
    private Instant ultimaAtividadeEm;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "utilizada_em")
    private Instant utilizadaEm;

    @Column(name = "revogada_em")
    private Instant revogadaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_revogacao", length = 30)
    private MotivoRevogacaoRefreshToken motivoRevogacao;

    protected SessaoRefreshToken() {
    }

    public SessaoRefreshToken(
            ContaAcesso conta,
            String familiaId,
            String tokenHash,
            Instant criadaEm,
            Instant expiraEm) {
        this(conta, familiaId, tokenHash, criadaEm, criadaEm, expiraEm);
    }

    public SessaoRefreshToken(
            ContaAcesso conta,
            String familiaId,
            String tokenHash,
            Instant criadaEm,
            Instant ultimaAtividadeEm,
            Instant expiraEm) {
        this.conta = Objects.requireNonNull(conta, "Conta de acesso é obrigatória");
        this.familiaId = validarTextoObrigatorio(familiaId, "Identificador da família é obrigatório");
        this.tokenHash = validarTextoObrigatorio(tokenHash, "Hash do token é obrigatório");
        this.criadaEm = Objects.requireNonNull(criadaEm, "Instante de criação é obrigatório");
        this.ultimaAtividadeEm = Objects.requireNonNull(ultimaAtividadeEm, "Instante de atividade é obrigatório");
        this.expiraEm = Objects.requireNonNull(expiraEm, "Instante de expiração é obrigatório");
        if (!this.expiraEm.isAfter(this.criadaEm)) {
            throw new IllegalArgumentException("Expiração absoluta deve ser posterior à criação");
        }
    }

    public Long getId() {
        return id;
    }

    public ContaAcesso getConta() {
        return conta;
    }

    public String getFamiliaId() {
        return familiaId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getUltimaAtividadeEm() {
        return ultimaAtividadeEm;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }

    public Instant getUtilizadaEm() {
        return utilizadaEm;
    }

    public Instant getRevogadaEm() {
        return revogadaEm;
    }

    public MotivoRevogacaoRefreshToken getMotivoRevogacao() {
        return motivoRevogacao;
    }

    public boolean foiUtilizada() {
        return utilizadaEm != null;
    }

    public boolean foiRevogada() {
        return revogadaEm != null;
    }

    public boolean expirouPorInatividade(Instant agora, Duration idleTimeout) {
        Objects.requireNonNull(agora, "Instante atual é obrigatório");
        Objects.requireNonNull(idleTimeout, "Idle timeout é obrigatório");
        Instant limite = ultimaAtividadeEm.plus(idleTimeout);
        return !agora.isBefore(limite);
    }

    public boolean atingiuExpiracaoAbsoluta(Instant agora) {
        Objects.requireNonNull(agora, "Instante atual é obrigatório");
        return !agora.isBefore(expiraEm);
    }

    public boolean estaAtiva(Instant agora, Duration idleTimeout) {
        return !foiUtilizada()
                && !foiRevogada()
                && !expirouPorInatividade(agora, idleTimeout)
                && !atingiuExpiracaoAbsoluta(agora);
    }

    public void marcarUtilizada(Instant agora) {
        Objects.requireNonNull(agora, "Instante de utilização é obrigatório");
        if (foiRevogada()) {
            throw new IllegalStateException("Sessão revogada não pode ser utilizada");
        }
        if (foiUtilizada()) {
            throw new IllegalStateException("Refresh token já utilizado");
        }
        this.utilizadaEm = agora;
        this.ultimaAtividadeEm = agora;
    }

    public void revogar(Instant agora, MotivoRevogacaoRefreshToken motivo) {
        Objects.requireNonNull(agora, "Instante de revogação é obrigatório");
        Objects.requireNonNull(motivo, "Motivo de revogação é obrigatório");
        if (this.revogadaEm == null) {
            this.revogadaEm = agora;
            this.motivoRevogacao = motivo;
        }
    }

    private static String validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    @Override
    public String toString() {
        return "SessaoRefreshToken{id=" + id
                + ", familiaId='" + familiaId + '\''
                + ", criadaEm=" + criadaEm
                + ", utilizadaEm=" + utilizadaEm
                + ", revogadaEm=" + revogadaEm
                + ", motivoRevogacao=" + motivoRevogacao + '}';
    }
}