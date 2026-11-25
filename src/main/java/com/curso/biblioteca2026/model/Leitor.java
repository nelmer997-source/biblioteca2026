package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "leitor",
        uniqueConstraints = @UniqueConstraint(name = "uk_leitor_pessoa", columnNames = "pessoa_id")
)
public class Leitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pessoa_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_leitor_pessoa")
    )
    private Pessoa pessoa;

    @Column(length = 30)
    private String telefone;

    protected Leitor() {
    }

    public Leitor(Pessoa pessoa, String telefone) {
        this.pessoa = Objects.requireNonNull(pessoa, "Pessoa é obrigatória");
        this.telefone = normalizarOpcional(telefone);
    }

    public Long getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getTelefone() {
        return telefone;
    }

    public void alterarDados(String telefone) {
        this.telefone = normalizarOpcional(telefone);
    }

    private static String normalizarOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}