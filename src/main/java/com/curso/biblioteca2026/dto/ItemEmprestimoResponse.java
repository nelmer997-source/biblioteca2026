package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representação de um item de empréstimo com valor histórico")
public record ItemEmprestimoResponse(
        @Schema(description = "Identificador do item", example = "1")
        Long id,

        @Schema(description = "Identificador do livro", example = "1")
        Long livroId,

        @Schema(description = "ISBN do livro", example = "9788535902778")
        String livroIsbn,

        @Schema(description = "Título do livro", example = "Dom Casmurro")
        String livroTitulo,

        @Schema(description = "Quantidade emprestada", example = "1")
        Integer quantidade,

        @Schema(description = "Valor de reposição registrado no momento da inclusão", example = "50.00")
        BigDecimal valorReposicaoUnitario,

        @Schema(description = "Subtotal de reposição do item", example = "50.00")
        BigDecimal subtotalReposicao
) {
}