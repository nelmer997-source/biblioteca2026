package com.curso.biblioteca2026.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Representação padronizada de erro da API")
public record ApiError(
        @Schema(description = "Data e hora do erro em UTC", example = "2026-11-25T16:30:00Z")
        Instant timestamp,

        @Schema(description = "Código HTTP numérico", example = "400")
        int status,

        @Schema(description = "Nome padronizado do status HTTP", example = "Bad Request")
        String error,

        @Schema(description = "Mensagem descritiva do erro", example = "Dados inválidos")
        String message,

        @Schema(description = "Caminho da requisição", example = "/api/livros")
        String path,

        @Schema(description = "Detalhes de validação por campo, quando aplicável")
        Map<String, String> fields
) {
}
