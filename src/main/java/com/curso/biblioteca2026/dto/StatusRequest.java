package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Requisição para alterar o status de um recurso")
public record StatusRequest(
        @Schema(description = "Novo status (ATIVO ou INATIVO)", example = "ATIVO")
        @NotNull(message = "Status é obrigatório")
        Status status
) {
}
