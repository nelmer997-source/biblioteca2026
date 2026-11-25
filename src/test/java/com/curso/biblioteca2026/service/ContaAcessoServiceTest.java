package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.model.StatusContaAcesso;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ContaAcessoServiceTest {

    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(300_000_000);

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private ContaAcessoService contaAcessoService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void autenticarComoAdministrador() {
        autenticarComRole("ROLE_ADMINISTRADOR");
    }

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePersistirContaComHashBcryptSemGuardarSenhaAberta() {
        Pessoa pessoa = pessoaService.cadastrar(
                "Ana Souza",
                "ana." + System.nanoTime() + "@example.com",
                gerarCpfUnico());

        String senhaAberta = "uma-frase-senha-2026";
        String login = "ana." + System.nanoTime();

        ContaAcesso conta = contaAcessoService.cadastrar(pessoa.getId(), login, senhaAberta);

        assertNotNull(conta.getId());
        assertEquals(StatusContaAcesso.ATIVA, conta.getStatus());
        assertEquals(Set.of(PerfilAcesso.OPERADOR), conta.getPerfis());
        assertNotEquals(senhaAberta, conta.getSenhaHash());
        assertTrue(conta.getSenhaHash().startsWith("{bcrypt}"));
        assertTrue(passwordEncoder.matches(senhaAberta, conta.getSenhaHash()));
        assertFalse(conta.toString().contains(senhaAberta));
        assertFalse(conta.toString().contains(conta.getSenhaHash()));
    }

    @Test
    void deveRejeitarSenhaAcimaDe72BytesUtf8() {
        Pessoa pessoa = pessoaService.cadastrar(
                "Carlos Mendes",
                "carlos." + System.nanoTime() + "@example.com",
                gerarCpfUnico());

        String senha73Bytes = "á".repeat(36) + "a";

        assertThrows(
                IllegalArgumentException.class,
                () -> contaAcessoService.cadastrar(pessoa.getId(), "carlos." + System.nanoTime(), senha73Bytes));
    }

    @Test
    void deveBloquearCadastroDiretoNoServiceSemPerfilAdministrador() {
        Pessoa pessoa = pessoaService.cadastrar(
                "Diana Rocha",
                "diana." + System.nanoTime() + "@example.com",
                gerarCpfUnico());

        autenticarComRole("ROLE_OPERADOR");

        assertThrows(
                AccessDeniedException.class,
                () -> contaAcessoService.cadastrar(pessoa.getId(), "diana." + System.nanoTime(), "uma-frase-senha-2026"));
    }

    private static void autenticarComRole(String role) {
        var auth = UsernamePasswordAuthenticationToken.authenticated(
                "usuario.teste",
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static String gerarCpfUnico() {
        long semente = Math.abs(System.nanoTime() + SEQUENCIA_CPF.incrementAndGet()) % 899_999_999L + 100_000_000L;
        String base = String.format("%09d", semente);
        final char primeiroDigito = base.charAt(0);
        if (base.chars().allMatch(c -> c == primeiroDigito)) {
            base = "123456789";
        }
        int d1 = calcularDigitoCpf(base, 10);
        int d2 = calcularDigitoCpf(base + d1, 11);
        return base + d1 + d2;
    }

    private static int calcularDigitoCpf(String base, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < base.length(); i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (pesoInicial - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}