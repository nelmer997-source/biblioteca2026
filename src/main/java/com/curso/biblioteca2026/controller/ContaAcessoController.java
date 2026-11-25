package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.AlterarSenhaRequest;
import com.curso.biblioteca2026.dto.ContaAcessoRequest;
import com.curso.biblioteca2026.dto.ContaAcessoResponse;
import com.curso.biblioteca2026.dto.PerfisContaAcessoRequest;
import com.curso.biblioteca2026.dto.StatusContaAcessoRequest;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.mapper.ContaAcessoMapper;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.service.ContaAcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Contas de Acesso", description = "Cadastro, administração de perfis e status, consulta da conta autenticada e troca de senha")
@RestController
@RequestMapping("/api/contas-acesso")
public class ContaAcessoController {

    private final ContaAcessoService service;
    private final ContaAcessoMapper mapper;

    public ContaAcessoController(ContaAcessoService service, ContaAcessoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Cadastrar conta de acesso",
            description = "Cria conta ativa com perfil OPERADOR e armazena apenas hash BCrypt. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta de acesso criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou pessoa inativa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: requer perfil ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pessoa ou login já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<ContaAcessoResponse> cadastrar(@Valid @RequestBody ContaAcessoRequest request) {
        ContaAcesso conta = service.cadastrar(request.pessoaId(), request.login(), request.senha());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(conta.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(conta));
    }

    @Operation(
            summary = "Alterar perfis de uma conta de acesso",
            description = "Substitui os perfis da conta informada, protegendo o último administrador ativo. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfis atualizados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou violação de invariante administrativa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: requer perfil ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Conta de acesso não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/perfis")
    public ContaAcessoResponse alterarPerfis(
            @PathVariable Long id,
            @Valid @RequestBody PerfisContaAcessoRequest request,
            Authentication authentication) {
        ContaAcesso conta = service.alterarPerfis(id, request.perfis(), authentication.getName());
        return mapper.toResponse(conta);
    }

    @Operation(
            summary = "Ativar ou bloquear conta de acesso",
            description = "Altera o status da conta entre ATIVA e BLOQUEADA, protegendo a própria conta e o último administrador ativo. Requer perfil ADMINISTRADOR."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status da conta atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou violação de invariante administrativa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: requer perfil ADMINISTRADOR"),
            @ApiResponse(responseCode = "404", description = "Conta de acesso não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public ContaAcessoResponse alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusContaAcessoRequest request,
            Authentication authentication) {
        ContaAcesso conta = service.alterarStatus(id, request.status(), authentication.getName());
        return mapper.toResponse(conta);
    }

    @Operation(summary = "Consultar dados da conta autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados da conta autenticada"),
            @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas")
    })
    @GetMapping("/me")
    public ContaAcessoResponse me(Authentication authentication) {
        return mapper.toResponse(service.buscarPorLogin(authentication.getName()));
    }

    @Operation(summary = "Alterar senha da conta autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas")
    })
    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarSenha(
            Authentication authentication,
            @Valid @RequestBody AlterarSenhaRequest request) {
        service.alterarSenha(authentication.getName(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}