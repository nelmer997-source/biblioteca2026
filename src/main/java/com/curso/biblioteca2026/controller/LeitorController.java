package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.LeitorAtualizacaoRequest;
import com.curso.biblioteca2026.dto.LeitorRequest;
import com.curso.biblioteca2026.dto.LeitorResponse;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.mapper.LeitorMapper;
import com.curso.biblioteca2026.model.Leitor;
import com.curso.biblioteca2026.service.LeitorService;
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

@Tag(name = "Leitores", description = "Gerenciamento do papel de leitor associado a uma pessoa")
@RestController
@RequestMapping("/api/leitores")
public class LeitorController {

    private final LeitorService service;
    private final LeitorMapper mapper;

    public LeitorController(LeitorService service, LeitorMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Listar leitores com filtros e paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<LeitorResponse> listar(
            @Parameter(description = "Trecho do nome da pessoa") @RequestParam(required = false) String nome,
            @Parameter(description = "CPF exato") @RequestParam(required = false) String cpf,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<LeitorResponse> pagina = service.listar(nome, cpf, pageable)
                .map(mapper::toResponse);
        return PaginaResponse.from(pagina);
    }

    @Operation(summary = "Buscar leitor por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitor encontrado"),
            @ApiResponse(responseCode = "404", description = "Leitor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public LeitorResponse buscar(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Associar papel de leitor a uma pessoa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Leitor cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou pessoa inativa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pessoa já cadastrada como leitor",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<LeitorResponse> cadastrar(@Valid @RequestBody LeitorRequest request) {
        Leitor leitor = service.cadastrar(request.pessoaId(), request.telefone());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(leitor.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(leitor));
    }

    @Operation(summary = "Atualizar dados do leitor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leitor atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leitor não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public LeitorResponse atualizar(@PathVariable Long id, @Valid @RequestBody LeitorAtualizacaoRequest request) {
        return mapper.toResponse(service.atualizar(id, request.telefone()));
    }
}