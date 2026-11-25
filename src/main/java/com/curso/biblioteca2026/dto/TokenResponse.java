package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resposta contendo o token de acesso JWT")
public record TokenResponse(
        @Schema(description = "Token JWT assinado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Esquema de autenticação", example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de validade em segundos", example = "1800")
        long expiresIn,

        @Schema(description = "Instante de expiração em UTC", example = "2026-11-25T20:30:00Z")
        Instant expiresAt
) {
}