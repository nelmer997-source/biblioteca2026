package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.repository.ContaAcessoRepository;
import com.curso.biblioteca2026.repository.PessoaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class ContaAcessoService {

    private static final int TAMANHO_MINIMO_SENHA = 12;
    private static final int LIMITE_BYTES_BCRYPT = 72;

    private final ContaAcessoRepository repository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaAcessoService(
            ContaAcessoRepository repository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ContaAcesso buscarPorLogin(String login) {
        String loginLimpo = login != null ? login.trim() : "";
        return repository.findByLoginIgnoreCase(loginLimpo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de acesso não encontrada"));
    }

    @Transactional
    public ContaAcesso cadastrar(Long pessoaId, String login, String senhaAberta) {
        Pessoa pessoa = pessoaRepository.findById(pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa não encontrada: " + pessoaId));

        if (pessoa.getStatus() != Status.ATIVO) {
            throw new IllegalArgumentException("Pessoa deve estar ativa para possuir conta de acesso");
        }
        if (repository.existsByPessoaId(pessoaId)) {
            throw new RecursoDuplicadoException("Pessoa já possui conta de acesso");
        }

        String loginLimpo = login != null ? login.trim() : "";
        if (repository.existsByLoginIgnoreCase(loginLimpo)) {
            throw new RecursoDuplicadoException("Login já cadastrado");
        }

        validarSenha(senhaAberta);
        String senhaHash = passwordEncoder.encode(senhaAberta);

        return repository.save(new ContaAcesso(pessoa, loginLimpo, senhaHash));
    }

    @Transactional
    public void alterarSenha(String login, String senhaAtual, String novaSenha) {
        ContaAcesso conta = buscarPorLogin(login);

        if (senhaAtual == null || senhaAtual.isBlank()) {
            throw new IllegalArgumentException("Senha atual é obrigatória");
        }
        validarSenha(novaSenha);

        if (!passwordEncoder.matches(senhaAtual, conta.getSenhaHash())) {
            throw new IllegalArgumentException("Senha atual inválida");
        }
        if (passwordEncoder.matches(novaSenha, conta.getSenhaHash())) {
            throw new IllegalArgumentException("Nova senha deve ser diferente da atual");
        }

        conta.alterarSenha(passwordEncoder.encode(novaSenha));
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("Senha deve possuir no mínimo 12 caracteres");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > LIMITE_BYTES_BCRYPT) {
            throw new IllegalArgumentException("Senha deve possuir no máximo 72 bytes em UTF-8");
        }
    }
}