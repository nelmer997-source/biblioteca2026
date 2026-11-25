package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.BibliotecarioAtualizacaoRequest;
import com.curso.biblioteca2026.dto.BibliotecarioRequest;
import com.curso.biblioteca2026.dto.BibliotecarioResponse;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.exception.ApiError;
import com.curso.biblioteca2026.mapper.BibliotecarioMapper;
import com.curso.biblioteca2026.model.Bibliotecario;
import com.curso.biblioteca2026.model.FuncaoBibliotecario;
import com.curso.biblioteca2026.service.BibliotecarioService;
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

@Tag(name = "Bibliotecários", description = "Gerenciamento do vínculo profissional de bibliotecários")
@RestController
@RequestMapping("/api/bibliotecarios")
public class BibliotecarioController {

    private final BibliotecarioService service;
    private final BibliotecarioMapper mapper;

    public BibliotecarioController(BibliotecarioService service, BibliotecarioMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @Operation(summary = "Listar bibliotecários com filtros e paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public PaginaResponse<BibliotecarioResponse> listar(
            @Parameter(description = "Trecho do nome") @RequestParam(required = false) String nome,
            @Parameter(description = "Matrícula exata") @RequestParam(required = false) String matricula,
            @Parameter(description = "Função exercida") @RequestParam(required = false) FuncaoBibliotecario funcao,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<BibliotecarioResponse> pagina = service.listar(nome, matricula, funcao, pageable)
                .map(mapper::toResponse);
        return PaginaResponse.from(pagina);
    }

    @Operation(summary = "Buscar bibliotecário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bibliotecário encontrado"),
            @ApiResponse(responseCode = "404", description = "Bibliotecário não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public BibliotecarioResponse buscar(@PathVariable Long id) {
        return mapper.toResponse(service.buscarPorId(id));
    }

    @Operation(summary = "Criar vínculo de bibliotecário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Bibliotecário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou pessoa inativa",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pessoa não encontrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Pessoa ou matrícula já cadastrada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<BibliotecarioResponse> cadastrar(@Valid @RequestBody BibliotecarioRequest request) {
        Bibliotecario bibliotecario = service.cadastrar(
                request.pessoaId(),
                request.matricula(),
                request.funcao(),
                request.dataAdmissao()
        );
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(bibliotecario.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(bibliotecario));
    }

    @Operation(summary = "Atualizar matrícula e função do bibliotecário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bibliotecário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Bibliotecário não encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Matrícula já utilizada",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public BibliotecarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody BibliotecarioAtualizacaoRequest request) {
        return mapper.toResponse(service.atualizar(id, request.matricula(), request.funcao()));
    }
}