package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.Livro;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LivroResponse(
        String isbn,
        String titulo,
        Integer quantidadeExemplares,
        BigDecimal valorReposicao,
        BigDecimal valorTotal,
        LocalDate dataCadastro,
        Boolean ativo,
        Long categoriaId,
        String categoriaNome
) {

    public static LivroResponse from(Livro livro) {
        return new LivroResponse(
                livro.getIsbn(),
                livro.getTitulo(),
                livro.getQuantidadeExemplares(),
                livro.getValorReposicao(),
                livro.getValorTotal(),
                livro.getDataCadastro(),
                livro.getAtivo(),
                livro.getCategoria().getId(),
                livro.getCategoria().getNome()
        );
    }
}