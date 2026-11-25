package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteração da senha da conta autenticada")
public record AlterarSenhaRequest(
        @Schema(description = "Senha atual", example = "uma-frase-senha-2026")
        @NotBlank(message = "Senha atual é obrigatória")
        String senhaAtual,

        @Schema(description = "Nova senha com no mínimo 12 caracteres e até 72 bytes UTF-8", example = "outra-frase-senha-2026")
        @NotBlank(message = "Nova senha é obrigatória")
        @Size(min = 12, max = 72, message = "Nova senha deve possuir entre 12 e 72 caracteres")
        String novaSenha
) {
}