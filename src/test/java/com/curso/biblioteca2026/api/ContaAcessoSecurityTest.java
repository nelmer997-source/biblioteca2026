package com.curso.biblioteca2026.api;

import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.Pessoa;
import com.curso.biblioteca2026.repository.ContaAcessoRepository;
import com.curso.biblioteca2026.repository.PessoaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ContaAcessoSecurityTest {

    private static final String SENHA_INICIAL = "uma-frase-senha-2026";
    private static final String NOVA_SENHA = "outra-frase-senha-2026";
    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(500_000_000);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ContaAcessoRepository contaAcessoRepository;

    @Test
    void devePermitirHealthPublicoERejeitarRotaProtegidaAnonima() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        mockMvc.perform(get("/api/livros"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveAutenticarComHttpBasicAlterarSenhaEBloquearContaInativa() throws Exception {
        String cpf = gerarCpfUnico();
        String email = "operadora." + System.nanoTime() + "@example.com";
        String login = "operadora." + System.nanoTime();

        String pessoaJson = """
                {
                  "nome": "Operadora Biblioteca",
                  "email": "%s",
                  "cpf": "%s"
                }
                """.formatted(email, cpf);

        mockMvc.perform(post("/api/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pessoaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(cpf));

        Pessoa pessoa = pessoaRepository.findAll().stream()
                .filter(p -> cpf.equals(p.getCpf()))
                .findFirst()
                .orElseThrow();

        String contaJson = """
                {
                  "pessoaId": %d,
                  "login": "%s",
                  "senha": "%s"
                }
                """.formatted(pessoa.getId(), login, SENHA_INICIAL);

        mockMvc.perform(post("/api/contas-acesso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contaJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value(login))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andExpect(jsonPath("$.perfis[0]").value("OPERADOR"))
                .andExpect(content().string(not(containsString(SENHA_INICIAL))))
                .andExpect(content().string(not(containsString("{bcrypt}"))));

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic(login, SENHA_INICIAL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(login));

        mockMvc.perform(get("/api/livros")
                        .with(httpBasic(login, SENHA_INICIAL)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic(login, "senha-incorreta-2026")))
                .andExpect(status().isUnauthorized());

        String trocaSenhaJson = """
                {
                  "senhaAtual": "%s",
                  "novaSenha": "%s"
                }
                """.formatted(SENHA_INICIAL, NOVA_SENHA);

        mockMvc.perform(put("/api/contas-acesso/me/senha")
                        .with(httpBasic(login, SENHA_INICIAL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trocaSenhaJson))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic(login, SENHA_INICIAL)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic(login, NOVA_SENHA)))
                .andExpect(status().isOk());

        ContaAcesso conta = contaAcessoRepository.findByLoginIgnoreCase(login).orElseThrow();
        conta.bloquear();
        contaAcessoRepository.saveAndFlush(conta);

        mockMvc.perform(get("/api/contas-acesso/me")
                        .with(httpBasic(login, NOVA_SENHA)))
                .andExpect(status().isUnauthorized());
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