package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representação de resposta de uma pessoa")
public record PessoaResponse(
        @Schema(description = "Identificador da pessoa", example = "1")
        Long id,

        @Schema(description = "Nome completo", example = "Ana Souza")
        String nome,

        @Schema(description = "E-mail normalizado", example = "ana@example.com")
        String email,

        @Schema(description = "CPF válido", example = "52998224725")
        String cpf,

        @Schema(description = "Data de cadastro", example = "2026-11-25")
        LocalDate dataCadastro,

        @Schema(description = "Status atual", example = "ATIVO")
        Status status
) {
}