package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private Integer quantidadeExemplares;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorReposicao;

    @Column(nullable = false)
    private LocalDate dataCadastro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ATIVO;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaLivro categoria;

    protected Livro() {
    }

    public Livro(String isbn, String titulo, Integer quantidadeExemplares,
                 BigDecimal valorReposicao, LocalDate dataCadastro,
                 Boolean ativo, CategoriaLivro categoria) {
        this.isbn = isbn;
        alterarTitulo(titulo);
        definirQuantidadeInicial(quantidadeExemplares);
        alterarValorReposicao(valorReposicao);
        this.dataCadastro = dataCadastro != null ? dataCadastro : LocalDate.now();
        this.status = Boolean.FALSE.equals(ativo) ? Status.INATIVO : Status.ATIVO;
        alterarCategoria(categoria);
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getQuantidadeExemplares() {
        return quantidadeExemplares;
    }

    public BigDecimal getValorReposicao() {
        return valorReposicao;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public Status getStatus() {
        return status;
    }

    public Boolean getAtivo() {
        return status == Status.ATIVO;
    }

    public CategoriaLivro getCategoria() {
        return categoria;
    }

    public void alterarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título do livro é obrigatório");
        }
        this.titulo = titulo.trim();
    }

    public void alterarValorReposicao(BigDecimal valorReposicao) {
        if (valorReposicao == null || valorReposicao.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O valor de reposição não pode ser negativo");
        }
        this.valorReposicao = valorReposicao;
    }

    public void alterarCategoria(CategoriaLivro categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("A categoria é obrigatória");
        }
        this.categoria = categoria;
    }

    public void alterarStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("O status é obrigatório");
        }
        this.status = status;
    }

    public void receberExemplares(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser maior que zero");
        }
        this.quantidadeExemplares += quantidade;
    }

    public void retirarExemplares(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de saída deve ser maior que zero");
        }
        if (this.quantidadeExemplares < quantidade) {
            throw new IllegalArgumentException("Quantidade de exemplares insuficiente para retirada");
        }
        this.quantidadeExemplares -= quantidade;
    }

    private void definirQuantidadeInicial(Integer quantidadeExemplares) {
        if (quantidadeExemplares == null || quantidadeExemplares < 0) {
            throw new IllegalArgumentException("A quantidade de exemplares não pode ser negativa");
        }
        this.quantidadeExemplares = quantidadeExemplares;
    }

    @Transient
    public BigDecimal getValorTotal() {
        BigDecimal valor = valorReposicao != null ? valorReposicao : BigDecimal.ZERO;
        int qtd = quantidadeExemplares != null ? quantidadeExemplares : 0;
        return valor.multiply(BigDecimal.valueOf(qtd));
    }
}
