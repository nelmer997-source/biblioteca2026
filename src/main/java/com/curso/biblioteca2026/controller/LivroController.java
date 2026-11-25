package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.LivroRequest;
import com.curso.biblioteca2026.dto.LivroResponse;
import com.curso.biblioteca2026.dto.MovimentacaoEstoqueRequest;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.dto.StatusRequest;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.service.LivroService;
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

@Tag(name = "Livros", description = "Cadastro, consulta, status e movimentação de exemplares")
@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @Operation(
            summary = "Listar livros com filtros e paginação",
            description = "Filtra livros por título, ISBN, categoria e status. Parâmetros de paginação: page (inicia em 0), size e sort."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<LivroResponse> listar(
            @Parameter(description = "Trecho do título do livro (case-insensitive)", example = "Dom Casmurro")
            @RequestParam(required = false) String titulo,
            @Parameter(description = "Código ISBN exato (case-insensitive)", example = "9788535902778")
            @RequestParam(required = false) String isbn,
            @Parameter(description = "Identificador da categoria", example = "1")
            @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "Status do livro", example = "ATIVO")
            @RequestParam(required = false) Status status,
            @Parameter(description = "Configuração de paginação e ordenação (page, size, sort)")
            @PageableDefault(size = 20, sort = "titulo") Pageable pageable) {

        return service.listar(titulo, isbn, categoriaId, status, pageable);
    }

    @Operation(summary = "Buscar livro por ID", description = "Retorna os detalhes de um livro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro encontrado"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public LivroResponse buscar(
            @Parameter(description = "Identificador do livro", example = "1")
            @PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(summary = "Criar livro", description = "Cadastra um novo livro vinculado a uma categoria existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Livro cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "ISBN já cadastrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<LivroResponse> criar(
            @Valid @RequestBody LivroRequest request) {

        LivroResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @Operation(summary = "Atualizar livro", description = "Atualiza dados cadastrais do livro e ajusta a quantidade de exemplares.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Livro atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Livro ou categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "ISBN já utilizado por outro livro",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public LivroResponse atualizar(
            @Parameter(description = "Identificador do livro", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody LivroRequest request) {

        return service.atualizar(id, request);
    }

    @Operation(summary = "Alterar status do livro", description = "Altera o status do livro entre ATIVO e INATIVO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Status inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    public LivroResponse alterarStatus(
            @Parameter(description = "Identificador do livro", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return service.alterarStatus(id, request);
    }

    @Operation(
            summary = "Registrar entrada de exemplares",
            description = "Bloqueia o livro e soma uma quantidade positiva ao saldo atual. Repetir a chamada repete o efeito."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/entradas")
    public LivroResponse registrarEntrada(
            @Parameter(description = "Identificador do livro", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {

        return service.registrarEntrada(id, request);
    }

    @Operation(
            summary = "Registrar saída de exemplares",
            description = "Bloqueia o livro e subtrai uma quantidade positiva sem permitir saldo negativo. Repetir a chamada repete o efeito."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saída registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade inválida ou saldo insuficiente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/estoque/saidas")
    public LivroResponse registrarSaida(
            @Parameter(description = "Identificador do livro", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request) {

        return service.registrarSaida(id, request);
    }

    @Operation(summary = "Excluir livro", description = "Remove um livro pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Livro excluído"),
            @ApiResponse(responseCode = "404", description = "Livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do livro", example = "1")
            @PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}