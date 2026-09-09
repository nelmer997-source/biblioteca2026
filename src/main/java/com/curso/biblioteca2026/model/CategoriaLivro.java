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

    public CategoriaLivro() {
    }

    public CategoriaLivro(String nome) {
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}