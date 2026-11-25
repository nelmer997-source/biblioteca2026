package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de conta de acesso vinculada a uma pessoa")
public record ContaAcessoRequest(
        @Schema(description = "Identificador da pessoa", example = "1")
        @NotNull(message = "Pessoa é obrigatória")
        Long pessoaId,

        @Schema(description = "Login único (letras, números, ponto, hífen ou sublinhado)", example = "ana.souza")
        @NotBlank(message = "Login é obrigatório")
        @Size(min = 3, max = 80, message = "Login deve possuir entre 3 e 80 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Login deve conter apenas letras, números, ponto, hífen ou sublinhado")
        String login,

        @Schema(description = "Senha com no mínimo 12 caracteres e até 72 bytes UTF-8", example = "uma-frase-senha-2026")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 12, max = 72, message = "Senha deve possuir entre 12 e 72 caracteres")
        String senha
) {
}