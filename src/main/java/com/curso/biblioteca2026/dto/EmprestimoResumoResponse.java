package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.StatusEmprestimo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resumo paginado de empréstimo sem carregamento de itens")
public record EmprestimoResumoResponse(
        @Schema(description = "Identificador do empréstimo", example = "1")
        Long id,

        @Schema(description = "Identificador do leitor", example = "1")
        Long leitorId,

        @Schema(description = "Nome do leitor", example = "Ana Souza")
        String leitorNome,

        @Schema(description = "Identificador do bibliotecário", example = "1")
        Long bibliotecarioId,

        @Schema(description = "Nome do bibliotecário", example = "Bruno Lima")
        String bibliotecarioNome,

        @Schema(description = "Data e hora de abertura", example = "2026-11-25T16:30:00")
        LocalDateTime dataEmprestimo,

        @Schema(description = "Status atual do empréstimo", example = "ABERTO")
        StatusEmprestimo status
) {
}