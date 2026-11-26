package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.EventoSegurancaResponse;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.model.TipoEventoSeguranca;
import com.curso.biblioteca2026.service.AuditoriaSegurancaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auditoria de Segurança", description = "Consulta administrativa da trilha de eventos de segurança")
@RestController
@RequestMapping("/api/auditoria-seguranca")
public class AuditoriaSegurancaController {

    private final AuditoriaSegurancaService service;

    public AuditoriaSegurancaController(AuditoriaSegurancaService service) {
        this.service = service;
    }

    @Operation(
            summary = "Consultar eventos de segurança",
            description = "Retorna eventos de segurança paginados em ordem cronológica decrescente. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: requer perfil ADMINISTRADOR")
    })
    @GetMapping
    public PaginaResponse<EventoSegurancaResponse> consultar(
            @Parameter(description = "Tipo do evento de segurança")
            @RequestParam(required = false) TipoEventoSeguranca tipo,
            @Parameter(description = "Configuração de paginação e ordenação (page, size, sort)")
            @PageableDefault(size = 20, sort = "ocorridoEm", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EventoSegurancaResponse> pagina = service.consultar(tipo, pageable)
                .map(EventoSegurancaResponse::from);
        return PaginaResponse.from(pagina);
    }
}