package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.config.AdministradorInicialProperties;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.StatusContaAcesso;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdministradorInicialServiceTest {

    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(700_000_000);

    @Autowired
    private AdministradorInicialService service;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void deveCriarAdministradorInicialQuandoNaoExisteAdminAtivoESerIdempotente() {
        String cpf = gerarCpfUnico();
        String email = "admin." + System.nanoTime() + "@example.com";
        String login = "admin." + System.nanoTime();
        String senha = "senha-admin-inicial-2026";

        AdministradorInicialProperties properties = new AdministradorInicialProperties(
                true,
                "Administrador Inicial",
                email,
                cpf,
                login,
                senha
        );

        Optional<ContaAcesso> criada = service.criarSeNecessario(properties);

        assertTrue(criada.isPresent());
        ContaAcesso conta = criada.orElseThrow();
        assertEquals(login, conta.getLogin());
        assertEquals(StatusContaAcesso.ATIVA, conta.getStatus());
        assertTrue(conta.possuiPerfil(PerfilAcesso.OPERADOR));
        assertTrue(conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR));
        assertTrue(passwordEncoder.matches(senha, conta.getSenhaHash()));

        Optional<ContaAcesso> segundaExecucao = service.criarSeNecessario(properties);
        assertTrue(segundaExecucao.isEmpty());
    }

    @Test
    void deveRejeitarConfiguracaoIncompletaDoAdministradorInicial() {
        AdministradorInicialProperties incompleta = new AdministradorInicialProperties(
                true,
                "Administrador Incompleto",
                "admin.incompleto@example.com",
                gerarCpfUnico(),
                "admin.incompleto",
                ""
        );

        assertThrows(IllegalArgumentException.class, () -> service.criarSeNecessario(incompleta));
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