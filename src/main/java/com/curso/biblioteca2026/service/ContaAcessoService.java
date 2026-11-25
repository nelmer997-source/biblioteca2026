package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.model.StatusContaAcesso;
import com.curso.biblioteca2026.repository.ContaAcessoRepository;
import com.curso.biblioteca2026.repository.PessoaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Set;

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

    public ContaAcesso buscarPorId(Long id) {
        return repository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de acesso não encontrada: " + id));
    }

    public ContaAcesso buscarPorLogin(String login) {
        String loginLimpo = login != null ? login.trim() : "";
        return repository.findByLoginIgnoreCase(loginLimpo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de acesso não encontrada"));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
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
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ContaAcesso alterarPerfis(Long id, Set<PerfilAcesso> novosPerfis, String loginAutenticado) {
        if (novosPerfis == null || novosPerfis.isEmpty() || novosPerfis.contains(null)) {
            throw new IllegalArgumentException("A conta deve possuir ao menos um perfil");
        }

        ContaAcesso conta = buscarPorId(id);
        String loginAdmin = loginAutenticado != null ? loginAutenticado.trim() : "";
        boolean removeAdministrador = !novosPerfis.contains(PerfilAcesso.ADMINISTRADOR);

        if (conta.getLogin().equalsIgnoreCase(loginAdmin) && removeAdministrador) {
            throw new IllegalArgumentException(
                    "O administrador autenticado não pode remover o próprio perfil de administrador");
        }

        boolean eraAdministradorAtivo = conta.podeAutenticar() && conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR);
        if (eraAdministradorAtivo && removeAdministrador && quantidadeAdministradoresAtivos() <= 1) {
            throw new IllegalArgumentException("O sistema deve manter ao menos um administrador ativo");
        }

        conta.substituirPerfis(novosPerfis);
        return conta;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ContaAcesso alterarStatus(Long id, StatusContaAcesso novoStatus, String loginAutenticado) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("Status é obrigatório");
        }

        ContaAcesso conta = buscarPorId(id);
        String loginAdmin = loginAutenticado != null ? loginAutenticado.trim() : "";

        if (novoStatus == StatusContaAcesso.BLOQUEADA && conta.getLogin().equalsIgnoreCase(loginAdmin)) {
            throw new IllegalArgumentException("O administrador autenticado não pode bloquear a própria conta");
        }

        boolean eraAdministradorAtivo = conta.podeAutenticar() && conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR);
        if (novoStatus == StatusContaAcesso.BLOQUEADA
                && eraAdministradorAtivo
                && quantidadeAdministradoresAtivos() <= 1) {
            throw new IllegalArgumentException("O sistema deve manter ao menos um administrador ativo");
        }

        if (novoStatus == StatusContaAcesso.ATIVA) {
            conta.ativar();
        } else {
            conta.bloquear();
        }
        return conta;
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

    private long quantidadeAdministradoresAtivos() {
        return repository.countAtivasPorPerfil(
                StatusContaAcesso.ATIVA,
                Status.ATIVO,
                PerfilAcesso.ADMINISTRADOR
        );
    }

    static void validarSenha(String senha) {
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