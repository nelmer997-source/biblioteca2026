package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.dto.PessoaRequest;
import com.curso.biblioteca2026.dto.PessoaResponse;
import com.curso.biblioteca2026.dto.StatusRequest;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.mapper.PessoaMapper;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Pessoas", description = "Cadastro e consulta da identidade civil de pessoas")
@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    private final PessoaService service;
    private final PessoaMapper mapper;

    public PessoaController(PessoaService service, PessoaMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Listar pessoas com filtros e paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<PessoaResponse> listar(
            @Parameter(description = "Trecho do nome") @RequestParam(required = false) String nome,
            @Parameter(description = "CPF exato com 11 dígitos") @RequestParam(required = false) String cpf,
            @Parameter(description = "Status da pessoa") @RequestParam(required = false) Status status,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

        Page<PessoaResponse> pagina = service.listar(nome, cpf, status, pageable)
                .map(mapper::toResponse);
        return PaginaResponse.from(pagina);
    }

    @Operation(summary = "Buscar pessoa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa encontrada"),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public PessoaResponse buscar(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Cadastrar pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pessoa cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados ou CPF inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<PessoaResponse> cadastrar(@Valid @RequestBody PessoaRequest request) {
        Pessoa pessoa = service.cadastrar(request.nome(), request.email(), request.cpf());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pessoa.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(pessoa));
    }

    @Operation(summary = "Atualizar dados da pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pessoa atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "CPF ou e-mail duplicado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public PessoaResponse atualizar(@PathVariable Long id, @Valid @RequestBody PessoaRequest request) {
        return mapper.toResponse(service.atualizar(id, request.nome(), request.email(), request.cpf()));
    }

    @Operation(summary = "Alterar status da pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public PessoaResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return mapper.toResponse(service.alterarStatus(id, request.status()));
    }
}