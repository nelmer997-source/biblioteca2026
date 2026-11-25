package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.FuncaoBibliotecario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualizar matrícula e função do bibliotecário")
public record BibliotecarioAtualizacaoRequest(
        @Schema(description = "Matrícula funcional única", example = "BIB-001")
        @NotBlank(message = "Matrícula é obrigatória")
        @Size(max = 30, message = "Matrícula deve possuir no máximo 30 caracteres")
        String matricula,

        @Schema(description = "Função exercida na biblioteca", example = "COORDENADOR")
        @NotNull(message = "Função é obrigatória")
        FuncaoBibliotecario funcao
) {
}