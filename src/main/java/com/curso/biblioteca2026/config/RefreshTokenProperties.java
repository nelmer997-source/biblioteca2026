package com.curso.biblioteca2026.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security.refresh-token")
public record RefreshTokenProperties(
        Duration idleTimeout,
        Duration absoluteExpiration) {

    public RefreshTokenProperties {
        if (idleTimeout == null || idleTimeout.isNegative() || idleTimeout.isZero()) {
            throw new IllegalArgumentException("Idle timeout deve ser positivo");
        }
        if (absoluteExpiration == null || absoluteExpiration.compareTo(idleTimeout) <= 0) {
            throw new IllegalArgumentException("Expiração absoluta deve ser maior que o idle timeout");
        }
    }
}