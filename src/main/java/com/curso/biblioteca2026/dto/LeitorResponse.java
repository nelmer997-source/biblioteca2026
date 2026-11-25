package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de resposta de um leitor")
public record LeitorResponse(
        @Schema(description = "Identificador do leitor", example = "1")
        Long id,

        @Schema(description = "Identificador da pessoa vinculada", example = "1")
        Long pessoaId,

        @Schema(description = "Nome do leitor", example = "Ana Souza")
        String nome,

        @Schema(description = "E-mail do leitor", example = "ana@example.com")
        String email,

        @Schema(description = "CPF do leitor", example = "52998224725")
        String cpf,

        @Schema(description = "Telefone de contato", example = "(17) 99999-0000")
        String telefone,

        @Schema(description = "Status da pessoa", example = "ATIVO")
        Status statusPessoa
) {
}