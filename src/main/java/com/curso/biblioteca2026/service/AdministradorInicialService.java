package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.config.AdministradorInicialProperties;
import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.model.StatusContaAcesso;
import com.curso.biblioteca2026.repository.ContaAcessoRepository;
import com.curso.biblioteca2026.repository.PessoaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AdministradorInicialService {

    private final ContaAcessoRepository contaAcessoRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorInicialService(
            ContaAcessoRepository contaAcessoRepository,
            PessoaRepository pessoaRepository,
            PasswordEncoder passwordEncoder) {
        this.contaAcessoRepository = contaAcessoRepository;
        this.pessoaRepository = pessoaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Optional<ContaAcesso> criarSeNecessario(AdministradorInicialProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Propriedades do administrador inicial n\u00e3o configuradas");
        }
        validarObrigatorio(properties.nome(), "Nome do administrador inicial \u00e9 obrigat\u00f3rio");
        validarObrigatorio(properties.email(), "E-mail do administrador inicial \u00e9 obrigat\u00f3rio");
        validarObrigatorio(properties.cpf(), "CPF do administrador inicial \u00e9 obrigat\u00f3rio");
        String loginLimpo = validarObrigatorio(properties.login(), "Login do administrador inicial \u00e9 obrigat\u00f3rio");
        ContaAcessoService.validarSenha(properties.senha());

        long administradoresAtivos = contaAcessoRepository.countAtivasPorPerfil(
                StatusContaAcesso.ATIVA,
                Status.ATIVO,
                PerfilAcesso.ADMINISTRADOR
        );
        if (administradoresAtivos > 0) {
            return Optional.empty();
        }

        Pessoa pessoa = new Pessoa(properties.nome(), properties.email(), properties.cpf(), LocalDate.now());

        if (pessoaRepository.existsByCpf(pessoa.getCpf())) {
            throw new RecursoDuplicadoException("CPF já cadastrado para o administrador inicial");
        }
        if (pessoaRepository.existsByEmailIgnoreCase(pessoa.getEmail())) {
            throw new RecursoDuplicadoException("E-mail já cadastrado para o administrador inicial");
        }
        if (contaAcessoRepository.existsByLoginIgnoreCase(loginLimpo)) {
            throw new RecursoDuplicadoException("Login já cadastrado para o administrador inicial");
        }

        Pessoa pessoaSalva = pessoaRepository.save(pessoa);
        String senhaHash = passwordEncoder.encode(properties.senha());
        ContaAcesso conta = new ContaAcesso(pessoaSalva, loginLimpo, senhaHash);
        conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);

        return Optional.of(contaAcessoRepository.save(conta));
    }

    private static String validarObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }
}