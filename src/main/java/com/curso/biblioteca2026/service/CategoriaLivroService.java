package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.dto.CategoriaRequest;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.repository.CategoriaLivroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaLivroService {

    private final CategoriaLivroRepository repository;

    public CategoriaLivroService(CategoriaLivroRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaLivro> listar() {
        return repository.findAll();
    }

    public CategoriaLivro buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria não encontrada: " + id
                        ));
    }

    public CategoriaLivro criar(CategoriaRequest request) {
        repository.findByNomeIgnoreCase(request.nome())
                .ifPresent(categoria -> {
                    throw new IllegalArgumentException(
                            "Já existe uma categoria com esse nome"
                    );
                });

        CategoriaLivro categoria = new CategoriaLivro(request.nome());

        return repository.save(categoria);
    }

    public CategoriaLivro atualizar(Long id, CategoriaRequest request) {
        CategoriaLivro categoria = buscar(id);

        repository.findByNomeIgnoreCase(request.nome())
                .ifPresent(outra -> {
                    if (!outra.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Já existe uma categoria com esse nome"
                        );
                    }
                });

        categoria.setNome(request.nome());

        return repository.save(categoria);
    }

    public void excluir(Long id) {
        CategoriaLivro categoria = buscar(id);
        repository.delete(categoria);
    }
}