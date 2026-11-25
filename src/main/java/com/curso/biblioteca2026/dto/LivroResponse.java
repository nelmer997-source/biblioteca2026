package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.Livro;
import com.curso.biblioteca2026.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Representação completa de um livro")
public record LivroResponse(
        @Schema(description = "Identificador do livro", example = "1")
        Long id,

        @Schema(description = "Código ISBN único", example = "9788535902778")
        String isbn,

        @Schema(description = "Título do livro", example = "Dom Casmurro")
        String titulo,

        @Schema(description = "Quantidade atual de exemplares", example = "5")
        Integer quantidadeExemplares,

        @Schema(description = "Valor unitário de reposição", example = "50.00")
        BigDecimal valorReposicao,

        @Schema(description = "Valor total em estoque (quantidade × valor de reposição)", example = "250.00")
        BigDecimal valorTotal,

        @Schema(description = "Data de cadastro do livro", example = "2026-11-25")
        LocalDate dataCadastro,

        @Schema(description = "Status atual do livro", example = "ATIVO")
        Status status,

        @Schema(description = "Indica se o livro está ativo", example = "true")
        Boolean ativo,

        @Schema(description = "Identificador da categoria", example = "1")
        Long categoriaId,

        @Schema(description = "Nome da categoria", example = "Literatura")
        String categoriaNome
) {

    public static LivroResponse from(Livro livro) {
        return new LivroResponse(
                livro.getId(),
                livro.getIsbn(),
                livro.getTitulo(),
                livro.getQuantidadeExemplares(),
                livro.getValorReposicao(),
                livro.getValorTotal(),
                livro.getDataCadastro(),
                livro.getStatus(),
                livro.getAtivo(),
                livro.getCategoria().getId(),
                livro.getCategoria().getNome()
        );
    }
}
