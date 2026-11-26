package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.model.*;
import com.curso.biblioteca2026.repository.BibliotecarioRepository;
import com.curso.biblioteca2026.repository.EmprestimoRepository;
import com.curso.biblioteca2026.repository.LeitorRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmprestimoServiceUnitTest {

    @Mock
    private EmprestimoRepository repository;

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private BibliotecarioRepository bibliotecarioRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private EmprestimoService service;

    @Test
    void naoDeveListarQuandoDataInicialForPosteriorADataFinal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.listar(
                        null,
                        null,
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 11, 20),
                        PageRequest.of(0, 20)
                )
        );
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveListarQuandoCampoDeOrdenacaoForInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.listar(
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20, Sort.by("campoInexistente"))
                )
        );
        verifyNoInteractions(repository);
    }

    @Test
    void naoDeveAbrirEmprestimoQuandoLeitorEstiverInativo() {
        Pessoa pessoaLeitor = new Pessoa("Ana Souza", "ana@example.com", "52998224725", LocalDate.now());
        pessoaLeitor.inativar();
        Leitor leitor = new Leitor(pessoaLeitor, "(17) 99999-0000");

        Pessoa pessoaBib = new Pessoa("Bruno Lima", "bruno@example.com", "11144477735", LocalDate.now());
        Bibliotecario bib = new Bibliotecario(pessoaBib, "BIB-001", FuncaoBibliotecario.ATENDENTE, LocalDate.now());

        when(leitorRepository.findOneById(1L)).thenReturn(Optional.of(leitor));
        when(bibliotecarioRepository.findOneById(1L)).thenReturn(Optional.of(bib));

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(1L, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveAbrirEmprestimoQuandoBibliotecarioNaoForAtendenteOuCoordenador() {
        Pessoa pessoaLeitor = new Pessoa("Ana Souza", "ana@example.com", "52998224725", LocalDate.now());
        Leitor leitor = new Leitor(pessoaLeitor, "(17) 99999-0000");

        Pessoa pessoaBib = new Pessoa("Bruno Lima", "bruno@example.com", "11144477735", LocalDate.now());
        Bibliotecario bib = new Bibliotecario(pessoaBib, "BIB-002", FuncaoBibliotecario.CATALOGADOR, LocalDate.now());

        when(leitorRepository.findOneById(1L)).thenReturn(Optional.of(leitor));
        when(bibliotecarioRepository.findOneById(1L)).thenReturn(Optional.of(bib));

        assertThrows(IllegalArgumentException.class, () -> service.cadastrar(1L, 1L));
        verify(repository, never()).save(any());
    }

    @Test
    void naoDeveAdicionarLivroInativoAoEmprestimo() {
        Emprestimo emprestimo = novoEmprestimoAberto();
        CategoriaLivro categoria = new CategoriaLivro("Literatura");
        Livro livroInativo = new Livro(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                LocalDate.now(),
                false,
                categoria
        );

        when(repository.findOneById(1L)).thenReturn(Optional.of(emprestimo));
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livroInativo));

        assertThrows(IllegalArgumentException.class, () -> service.adicionarItem(1L, 1L, 1));
        assertTrue(emprestimo.getItens().isEmpty());
    }

    @Test
    void deveCancelarEmprestimoAberto() {
        Emprestimo emprestimo = novoEmprestimoAberto();
        when(repository.findOneById(1L)).thenReturn(Optional.of(emprestimo));

        Emprestimo resultado = service.cancelar(1L);

        assertEquals(StatusEmprestimo.CANCELADO, resultado.getStatus());
    }

    private Emprestimo novoEmprestimoAberto() {
        Pessoa pessoaLeitor = new Pessoa("Ana Souza", "ana@example.com", "52998224725", LocalDate.now());
        Pessoa pessoaBib = new Pessoa("Bruno Lima", "bruno@example.com", "11144477735", LocalDate.now());
        return new Emprestimo(
                new Leitor(pessoaLeitor, "(17) 99999-0000"),
                new Bibliotecario(pessoaBib, "BIB-001", FuncaoBibliotecario.ATENDENTE, LocalDate.now()),
                LocalDateTime.now()
        );
    }
}