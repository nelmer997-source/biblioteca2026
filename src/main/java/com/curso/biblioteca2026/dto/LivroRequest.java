package com.curso.biblioteca2026.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LivroRequest(
        @NotBlank
        @Size(max = 20)
        String isbn,

        @NotBlank
        @Size(max = 200)
        String titulo,

        @NotNull
        @PositiveOrZero
        Integer quantidadeExemplares,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal valorReposicao,

        @NotNull
        Boolean ativo,

        @NotNull
        Long categoriaId
) {
}