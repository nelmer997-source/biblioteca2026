package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.dto.LivroRequest;
import com.curso.biblioteca2026.dto.LivroResponse;
import com.curso.biblioteca2026.dto.MovimentacaoEstoqueRequest;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.dto.StatusRequest;
import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoEmUsoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.model.Livro;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.repository.ItemEmprestimoRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class LivroService {

    private final LivroRepository livroRepository;
    private final CategoriaLivroService categoriaLivroService;
    private final ItemEmprestimoRepository itemEmprestimoRepository;
    private final EntityManager entityManager;

    public LivroService(
            LivroRepository livroRepository,
            CategoriaLivroService categoriaLivroService,
            ItemEmprestimoRepository itemEmprestimoRepository,
            EntityManager entityManager) {
        this.livroRepository = livroRepository;
        this.categoriaLivroService = categoriaLivroService;
        this.itemEmprestimoRepository = itemEmprestimoRepository;
        this.entityManager = entityManager;
    }

    public PaginaResponse<LivroResponse> listar(
            String titulo,
            String isbn,
            Long categoriaId,
            Status status,
            Pageable pageable) {

        Specification<Livro> spec = (root, query, cb) -> cb.conjunction();

        if (titulo != null && !titulo.isBlank()) {
            String termo = "%" + titulo.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("titulo")), termo));
        }

        if (isbn != null && !isbn.isBlank()) {
            String isbnLimpo = isbn.trim().toLowerCase();
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("isbn")), isbnLimpo));
        }

        if (categoriaId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria").get("id"), categoriaId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        Page<LivroResponse> pagina = livroRepository.findAll(spec, pageable)
                .map(LivroResponse::from);

        return PaginaResponse.from(pagina);
    }

    public LivroResponse buscar(Long id) {
        return LivroResponse.from(buscarEntidade(id));
    }

    @Transactional
    public LivroResponse criar(LivroRequest request) {
        String isbnLimpo = request.isbn().trim();
        if (livroRepository.existsByIsbnIgnoreCase(isbnLimpo)) {
            throw new RecursoDuplicadoException("Já existe um livro com esse ISBN");
        }

        CategoriaLivro categoria = categoriaLivroService.buscarEntidade(request.categoriaId());

        Livro livro = new Livro(
                isbnLimpo,
                request.titulo(),
                request.quantidadeExemplares(),
                request.valorReposicao(),
                LocalDate.now(),
                request.ativo(),
                categoria
        );

        return LivroResponse.from(livroRepository.save(livro));
    }

    @Transactional
    public LivroResponse atualizar(Long id, LivroRequest request) {
        Livro livro = buscarEntidade(id);
        String isbnLimpo = request.isbn().trim();

        livroRepository.findByIsbnIgnoreCase(isbnLimpo)
                .ifPresent(outro -> {
                    if (!outro.getId().equals(id)) {
                        throw new RecursoDuplicadoException("Já existe um livro com esse ISBN");
                    }
                });

        CategoriaLivro categoria = categoriaLivroService.buscarEntidade(request.categoriaId());

        livro.alterarTitulo(request.titulo());
        livro.alterarValorReposicao(request.valorReposicao());
        livro.alterarCategoria(categoria);
        livro.alterarStatus(Boolean.FALSE.equals(request.ativo()) ? Status.INATIVO : Status.ATIVO);

        int diferenca = request.quantidadeExemplares() - livro.getQuantidadeExemplares();
        if (diferenca != 0) {
            entityManager.refresh(livro, LockModeType.PESSIMISTIC_WRITE);
            int diferencaAtualizada = request.quantidadeExemplares() - livro.getQuantidadeExemplares();
            if (diferencaAtualizada > 0) {
                livro.receberExemplares(diferencaAtualizada);
            } else if (diferencaAtualizada < 0) {
                livro.retirarExemplares(Math.abs(diferencaAtualizada));
            }
        }

        return LivroResponse.from(livroRepository.save(livro));
    }

    @Transactional
    public LivroResponse alterarStatus(Long id, StatusRequest request) {
        Livro livro = buscarEntidade(id);
        livro.alterarStatus(request.status());
        return LivroResponse.from(livroRepository.save(livro));
    }

    @Transactional
    public LivroResponse registrarEntrada(Long id, MovimentacaoEstoqueRequest request) {
        Livro livro = buscarEntidade(id);
        entityManager.refresh(livro, LockModeType.PESSIMISTIC_WRITE);
        livro.receberExemplares(request.quantidade());
        return LivroResponse.from(livroRepository.save(livro));
    }

    @Transactional
    public LivroResponse registrarSaida(Long id, MovimentacaoEstoqueRequest request) {
        Livro livro = buscarEntidade(id);
        entityManager.refresh(livro, LockModeType.PESSIMISTIC_WRITE);
        livro.retirarExemplares(request.quantidade());
        return LivroResponse.from(livroRepository.save(livro));
    }

    @Transactional
    public void excluir(Long id) {
        Livro livro = buscarEntidade(id);
        if (itemEmprestimoRepository.existsByLivroId(id)) {
            throw new RecursoEmUsoException("Não é possível excluir livro vinculado a empréstimos");
        }
        livroRepository.delete(livro);
    }

    public Livro buscarEntidade(Long id) {
        return livroRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Livro não encontrado: " + id));
    }
}