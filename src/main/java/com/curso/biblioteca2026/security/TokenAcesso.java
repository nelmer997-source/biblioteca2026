package com.curso.biblioteca2026.security;

import java.time.Instant;

public record TokenAcesso(
        String valor,
        Instant expiraEm,
        long duracaoSegundos) {
}