package com.curso.biblioteca2026.controller;

import com.curso.biblioteca2026.dto.LivroRequest;
import com.curso.biblioteca2026.dto.LivroResponse;
import com.curso.biblioteca2026.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @GetMapping
    public List<LivroResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{isbn}")
    public LivroResponse buscar(@PathVariable String isbn) {
        return service.buscar(isbn);
    }

    @PostMapping
    public ResponseEntity<LivroResponse> criar(
            @Valid @RequestBody LivroRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.criar(request));
    }

    @PutMapping("/{isbn}")
    public LivroResponse atualizar(
            @PathVariable String isbn,
            @Valid @RequestBody LivroRequest request) {

        return service.atualizar(isbn, request);
    }

    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String isbn) {
        service.excluir(isbn);
    }
}