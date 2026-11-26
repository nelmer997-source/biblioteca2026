package com.curso.biblioteca2026.exception;

public class RefreshTokenInvalidoException extends RuntimeException {

    public RefreshTokenInvalidoException() {
        super("Refresh token inválido ou expirado");
    }

    public RefreshTokenInvalidoException(String message) {
        super(message);
    }
}