package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.LoginRequest;
import com.curso.biblioteca2026.dto.TokenResponse;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.security.TokenAcesso;
import com.curso.biblioteca2026.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão de JSON Web Token (JWT) a partir de credenciais válidas")
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @Operation(
            summary = "Autenticar e emitir JWT",
            description = "Valida login e senha da conta ativa e emite um Bearer Token JWT válido por 30 minutos."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token emitido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Login ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        TokenAcesso token = service.autenticar(request.login(), request.senha());
        return new TokenResponse(
                token.valor(),
                "Bearer",
                token.duracaoSegundos(),
                token.expiraEm());
    }
}