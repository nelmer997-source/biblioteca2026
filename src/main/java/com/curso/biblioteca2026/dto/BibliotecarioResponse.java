package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.FuncaoBibliotecario;
import com.curso.biblioteca2026.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Representação de resposta de um bibliotecário")
public record BibliotecarioResponse(
        @Schema(description = "Identificador do bibliotecário", example = "1")
        Long id,

        @Schema(description = "Identificador da pessoa vinculada", example = "2")
        Long pessoaId,

        @Schema(description = "Nome do bibliotecário", example = "Bruno Lima")
        String nome,

        @Schema(description = "CPF do bibliotecário", example = "11144477735")
        String cpf,

        @Schema(description = "Matrícula funcional", example = "BIB-001")
        String matricula,

        @Schema(description = "Função exercida", example = "ATENDENTE")
        FuncaoBibliotecario funcao,

        @Schema(description = "Data de admissão", example = "2026-11-25")
        LocalDate dataAdmissao,

        @Schema(description = "Status da pessoa", example = "ATIVO")
        Status statusPessoa
) {
}