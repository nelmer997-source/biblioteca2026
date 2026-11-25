package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.StatusContaAcesso;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Representação de resposta de uma conta de acesso (sem credenciais)")
public record ContaAcessoResponse(
        @Schema(description = "Identificador da conta", example = "1")
        Long id,

        @Schema(description = "Identificador da pessoa vinculada", example = "1")
        Long pessoaId,

        @Schema(description = "Nome da pessoa vinculada", example = "Ana Souza")
        String nomePessoa,

        @Schema(description = "Login da conta", example = "ana.souza")
        String login,

        @Schema(description = "Status da conta de acesso", example = "ATIVA")
        StatusContaAcesso status,

        @Schema(description = "Perfis atribuídos à conta", example = "[\"OPERADOR\"]")
        Set<PerfilAcesso> perfis
) {
}