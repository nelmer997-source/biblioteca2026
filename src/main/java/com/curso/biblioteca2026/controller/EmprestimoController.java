package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.*;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.mapper.EmprestimoMapper;
import com.curso.biblioteca2026.model.Emprestimo;
import com.curso.biblioteca2026.model.StatusEmprestimo;
import com.curso.biblioteca2026.service.EmprestimoService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Empréstimos", description = "Abertura, gestão de itens, finalização atômica e cancelamento de empréstimos")
@RestController
@RequestMapping("/api/emprestimos")
public class EmprestimoController {

    private final EmprestimoService service;
    private final EmprestimoMapper mapper;

    public EmprestimoController(EmprestimoService service, EmprestimoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Listar resumos de empréstimos com filtros e paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtros ou ordenação inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<EmprestimoResumoResponse> listar(
            @Parameter(description = "Status do empréstimo") @RequestParam(required = false) StatusEmprestimo status,
            @Parameter(description = "Identificador do leitor") @RequestParam(required = false) Long leitorId,
            @Parameter(description = "Data inicial (AAAA-MM-DD)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @Parameter(description = "Data final (AAAA-MM-DD)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @PageableDefault(size = 20, sort = "dataEmprestimo") Pageable pageable) {

        Page<EmprestimoResumoResponse> pagina = service.listar(status, leitorId, dataInicial, dataFinal, pageable)
                .map(mapper::toResumo);
        return PaginaResponse.from(pagina);
    }

    @Operation(summary = "Buscar empréstimo completo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo encontrado"),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public EmprestimoResponse buscar(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Abrir novo empréstimo")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empréstimo aberto"),
            @ApiResponse(responseCode = "400", description = "Participantes inválidos ou inativos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Leitor ou bibliotecário não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<EmprestimoResponse> cadastrar(@Valid @RequestBody EmprestimoRequest request) {
        Emprestimo emprestimo = service.cadastrar(request.leitorId(), request.bibliotecarioId());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(emprestimo.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(emprestimo));
    }

    @Operation(summary = "Adicionar livro ao empréstimo aberto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item adicionado"),
            @ApiResponse(responseCode = "400", description = "Empréstimo fechado, livro inativo ou duplicado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo ou livro não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/itens")
    public EmprestimoResponse adicionarItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemEmprestimoRequest request) {
        return mapper.toResponse(service.adicionarItem(id, request.livroId(), request.quantidade()));
    }

    @Operation(summary = "Remover item de empréstimo aberto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removido"),
            @ApiResponse(responseCode = "400", description = "Empréstimo fechado ou item inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{emprestimoId}/itens/{itemId}")
    public EmprestimoResponse removerItem(@PathVariable Long emprestimoId, @PathVariable Long itemId) {
        return mapper.toResponse(service.removerItem(emprestimoId, itemId));
    }

    @Operation(
            summary = "Finalizar empréstimo",
            description = "Valida todo o empréstimo, bloqueia os livros em ordem determinística e baixa os exemplares na mesma transação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo finalizado"),
            @ApiResponse(responseCode = "400", description = "Empréstimo vazio, fechado ou exemplares insuficientes",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/finalizacao")
    public EmprestimoResponse finalizar(@PathVariable Long id) {
        return mapper.toResponse(service.finalizar(id));
    }

    @Operation(summary = "Cancelar empréstimo aberto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo cancelado"),
            @ApiResponse(responseCode = "400", description = "Empréstimo não está aberto",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/cancelamento")
    public EmprestimoResponse cancelar(@PathVariable Long id) {
        return mapper.toResponse(service.cancelar(id));
    }
}