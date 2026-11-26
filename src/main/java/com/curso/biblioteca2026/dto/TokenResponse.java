package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.security.TokensAutenticacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Resposta contendo o par de tokens de acesso e renovação")
public record TokenResponse(
        @Schema(description = "Token JWT assinado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Esquema de autenticação", example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de validade do access token em segundos", example = "1800")
        long expiresIn,

        @Schema(description = "Instante de expiração do access token em UTC", example = "2026-11-25T20:30:00Z")
        Instant expiresAt,

        @Schema(description = "Refresh token opaco de uso único para renovação da sessão", example = "4f8d9a2b7c1e6f3a0b5d8c2e7a1f4b9c")
        String refreshToken,

        @Schema(description = "Instante de expiração absoluta da família de sessão em UTC", example = "2026-11-26T04:00:00Z")
        Instant refreshTokenExpiresAt
) {

    public TokenResponse(String accessToken, String tokenType, long expiresIn, Instant expiresAt) {
        this(accessToken, tokenType, expiresIn, expiresAt, null, null);
    }

    public static TokenResponse from(TokensAutenticacao tokens) {
        return new TokenResponse(
                tokens.accessToken().valor(),
                "Bearer",
                tokens.accessToken().duracaoSegundos(),
                tokens.accessToken().expiraEm(),
                tokens.refreshToken(),
                tokens.refreshTokenExpiraEm()
        );
    }
}