package com.curso.biblioteca2026.security;

import java.time.Instant;

public record TokensAutenticacao(
        TokenAcesso accessToken,
        String refreshToken,
        Instant refreshTokenExpiraEm) {
}