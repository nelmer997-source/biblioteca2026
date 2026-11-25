package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias_livro")
public class CategoriaLivro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ATIVO;

    protected CategoriaLivro() {
    }

    public CategoriaLivro(String nome) {
        alterarNome(nome);
        this.status = Status.ATIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Status getStatus() {
        return status;
    }

    public void alterarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da categoria é obrigatório");
        }
        this.nome = nome.trim();
    }

    public void alterarStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("O status é obrigatório");
        }
        this.status = status;
    }

    public void ativar() {
        this.status = Status.ATIVO;
    }

    public void inativar() {
        this.status = Status.INATIVO;
    }
}
