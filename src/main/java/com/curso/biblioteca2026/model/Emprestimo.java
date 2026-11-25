package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "emprestimo")
public class Emprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leitor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emprestimo_leitor"))
    private Leitor leitor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bibliotecario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emprestimo_bibliotecario"))
    private Bibliotecario bibliotecario;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDateTime dataEmprestimo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmprestimo status;

    @OneToMany(mappedBy = "emprestimo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ItemEmprestimo> itens = new ArrayList<>();

    protected Emprestimo() {
    }

    public Emprestimo(Leitor leitor, Bibliotecario bibliotecario, LocalDateTime dataEmprestimo) {
        this.leitor = Objects.requireNonNull(leitor, "Leitor é obrigatório");
        this.bibliotecario = Objects.requireNonNull(bibliotecario, "Bibliotecário é obrigatório");
        this.dataEmprestimo = Objects.requireNonNull(dataEmprestimo, "Data do empréstimo é obrigatória");
        this.status = StatusEmprestimo.ABERTO;
    }

    public Long getId() {
        return id;
    }

    public Leitor getLeitor() {
        return leitor;
    }

    public Bibliotecario getBibliotecario() {
        return bibliotecario;
    }

    public LocalDateTime getDataEmprestimo() {
        return dataEmprestimo;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public List<ItemEmprestimo> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public ItemEmprestimo adicionarItem(Livro livro, Integer quantidade) {
        validarAberto();
        Objects.requireNonNull(livro, "Livro é obrigatório");
        boolean repetido = itens.stream()
                .anyMatch(item -> Objects.equals(item.getLivro().getId(), livro.getId()));
        if (repetido) {
            throw new IllegalArgumentException("Livro já incluído no empréstimo");
        }
        ItemEmprestimo item = new ItemEmprestimo(this, livro, quantidade, livro.getValorReposicao());
        itens.add(item);
        return item;
    }

    public void removerItem(Long itemId) {
        validarAberto();
        boolean removido = itens.removeIf(item -> Objects.equals(item.getId(), itemId));
        if (!removido) {
            throw new IllegalArgumentException("Item não pertence ao empréstimo");
        }
    }

    public void finalizar() {
        validarAberto();
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("Empréstimo deve possuir ao menos um item");
        }
        this.status = StatusEmprestimo.FINALIZADO;
    }

    public void cancelar() {
        validarAberto();
        this.status = StatusEmprestimo.CANCELADO;
    }

    @Transient
    public BigDecimal calcularValorTotalReposicao() {
        return itens.stream()
                .map(ItemEmprestimo::calcularSubtotalReposicao)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validarAberto() {
        if (this.status != StatusEmprestimo.ABERTO) {
            throw new IllegalStateException("Somente empréstimos abertos podem ser alterados");
        }
    }
}