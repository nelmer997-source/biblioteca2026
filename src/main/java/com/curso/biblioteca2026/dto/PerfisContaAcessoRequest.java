package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.PerfilAcesso;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

@Schema(description = "Dados para substituição dos perfis de uma conta de acesso")
public record PerfisContaAcessoRequest(
        @Schema(description = "Novo conjunto de perfis da conta", example = "[\"OPERADOR\", \"ADMINISTRADOR\"]")
        @NotEmpty(message = "Informe ao menos um perfil")
        Set<PerfilAcesso> perfis
) {
}