package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.CategoriaRequest;
import com.curso.biblioteca2026.dto.CategoriaResponse;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.dto.StatusRequest;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.service.CategoriaLivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Categorias de livros", description = "Cadastro, consulta, status e exclusão de categorias")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaLivroController {

    private final CategoriaLivroService service;

    public CategoriaLivroController(CategoriaLivroService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar categorias com filtros e paginação",
            description = "Retorna categorias filtradas por nome e status. Parâmetros de paginação: page (inicia em 0), size e sort."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<CategoriaResponse> listar(
            @Parameter(description = "Parte do nome da categoria (case-insensitive)", example = "literatura")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Status da categoria", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Configuração de paginação e ordenação (page, size, sort)")
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

        return service.listar(nome, status, pageable);
    }

    @Operation(summary = "Buscar categoria por ID", description = "Retorna os dados de uma categoria específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public CategoriaResponse buscar(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria ativa com nome único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome de categoria já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<CategoriaResponse> criar(
            @Valid @RequestBody CategoriaRequest request) {

        CategoriaResponse criada = service.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();

        return ResponseEntity.created(location).body(criada);
    }

    @Operation(summary = "Atualizar categoria", description = "Atualiza o nome de uma categoria existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Nome já utilizado por outra categoria",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public CategoriaResponse atualizar(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {

        return service.atualizar(id, request);
    }

    @Operation(summary = "Alterar status da categoria", description = "Altera o status entre ATIVO e INATIVO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public CategoriaResponse alterarStatus(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return service.alterarStatus(id, request);
    }

    @Operation(
            summary = "Excluir categoria",
            description = "Remove a categoria apenas quando não houver livros associados a ela."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Categoria possui livros vinculados",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador da categoria", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
