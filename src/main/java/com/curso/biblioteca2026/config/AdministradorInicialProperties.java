package com.curso.biblioteca2026.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.initial-admin")
public record AdministradorInicialProperties(
        boolean enabled,
        String nome,
        String email,
        String cpf,
        String login,
        String senha
) {
}