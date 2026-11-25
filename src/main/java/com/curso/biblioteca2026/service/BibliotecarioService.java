package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.Bibliotecario;
import com.curso.biblioteca2026.model.FuncaoBibliotecario;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.repository.BibliotecarioRepository;
import com.curso.biblioteca2026.repository.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class BibliotecarioService {

    private final BibliotecarioRepository repository;
    private final PessoaRepository pessoaRepository;

    public BibliotecarioService(BibliotecarioRepository repository, PessoaRepository pessoaRepository) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
    }

    public Page<Bibliotecario> listar(String nome, String matricula, FuncaoBibliotecario funcao, Pageable pageable) {
        Specification<Bibliotecario> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            String termo = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("pessoa").get("nome")), termo));
        }

        if (matricula != null && !matricula.isBlank()) {
            String matLimpa = matricula.trim().toLowerCase(Locale.ROOT);
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("matricula")), matLimpa));
        }

        if (funcao != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("funcao"), funcao));
        }

        return repository.findAll(spec, pageable);
    }

    public Bibliotecario buscarPorId(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bibliotecário não encontrado: " + id));
    }

    @Transactional
    public Bibliotecario cadastrar(Long pessoaId, String matricula, FuncaoBibliotecario funcao, LocalDate dataAdmissao) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + pessoaId));

        if (pessoa.getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Pessoa deve estar ativa para assumir vínculo de bibliotecário");
        }
        if (repository.existsByPessoaId(pessoaId)) {
            throw new RecursoDuplicadoException("Pessoa já possui cadastro de bibliotecário");
        }
        String matLimpa = matricula != null ? matricula.trim() : "";
        if (repository.existsByMatriculaIgnoreCase(matLimpa)) {
            throw new RecursoDuplicadoException("Matrícula já cadastrada");
        }

        return repository.save(new Bibliotecario(pessoa, matLimpa, funcao, dataAdmissao));
    }

    @Transactional
    public Bibliotecario atualizar(Long id, String matricula, FuncaoBibliotecario funcao) {
        Bibliotecario bibliotecario = buscarPorId(id);
        String matLimpa = matricula != null ? matricula.trim() : "";
        if (repository.existsByMatriculaIgnoreCaseAndIdNot(matLimpa, id)) {
            throw new RecursoDuplicadoException("Matrícula já cadastrada");
        }
        bibliotecario.alterarDados(matLimpa, funcao);
        return bibliotecario;
    }
}