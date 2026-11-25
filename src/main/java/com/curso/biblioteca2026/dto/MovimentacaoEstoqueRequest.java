package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Requisição para entrada ou saída de exemplares")
public record MovimentacaoEstoqueRequest(
        @Schema(description = "Quantidade positiva de exemplares a movimentar", example = "3")
        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantidade
) {
}
