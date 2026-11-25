package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        name = "item_emprestimo",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_item_emprestimo_livro",
                columnNames = {"emprestimo_id", "livro_id"}
        )
)
public class ItemEmprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emprestimo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_emprestimo_emprestimo"))
    private Emprestimo emprestimo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "livro_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_emprestimo_livro"))
    private Livro livro;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "valor_reposicao_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorReposicaoUnitario;

    protected ItemEmprestimo() {
    }

    ItemEmprestimo(Emprestimo emprestimo, Livro livro, Integer quantidade, BigDecimal valorReposicaoUnitario) {
        this.emprestimo = Objects.requireNonNull(emprestimo, "Empréstimo é obrigatório");
        this.livro = Objects.requireNonNull(livro, "Livro é obrigatório");
        this.quantidade = validarPositivo(quantidade);
        this.valorReposicaoUnitario = validarNaoNegativo(valorReposicaoUnitario);
    }

    public Long getId() {
        return id;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public Livro getLivro() {
        return livro;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getValorReposicaoUnitario() {
        return valorReposicaoUnitario;
    }

    @Transient
    public BigDecimal calcularSubtotalReposicao() {
        return valorReposicaoUnitario
                .multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static Integer validarPositivo(Integer valor) {
        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        return valor;
    }

    private static BigDecimal validarNaoNegativo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de reposição não pode ser negativo");
        }
        return valor;
    }
}