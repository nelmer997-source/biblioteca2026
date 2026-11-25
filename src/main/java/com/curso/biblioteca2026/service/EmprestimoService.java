package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.*;
import com.curso.biblioteca2026.repository.BibliotecarioRepository;
import com.curso.biblioteca2026.repository.EmprestimoRepository;
import com.curso.biblioteca2026.repository.LeitorRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class EmprestimoService {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "dataEmprestimo", "status");

    private final EmprestimoRepository repository;
    private final LeitorRepository leitorRepository;
    private final BibliotecarioRepository bibliotecarioRepository;
    private final LivroRepository livroRepository;
    private final EntityManager entityManager;

    public EmprestimoService(
            EmprestimoRepository repository,
            LeitorRepository leitorRepository,
            BibliotecarioRepository bibliotecarioRepository,
            LivroRepository livroRepository,
            EntityManager entityManager) {
        this.repository = repository;
        this.leitorRepository = leitorRepository;
        this.bibliotecarioRepository = bibliotecarioRepository;
        this.livroRepository = livroRepository;
        this.entityManager = entityManager;
    }

    public Page<Emprestimo> listar(
            StatusEmprestimo status,
            Long leitorId,
            LocalDate dataInicial,
            LocalDate dataFinal,
            Pageable pageable) {

        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        validarOrdenacao(pageable.getSort());

        Specification<Emprestimo> filtros = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }
        if (leitorId != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.equal(root.get("leitor").get("id"), leitorId));
        }
        if (dataInicial != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("dataEmprestimo"), dataInicial.atStartOfDay()));
        }
        if (dataFinal != null) {
            filtros = filtros.and((root, query, cb) ->
                    cb.lessThan(root.get("dataEmprestimo"), dataFinal.plusDays(1).atStartOfDay()));
        }

        return repository.findAll(filtros, pageable);
    }

    public Emprestimo buscarPorId(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado: " + id));
    }

    @Transactional
    public Emprestimo cadastrar(Long leitorId, Long bibliotecarioId) {
        Leitor leitor = leitorRepository.findOneById(leitorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado: " + leitorId));
        Bibliotecario bibliotecario = bibliotecarioRepository.findOneById(bibliotecarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bibliotecário não encontrado: " + bibliotecarioId));

        validarParticipantes(leitor, bibliotecario);
        return repository.save(new Emprestimo(leitor, bibliotecario, LocalDateTime.now()));
    }

    @Transactional
    public Emprestimo adicionarItem(Long emprestimoId, Long livroId, Integer quantidade) {
        Emprestimo emprestimo = buscarPorId(emprestimoId);
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Livro não encontrado: " + livroId));

        if (livro.getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Livro deve estar ativo para empréstimo");
        }

        emprestimo.adicionarItem(livro, quantidade);
        return emprestimo;
    }

    @Transactional
    public Emprestimo removerItem(Long emprestimoId, Long itemId) {
        Emprestimo emprestimo = buscarPorId(emprestimoId);
        emprestimo.removerItem(itemId);
        return emprestimo;
    }

    @Transactional
    public Emprestimo finalizar(Long id) {
        Emprestimo emprestimo = buscarPorId(id);
        validarParticipantes(emprestimo.getLeitor(), emprestimo.getBibliotecario());

        List<ItemEmprestimo> itensOrdenados = emprestimo.getItens().stream()
                .sorted(Comparator.comparing(item -> item.getLivro().getId()))
                .toList();

        for (ItemEmprestimo item : itensOrdenados) {
            Livro livro = item.getLivro();
            entityManager.refresh(livro, LockModeType.PESSIMISTIC_WRITE);

            if (livro.getStatus() != Status.ATIVO) {
                throw new IllegalArgumentException("Todos os livros do empréstimo devem estar ativos");
            }
            livro.retirarExemplares(item.getQuantidade());
        }

        emprestimo.finalizar();
        return emprestimo;
    }

    @Transactional
    public Emprestimo cancelar(Long id) {
        Emprestimo emprestimo = buscarPorId(id);
        emprestimo.cancelar();
        return emprestimo;
    }

    private void validarParticipantes(Leitor leitor, Bibliotecario bibliotecario) {
        if (leitor.getPessoa().getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Leitor deve estar com cadastro de pessoa ativo");
        }
        if (bibliotecario.getPessoa().getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Bibliotecário deve estar com cadastro de pessoa ativo");
        }
        if (bibliotecario.getFuncao() != FuncaoBibliotecario.ATENDENTE
                && bibliotecario.getFuncao() != FuncaoBibliotecario.COORDENADOR) {
            throw new IllegalArgumentException("Apenas atendentes ou coordenadores podem registrar empréstimos");
        }
    }

    private void validarOrdenacao(Sort sort) {
        for (Sort.Order order : sort) {
            if (!CAMPOS_ORDENACAO.contains(order.getProperty())) {
                throw new IllegalArgumentException("Campo de ordenação inválido: " + order.getProperty());
            }
        }
    }
}