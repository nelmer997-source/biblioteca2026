package com.curso.biblioteca2026.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LivroTest {

    @Test
    void deveAtualizarSaldoECalcularValorTotalNaMovimentacaoDeExemplares() {
        CategoriaLivro categoria = new CategoriaLivro("Literatura");
        Livro livro = new Livro(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 11, 25),
                true,
                categoria
        );

        livro.receberExemplares(3);
        assertEquals(8, livro.getQuantidadeExemplares());

        livro.retirarExemplares(2);
        assertEquals(6, livro.getQuantidadeExemplares());
        assertEquals(0, new BigDecimal("300.00").compareTo(livro.getValorTotal()));
    }

    @Test
    void naoDevePermitirRetiradaAcimaDoSaldoOuQuantidadeNaoPositiva() {
        CategoriaLivro categoria = new CategoriaLivro("Literatura");
        Livro livro = new Livro(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 11, 25),
                true,
                categoria
        );

        assertThrows(IllegalArgumentException.class, () -> livro.retirarExemplares(6));
        assertThrows(IllegalArgumentException.class, () -> livro.retirarExemplares(0));
        assertThrows(IllegalArgumentException.class, () -> livro.receberExemplares(0));
        assertEquals(5, livro.getQuantidadeExemplares());
    }

    @Test
    void deveRefletirStatusAtivoEInativo() {
        CategoriaLivro categoria = new CategoriaLivro("Literatura");
        Livro livro = new Livro(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 11, 25),
                true,
                categoria
        );

        assertTrue(livro.getAtivo());

        livro.alterarStatus(Status.INATIVO);
        assertFalse(livro.getAtivo());
        assertEquals(Status.INATIVO, livro.getStatus());
    }
}