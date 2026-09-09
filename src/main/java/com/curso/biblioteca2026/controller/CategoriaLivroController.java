package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.CategoriaRequest;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.service.CategoriaLivroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaLivroController {

    private final CategoriaLivroService service;

    public CategoriaLivroController(CategoriaLivroService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaLivro> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaLivro buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaLivro> criar(
            @Valid @RequestBody CategoriaRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.criar(request));
    }

    @PutMapping("/{id}")
    public CategoriaLivro atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {

        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}