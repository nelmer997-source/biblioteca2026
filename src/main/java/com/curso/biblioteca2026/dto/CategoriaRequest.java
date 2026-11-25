package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criar ou atualizar uma categoria de livro")
public record CategoriaRequest(
        @Schema(description = "Nome único da categoria", example = "Literatura")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve possuir no máximo 100 caracteres")
        String nome
) {
}
