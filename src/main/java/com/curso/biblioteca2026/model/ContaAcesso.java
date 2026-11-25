package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "conta_acesso",
        uniqueConstraints = @UniqueConstraint(name = "uk_conta_acesso_pessoa", columnNames = "pessoa_id")
)
public class ContaAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pessoa_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_conta_acesso_pessoa")
    )
    private Pessoa pessoa;

    @Column(nullable = false, length = 80)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusContaAcesso status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "conta_acesso_perfil",
            joinColumns = @JoinColumn(
                    name = "conta_acesso_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk_conta_acesso_perfil_conta")
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 30)
    private Set<PerfilAcesso> perfis = new LinkedHashSet<>();

    protected ContaAcesso() {
    }

    public ContaAcesso(Pessoa pessoa, String login, String senhaHash) {
        this.pessoa = Objects.requireNonNull(pessoa, "Pessoa é obrigatória");
        this.login = validarLogin(login);
        this.senhaHash = validarHash(senhaHash);
        this.status = StatusContaAcesso.ATIVA;
        this.perfis.add(PerfilAcesso.OPERADOR);
    }

    public Long getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getLogin() {
        return login;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public StatusContaAcesso getStatus() {
        return status;
    }

    public Set<PerfilAcesso> getPerfis() {
        return Collections.unmodifiableSet(perfis);
    }

    public boolean podeAutenticar() {
        return this.status == StatusContaAcesso.ATIVA
                && this.pessoa != null
                && this.pessoa.getStatus() == Status.ATIVO;
    }

    public void alterarSenha(String novoSenhaHash) {
        this.senhaHash = validarHash(novoSenhaHash);
    }

    public void bloquear() {
        this.status = StatusContaAcesso.BLOQUEADA;
    }

    public void ativar() {
        this.status = StatusContaAcesso.ATIVA;
    }

    private static String validarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Login é obrigatório");
        }
        String valor = login.trim();
        if (valor.length() < 3 || valor.length() > 80 || !valor.matches("^[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException("Login inválido");
        }
        return valor;
    }

    private static String validarHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) {
            throw new IllegalArgumentException("Hash da senha é obrigatório");
        }
        return senhaHash;
    }

    @Override
    public String toString() {
        return "ContaAcesso{id=" + id + ", login='" + login + "', status=" + status + ", perfis=" + perfis + '}';
    }
}