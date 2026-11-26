package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.LoginRequest;
import com.curso.biblioteca2026.dto.RefreshTokenRequest;
import com.curso.biblioteca2026.dto.TokenResponse;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.service.AutenticacaoService;
import com.curso.biblioteca2026.service.SessaoAutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação", description = "Emissão, renovação com rotação e encerramento de tokens de sessão")
@RestController
@RequestMapping("/api/autenticacao")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final SessaoAutenticacaoService sessaoAutenticacaoService;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService,
            SessaoAutenticacaoService sessaoAutenticacaoService) {
        this.autenticacaoService = autenticacaoService;
        this.sessaoAutenticacaoService = sessaoAutenticacaoService;
    }

    @Operation(
            summary = "Autenticar e emitir par de tokens",
            description = "Valida login e senha da conta ativa e emite um Bearer Token JWT e um refresh token opaco de uso único."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens emitidos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Login ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return TokenResponse.from(
                autenticacaoService.autenticar(request.login(), request.senha(), httpRequest.getRemoteAddr())
        );
    }

    @Operation(
            summary = "Renovar par de tokens com rotação",
            description = "Consome o refresh token atual e emite um novo par (access + refresh) na mesma família de sessão."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return TokenResponse.from(sessaoAutenticacaoService.renovar(request.refreshToken()));
    }

    @Operation(
            summary = "Encerrar sessão atual (logout local)",
            description = "Revoga a família de sessão vinculada ao refresh token informado."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sessão encerrada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        sessaoAutenticacaoService.encerrar(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Encerrar todas as sessões da conta (logout global)",
            description = "Revoga todas as famílias de refresh tokens ativas da conta autenticada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as sessões encerradas"),
            @ApiResponse(responseCode = "401", description = "Token de acesso ausente ou inválido")
    })
    @PostMapping("/logout-todos")
    public ResponseEntity<Void> logoutTodos(Authentication authentication) {
        sessaoAutenticacaoService.encerrarTodas(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}