package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.FuncaoBibliotecario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Dados para criar vínculo profissional de bibliotecário")
public record BibliotecarioRequest(
        @Schema(description = "Identificador da pessoa", example = "2")
        @NotNull(message = "Pessoa é obrigatória")
        Long pessoaId,

        @Schema(description = "Matrícula funcional única", example = "BIB-001")
        @NotBlank(message = "Matrícula é obrigatória")
        @Size(max = 30, message = "Matrícula deve possuir no máximo 30 caracteres")
        String matricula,

        @Schema(description = "Função exercida na biblioteca", example = "ATENDENTE")
        @NotNull(message = "Função é obrigatória")
        FuncaoBibliotecario funcao,

        @Schema(description = "Data de admissão", example = "2026-11-25")
        @NotNull(message = "Data de admissão é obrigatória")
        LocalDate dataAdmissao
) {
}