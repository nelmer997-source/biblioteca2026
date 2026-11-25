package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.model.*;
import com.curso.biblioteca2026.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class EmprestimoServiceTest {

    private static final AtomicInteger SEQUENCIA_CPF = new AtomicInteger(100_000_000);

    @Autowired
    private EmprestimoService emprestimoService;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private LeitorService leitorService;

    @Autowired
    private BibliotecarioService bibliotecarioService;

    @Autowired
    private CategoriaLivroRepository categoriaRepository;

    @Autowired
    private LivroRepository livroRepository;

    @Test
    void deveFazerRollbackQuandoSegundoItemNaoPossuiExemplaresSuficientes() {
        CategoriaLivro categoria = categoriaRepository.save(new CategoriaLivro("Ficção Rollback " + System.nanoTime()));
        Livro livro1 = livroRepository.save(new Livro(
                "ISBN1-" + System.nanoTime(), "Livro Um", 5, new BigDecimal("30.00"), LocalDate.now(), true, categoria));
        Livro livro2 = livroRepository.save(new Livro(
                "ISBN2-" + System.nanoTime(), "Livro Dois", 1, new BigDecimal("40.00"), LocalDate.now(), true, categoria));

        Participantes participantes = criarParticipantes();

        Emprestimo emprestimo = emprestimoService.cadastrar(participantes.leitor().getId(), participantes.bibliotecario().getId());
        emprestimoService.adicionarItem(emprestimo.getId(), livro1.getId(), 2);
        emprestimoService.adicionarItem(emprestimo.getId(), livro2.getId(), 3);

        assertThrows(IllegalArgumentException.class, () -> emprestimoService.finalizar(emprestimo.getId()));

        Livro livro1Banco = livroRepository.findById(livro1.getId()).orElseThrow();
        Emprestimo emprestimoBanco = emprestimoService.buscarPorId(emprestimo.getId());

        assertEquals(5, livro1Banco.getQuantidadeExemplares());
        assertEquals(StatusEmprestimo.ABERTO, emprestimoBanco.getStatus());
    }

    @Test
    void deveImpedirEmprestimoAcimaDoSaldoEmFinalizacoesConcorrentes() throws Exception {
        CategoriaLivro categoria = categoriaRepository.save(new CategoriaLivro("Ficção Concorrente " + System.nanoTime()));
        Livro livro = livroRepository.save(new Livro(
                "CONC-" + System.nanoTime(), "Livro Disputado", 5, new BigDecimal("50.00"), LocalDate.now(), true, categoria));

        Participantes participantes = criarParticipantes();
        Emprestimo primeiro = criarEmprestimoComItem(participantes, livro, 4);
        Emprestimo segundo = criarEmprestimoComItem(participantes, livro, 4);

        CountDownLatch inicioSimultaneo = new CountDownLatch(1);

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<ResultadoFinalizacao> primeiroResultado = executor.submit(
                    () -> finalizarAposSinal(primeiro.getId(), inicioSimultaneo));
            Future<ResultadoFinalizacao> segundoResultado = executor.submit(
                    () -> finalizarAposSinal(segundo.getId(), inicioSimultaneo));

            inicioSimultaneo.countDown();

            List<ResultadoFinalizacao> resultados = List.of(
                    primeiroResultado.get(10, TimeUnit.SECONDS),
                    segundoResultado.get(10, TimeUnit.SECONDS));

            assertEquals(1, resultados.stream().filter(ResultadoFinalizacao::sucesso).count());
            assertEquals(1, resultados.stream().filter(r -> !r.sucesso()).count());
            assertTrue(resultados.stream()
                    .filter(r -> !r.sucesso())
                    .allMatch(r -> "Quantidade de exemplares insuficiente para retirada".equals(r.mensagem())));
        }

        assertEquals(1, livroRepository.findById(livro.getId()).orElseThrow().getQuantidadeExemplares());

        List<StatusEmprestimo> estados = List.of(
                emprestimoService.buscarPorId(primeiro.getId()).getStatus(),
                emprestimoService.buscarPorId(segundo.getId()).getStatus());
        assertEquals(1, estados.stream().filter(StatusEmprestimo.FINALIZADO::equals).count());
        assertEquals(1, estados.stream().filter(StatusEmprestimo.ABERTO::equals).count());
    }

    private Participantes criarParticipantes() {
        Pessoa pessoa = pessoaService.cadastrar(
                "Carlos Silva",
                "carlos." + System.nanoTime() + "@example.com",
                gerarCpfUnico());
        Leitor leitor = leitorService.cadastrar(pessoa.getId(), "(17) 98888-7777");
        Bibliotecario bib = bibliotecarioService.cadastrar(
                pessoa.getId(),
                "MAT-" + System.nanoTime(),
                FuncaoBibliotecario.ATENDENTE,
                LocalDate.now());
        return new Participantes(leitor, bib);
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

    private Emprestimo criarEmprestimoComItem(Participantes participantes, Livro livro, int quantidade) {
        Emprestimo emprestimo = emprestimoService.cadastrar(
                participantes.leitor().getId(),
                participantes.bibliotecario().getId());
        emprestimoService.adicionarItem(emprestimo.getId(), livro.getId(), quantidade);
        return emprestimo;
    }

    private ResultadoFinalizacao finalizarAposSinal(Long emprestimoId, CountDownLatch inicioSimultaneo)
            throws InterruptedException {
        inicioSimultaneo.await();
        try {
            emprestimoService.finalizar(emprestimoId);
            return new ResultadoFinalizacao(true, null);
        } catch (IllegalArgumentException exception) {
            return new ResultadoFinalizacao(false, exception.getMessage());
        }
    }

    private record Participantes(Leitor leitor, Bibliotecario bibliotecario) {
    }

    private record ResultadoFinalizacao(boolean sucesso, String mensagem) {
    }
}