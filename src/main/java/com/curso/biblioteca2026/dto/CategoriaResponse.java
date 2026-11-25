package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de resposta de uma categoria de livro")
public record CategoriaResponse(
        @Schema(description = "Identificador da categoria", example = "1")
        Long id,

        @Schema(description = "Nome da categoria", example = "Literatura")
        String nome,

        @Schema(description = "Status atual da categoria", example = "ATIVO")
        Status status
) {

    public static CategoriaResponse from(CategoriaLivro categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getStatus()
        );
    }
}
