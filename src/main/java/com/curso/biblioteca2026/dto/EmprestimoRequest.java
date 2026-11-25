package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para abertura de empréstimo")
public record EmprestimoRequest(
        @Schema(description = "Identificador do leitor", example = "1")
        @NotNull(message = "Leitor é obrigatório")
        Long leitorId,

        @Schema(description = "Identificador do bibliotecário responsável", example = "1")
        @NotNull(message = "Bibliotecário é obrigatório")
        Long bibliotecarioId
) {
}