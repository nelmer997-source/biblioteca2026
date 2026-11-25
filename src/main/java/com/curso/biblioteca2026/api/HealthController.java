package com.curso.biblioteca2026.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Infraestrutura", description = "Verificações técnicas da aplicação")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "Verificar saúde da API", description = "Retorna OK quando a aplicação está ativa.")
    @ApiResponse(responseCode = "200", description = "Aplicação ativa")
    @GetMapping
    public String health() {
        return "OK";
    }
}
