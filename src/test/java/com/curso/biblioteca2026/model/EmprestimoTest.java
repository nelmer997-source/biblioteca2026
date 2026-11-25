package com.curso.biblioteca2026.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmprestimoTest {

    @Test
    void devePreservarValorReposicaoHistoricoDoItem() {
        CategoriaLivro categoria = new CategoriaLivro("Literatura");
        Livro livro = new Livro("9788535902778", "Dom Casmurro", 5, new BigDecimal("50.00"), LocalDate.now(), true, categoria);
        Pessoa pLeitor = new Pessoa("Ana Souza", "ana@example.com", "52998224725", LocalDate.now());
        Pessoa pBib = new Pessoa("Bruno Lima", "bruno@example.com", "11144477735", LocalDate.now());
        Leitor leitor = new Leitor(pLeitor, "(17) 99999-0000");
        Bibliotecario bib = new Bibliotecario(pBib, "BIB-001", FuncaoBibliotecario.ATENDENTE, LocalDate.now());

        Emprestimo emprestimo = new Emprestimo(leitor, bib, LocalDateTime.now());
        emprestimo.adicionarItem(livro, 2);

        livro.alterarValorReposicao(new BigDecimal("80.00"));

        assertEquals(new BigDecimal("100.00"), emprestimo.calcularValorTotalReposicao());
    }

    @Test
    void naoDeveFinalizarEmprestimoVazioOuAlterarFinalizado() {
        Pessoa pLeitor = new Pessoa("Ana Souza", "ana@example.com", "52998224725", LocalDate.now());
        Pessoa pBib = new Pessoa("Bruno Lima", "bruno@example.com", "11144477735", LocalDate.now());
        Emprestimo emprestimo = new Emprestimo(
                new Leitor(pLeitor, null),
                new Bibliotecario(pBib, "BIB-001", FuncaoBibliotecario.ATENDENTE, LocalDate.now()),
                LocalDateTime.now()
        );

        assertThrows(IllegalArgumentException.class, emprestimo::finalizar);

        CategoriaLivro categoria = new CategoriaLivro("Literatura");
        Livro livro = new Livro("9788535902778", "Dom Casmurro", 5, new BigDecimal("50.00"), LocalDate.now(), true, categoria);
        emprestimo.adicionarItem(livro, 1);
        emprestimo.finalizar();

        assertEquals(StatusEmprestimo.FINALIZADO, emprestimo.getStatus());
        assertThrows(IllegalStateException.class, emprestimo::cancelar);
        assertThrows(IllegalStateException.class, () -> emprestimo.adicionarItem(livro, 1));
    }
}