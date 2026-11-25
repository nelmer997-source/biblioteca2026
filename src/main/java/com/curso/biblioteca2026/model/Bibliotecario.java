package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "bibliotecario",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bibliotecario_pessoa", columnNames = "pessoa_id"),
                @UniqueConstraint(name = "uk_bibliotecario_matricula", columnNames = "matricula")
        }
)
public class Bibliotecario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pessoa_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_bibliotecario_pessoa")
    )
    private Pessoa pessoa;

    @Column(nullable = false, length = 30)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FuncaoBibliotecario funcao;

    @Column(name = "data_admissao", nullable = false)
    private LocalDate dataAdmissao;

    protected Bibliotecario() {
    }

    public Bibliotecario(Pessoa pessoa, String matricula, FuncaoBibliotecario funcao, LocalDate dataAdmissao) {
        this.pessoa = Objects.requireNonNull(pessoa, "Pessoa é obrigatória");
        this.matricula = validarMatricula(matricula);
        this.funcao = Objects.requireNonNull(funcao, "Função é obrigatória");
        this.dataAdmissao = Objects.requireNonNull(dataAdmissao, "Data de admissão é obrigatória");
    }

    public Long getId() {
        return id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public String getMatricula() {
        return matricula;
    }

    public FuncaoBibliotecario getFuncao() {
        return funcao;
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void alterarDados(String matricula, FuncaoBibliotecario funcao) {
        this.matricula = validarMatricula(matricula);
        this.funcao = Objects.requireNonNull(funcao, "Função é obrigatória");
    }

    private static String validarMatricula(String matricula) {
        if (matricula == null || matricula.isBlank()) {
            throw new IllegalArgumentException("Matrícula é obrigatória");
        }
        return matricula.trim();
    }
}