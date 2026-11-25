package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais para autenticação e emissão de JWT")
public record LoginRequest(
        @Schema(description = "Login da conta de acesso", example = "ana.souza")
        @NotBlank(message = "Login é obrigatório")
        @Size(max = 80, message = "Login deve possuir no máximo 80 caracteres")
        String login,

        @Schema(description = "Senha da conta de acesso", example = "uma-frase-senha-2026")
        @NotBlank(message = "Senha é obrigatória")
        @Size(max = 72, message = "Senha deve possuir no máximo 72 caracteres")
        String senha
) {
}