package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização cadastral do leitor")
public record LeitorAtualizacaoRequest(
        @Schema(description = "Telefone de contato", example = "(17) 99999-0000")
        @Size(max = 30, message = "Telefone deve possuir no máximo 30 caracteres")
        String telefone
) {
}