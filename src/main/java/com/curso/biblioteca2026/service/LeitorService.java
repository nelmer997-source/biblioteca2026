package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.Leitor;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.repository.LeitorRepository;
import com.curso.biblioteca2026.repository.PessoaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class LeitorService {

    private final LeitorRepository repository;
    private final PessoaRepository pessoaRepository;

    public LeitorService(LeitorRepository repository, PessoaRepository pessoaRepository) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
    }

    public Page<Leitor> listar(String nome, String cpf, Pageable pageable) {
        Specification<Leitor> spec = (root, query, cb) -> cb.conjunction();

        if (nome != null && !nome.isBlank()) {
            String termo = "%" + nome.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("pessoa").get("nome")), termo));
        }

        if (cpf != null && !cpf.isBlank()) {
            String cpfLimpo = cpf.trim();
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("pessoa").get("cpf"), cpfLimpo));
        }

        return repository.findAll(spec, pageable);
    }

    public Leitor buscarPorId(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Leitor não encontrado: " + id));
    }

    @Transactional
    public Leitor cadastrar(Long pessoaId, String telefone) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + pessoaId));

        if (pessoa.getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Pessoa deve estar ativa para assumir papel de leitor");
        }
        if (repository.existsByPessoaId(pessoaId)) {
            throw new RecursoDuplicadoException("Pessoa já possui cadastro de leitor");
        }

        return repository.save(new Leitor(pessoa, telefone));
    }

    @Transactional
    public Leitor atualizar(Long id, String telefone) {
        Leitor leitor = buscarPorId(id);
        leitor.alterarDados(telefone);
        return leitor;
    }
}