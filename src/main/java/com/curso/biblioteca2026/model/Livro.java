package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @Column(length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private Integer quantidadeExemplares;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorReposicao;

    @Column(nullable = false)
    private LocalDate dataCadastro;

    @Column(nullable = false)
    private Boolean ativo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaLivro categoria;

    public Livro() {
    }

    public Livro(String isbn, String titulo, Integer quantidadeExemplares,
                 BigDecimal valorReposicao, LocalDate dataCadastro,
                 Boolean ativo, CategoriaLivro categoria) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.quantidadeExemplares = quantidadeExemplares;
        this.valorReposicao = valorReposicao;
        this.dataCadastro = dataCadastro;
        this.ativo = ativo;
        this.categoria = categoria;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getQuantidadeExemplares() {
        return quantidadeExemplares;
    }

    public void setQuantidadeExemplares(Integer quantidadeExemplares) {
        this.quantidadeExemplares = quantidadeExemplares;
    }

    public BigDecimal getValorReposicao() {
        return valorReposicao;
    }

    public void setValorReposicao(BigDecimal valorReposicao) {
        this.valorReposicao = valorReposicao;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public CategoriaLivro getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaLivro categoria) {
        this.categoria = categoria;
    }

    @Transient
    public BigDecimal getValorTotal() {
        return valorReposicao.multiply(
                BigDecimal.valueOf(quantidadeExemplares)
        );
    }
}