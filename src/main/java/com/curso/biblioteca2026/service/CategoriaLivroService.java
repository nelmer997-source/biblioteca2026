package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.dto.CategoriaRequest;
import com.curso.biblioteca2026.dto.CategoriaResponse;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.dto.StatusRequest;
import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoEmUsoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.repository.CategoriaLivroRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoriaLivroService {

    private final CategoriaLivroRepository repository;
    private final LivroRepository livroRepository;

    public CategoriaLivroService(
            CategoriaLivroRepository repository,
            LivroRepository livroRepository) {
        this.repository = repository;
        this.livroRepository = livroRepository;
    }

    public PaginaResponse<CategoriaResponse> listar(String nome, Status status, Pageable pageable) {
        Specification<CategoriaLivro> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            String termo = "%" + nome.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), termo));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        Page<CategoriaResponse> pagina = repository.findAll(spec, pageable)
                .map(CategoriaResponse::from);

        return PaginaResponse.from(pagina);
    }

    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.from(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nomeLimpo = request.nome().trim();
        repository.findByNomeIgnoreCase(nomeLimpo)
                .ifPresent(categoria -> {
                    throw new RecursoDuplicadoException(
                            "Já existe uma categoria com esse nome"
                    );
                });

        CategoriaLivro categoria = new CategoriaLivro(nomeLimpo);
        return CategoriaResponse.from(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        CategoriaLivro categoria = buscarEntidade(id);
        String nomeLimpo = request.nome().trim();

        repository.findByNomeIgnoreCase(nomeLimpo)
                .ifPresent(outra -> {
                    if (!outra.getId().equals(id)) {
                        throw new RecursoDuplicadoException(
                                "Já existe uma categoria com esse nome"
                        );
                    }
                });

        categoria.alterarNome(nomeLimpo);
        return CategoriaResponse.from(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse alterarStatus(Long id, StatusRequest request) {
        CategoriaLivro categoria = buscarEntidade(id);
        categoria.alterarStatus(request.status());
        return CategoriaResponse.from(repository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        CategoriaLivro categoria = buscarEntidade(id);

        if (livroRepository.existsByCategoriaId(id)) {
            throw new RecursoEmUsoException(
                    "Não é possível excluir categoria vinculada a livros"
            );
        }

        repository.delete(categoria);
    }

    public CategoriaLivro buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Categoria não encontrada: " + id
                        ));
    }
}
