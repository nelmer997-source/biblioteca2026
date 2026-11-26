package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Requisição contendo o refresh token opaco da sessão")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token opaco recebido no login ou na última renovação", example = "dGhpc2lzYXNhbXBsZXJlZnJlc2h0b2tlbjIwMjY")
        @NotBlank(message = "Refresh token é obrigatório")
        @Size(max = 256, message = "Refresh token inválido")
        String refreshToken
) {
}