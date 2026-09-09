package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.dto.LivroRequest;
import com.curso.biblioteca2026.dto.LivroResponse;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.model.Livro;
import com.curso.biblioteca2026.repository.CategoriaLivroRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final CategoriaLivroRepository categoriaRepository;

    public LivroService(
            LivroRepository livroRepository,
            CategoriaLivroRepository categoriaRepository) {
        this.livroRepository = livroRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<LivroResponse> listar() {
        return livroRepository.findAll()
                .stream()
                .map(LivroResponse::from)
                .toList();
    }

    public LivroResponse buscar(String isbn) {
        Livro livro = livroRepository.findById(isbn)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Livro não encontrado: " + isbn
                        ));

        return LivroResponse.from(livro);
    }

    public LivroResponse criar(LivroRequest request) {
        if (livroRepository.existsById(request.isbn())) {
            throw new IllegalArgumentException(
                    "Já existe um livro com esse ISBN"
            );
        }

        CategoriaLivro categoria = buscarCategoria(request.categoriaId());

        Livro livro = new Livro(
                request.isbn(),
                request.titulo(),
                request.quantidadeExemplares(),
                request.valorReposicao(),
                LocalDate.now(),
                request.ativo(),
                categoria
        );

        return LivroResponse.from(livroRepository.save(livro));
    }

    public LivroResponse atualizar(String isbn, LivroRequest request) {
        Livro livro = livroRepository.findById(isbn)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Livro não encontrado: " + isbn
                        ));

        CategoriaLivro categoria = buscarCategoria(request.categoriaId());

        livro.setTitulo(request.titulo());
        livro.setQuantidadeExemplares(request.quantidadeExemplares());
        livro.setValorReposicao(request.valorReposicao());
        livro.setAtivo(request.ativo());
        livro.setCategoria(categoria);

        return LivroResponse.from(livroRepository.save(livro));
    }

    public void excluir(String isbn) {
        Livro livro = livroRepository.findById(isbn)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Livro não encontrado: " + isbn
                        ));

        livroRepository.delete(livro);
    }

    private CategoriaLivro buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria não encontrada: " + categoriaId
                        ));
    }
}