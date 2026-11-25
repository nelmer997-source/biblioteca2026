package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.AlterarSenhaRequest;
import com.curso.biblioteca2026.dto.ContaAcessoRequest;
import com.curso.biblioteca2026.dto.ContaAcessoResponse;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.mapper.ContaAcessoMapper;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.service.ContaAcessoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Contas de Acesso", description = "Cadastro de credenciais, consulta da conta autenticada e troca de senha")
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
            description = "Endpoint público provisório de bootstrap. Cria conta ativa com perfil OPERADOR e armazena apenas hash BCrypt."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta de acesso criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou pessoa inativa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
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