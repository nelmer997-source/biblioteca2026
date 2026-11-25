package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.StatusContaAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para alteração do status de uma conta de acesso")
public record StatusContaAcessoRequest(
        @Schema(description = "Novo status da conta (ATIVA ou BLOQUEADA)", example = "BLOQUEADA")
        @NotNull(message = "Status é obrigatório")
        StatusContaAcesso status
) {
}