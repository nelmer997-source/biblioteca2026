package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Dados para cadastrar ou atualizar um livro")
public record LivroRequest(
        @Schema(description = "Código ISBN único do livro", example = "9788535902778")
        @NotBlank(message = "ISBN é obrigatório")
        @Size(max = 20, message = "ISBN deve possuir no máximo 20 caracteres")
        String isbn,

        @Schema(description = "Título do livro", example = "Dom Casmurro")
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 200, message = "Título deve possuir no máximo 200 caracteres")
        String titulo,

        @Schema(description = "Quantidade de exemplares em estoque", example = "5")
        @NotNull(message = "Quantidade de exemplares é obrigatória")
        @PositiveOrZero(message = "Quantidade de exemplares não pode ser negativa")
        Integer quantidadeExemplares,

        @Schema(description = "Valor unitário de reposição", example = "50.00")
        @NotNull(message = "Valor de reposição é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor de reposição não pode ser negativo")
        BigDecimal valorReposicao,

        @Schema(description = "Indica se o livro está ativo", example = "true")
        @NotNull(message = "Indicador de ativo é obrigatório")
        Boolean ativo,

        @Schema(description = "Identificador da categoria vinculada", example = "1")
        @NotNull(message = "Categoria é obrigatória")
        Long categoriaId
) {
}
