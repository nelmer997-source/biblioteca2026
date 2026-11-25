package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Dados para inclusão de livro em um empréstimo aberto")
public record ItemEmprestimoRequest(
        @Schema(description = "Identificador do livro", example = "1")
        @NotNull(message = "Livro é obrigatório")
        Long livroId,

        @Schema(description = "Quantidade de exemplares", example = "1")
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantidade
) {
}