package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
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
public class PessoaService {

    private final PessoaRepository repository;

    public PessoaService(PessoaRepository repository) {
        this.repository = repository;
    }

    public Page<Pessoa> listar(String nome, String cpf, Status status, Pageable pageable) {
        Specification<Pessoa> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            String termo = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nome")), termo));
        }

        if (cpf != null && !cpf.isBlank()) {
            String cpfLimpo = cpf.trim();
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("cpf"), cpfLimpo));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return repository.findAll(spec, pageable);
    }

    public Pessoa buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + id));
    }

    @Transactional
    public Pessoa cadastrar(String nome, String email, String cpf) {
        Pessoa pessoa = new Pessoa(nome, email, cpf, LocalDate.now());
        validarUnicidade(pessoa.getCpf(), pessoa.getEmail(), null);
        return repository.save(pessoa);
    }

    @Transactional
    public Pessoa atualizar(Long id, String nome, String email, String cpf) {
        Pessoa pessoa = buscarPorId(id);
        pessoa.alterarDados(nome, email, cpf);
        validarUnicidade(pessoa.getCpf(), pessoa.getEmail(), id);
        return pessoa;
    }

    @Transactional
    public Pessoa alterarStatus(Long id, Status status) {
        Pessoa pessoa = buscarPorId(id);
        pessoa.alterarStatus(status);
        return pessoa;
    }

    private void validarUnicidade(String cpf, String email, Long idAtual) {
        boolean cpfDuplicado = idAtual == null
                ? repository.existsByCpf(cpf)
                : repository.existsByCpfAndIdNot(cpf, idAtual);
        boolean emailDuplicado = idAtual == null
                ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, idAtual);

        if (cpfDuplicado) {
            throw new RecursoDuplicadoException("CPF já cadastrado");
        }
        if (emailDuplicado) {
            throw new RecursoDuplicadoException("E-mail já cadastrado");
        }
    }
}