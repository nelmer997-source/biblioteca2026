package com.curso.biblioteca2026.config;

import com.curso.biblioteca2026.service.AdministradorInicialService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.security.initial-admin.enabled",
        havingValue = "true")
public class AdministradorInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicialRunner.class);

    private final AdministradorInicialService service;
    private final AdministradorInicialProperties properties;

    public AdministradorInicialRunner(
            AdministradorInicialService service,
            AdministradorInicialProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        service.criarSeNecessario(properties).ifPresentOrElse(
                conta -> log.info(
                        "Conta administrativa inicial criada para o login '{}'. Desative INITIAL_ADMIN_ENABLED após a primeira execução.",
                        conta.getLogin()),
                () -> log.info("Conta administrativa ativa já existente; nenhuma nova conta inicial foi criada.")
        );
    }
}