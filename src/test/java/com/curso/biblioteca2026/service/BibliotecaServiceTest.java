package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.dto.CategoriaRequest;
import com.curso.biblioteca2026.dto.CategoriaResponse;
import com.curso.biblioteca2026.dto.LivroRequest;
import com.curso.biblioteca2026.dto.LivroResponse;
import com.curso.biblioteca2026.dto.MovimentacaoEstoqueRequest;
import com.curso.biblioteca2026.dto.PaginaResponse;
import com.curso.biblioteca2026.dto.StatusRequest;
import com.curso.biblioteca2026.exception.RecursoDuplicadoException;
import com.curso.biblioteca2026.exception.RecursoEmUsoException;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.model.Livro;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.repository.CategoriaLivroRepository;
import com.curso.biblioteca2026.repository.ItemEmprestimoRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BibliotecaServiceTest {

    @Mock
    private CategoriaLivroRepository categoriaRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private ItemEmprestimoRepository itemEmprestimoRepository;

    @Mock
    private EntityManager entityManager;

    private CategoriaLivroService categoriaService;
    private LivroService livroService;

    private CategoriaLivro categoria;
    private Livro livro;
    private Pageable pageable;

    @BeforeEach
    void prepararDados() {
        categoriaService = new CategoriaLivroService(categoriaRepository, livroRepository);
        livroService = new LivroService(livroRepository, categoriaService, itemEmprestimoRepository, entityManager);

        pageable = PageRequest.of(0, 20);
        categoria = new CategoriaLivro("Literatura");
        ReflectionTestUtils.setField(categoria, "id", 1L);

        livro = new Livro(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                LocalDate.now(),
                true,
                categoria
        );
        ReflectionTestUtils.setField(livro, "id", 1L);
    }

    @Test
    void teste01ListarCategorias() {
        Page<CategoriaLivro> pagina = new PageImpl<>(List.of(categoria), pageable, 1);
        when(categoriaRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(pagina);

        PaginaResponse<CategoriaResponse> resultado =
                categoriaService.listar(null, null, pageable);

        assertEquals(1, resultado.conteudo().size());
        assertEquals("Literatura", resultado.conteudo().get(0).nome());
        verify(categoriaRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void teste02BuscarCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaResponse resultado = categoriaService.buscar(1L);

        assertEquals("Literatura", resultado.nome());
        verify(categoriaRepository).findById(1L);
    }

    @Test
    void teste03BuscarCategoriaInexistente() {
        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.buscar(99L)
        );
        verify(categoriaRepository).findById(99L);
    }

    @Test
    void teste04CriarCategoria() {
        CategoriaRequest request = new CategoriaRequest("Computação");

        when(categoriaRepository.findByNomeIgnoreCase("Computação"))
                .thenReturn(Optional.empty());

        when(categoriaRepository.save(any(CategoriaLivro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaResponse resultado = categoriaService.criar(request);

        assertEquals("Computação", resultado.nome());
        assertEquals(Status.ATIVO, resultado.status());
        verify(categoriaRepository).findByNomeIgnoreCase("Computação");
        verify(categoriaRepository).save(any(CategoriaLivro.class));
    }

    @Test
    void teste05CriarCategoriaDuplicada() {
        CategoriaRequest request = new CategoriaRequest("Literatura");

        when(categoriaRepository.findByNomeIgnoreCase("Literatura"))
                .thenReturn(Optional.of(categoria));

        assertThrows(
                RecursoDuplicadoException.class,
                () -> categoriaService.criar(request)
        );
        verify(categoriaRepository, never()).save(any(CategoriaLivro.class));
    }

    @Test
    void teste06AtualizarCategoria() {
        CategoriaRequest request = new CategoriaRequest("Romance");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(categoriaRepository.findByNomeIgnoreCase("Romance"))
                .thenReturn(Optional.empty());
        when(categoriaRepository.save(any(CategoriaLivro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaResponse resultado = categoriaService.atualizar(1L, request);

        assertEquals("Romance", resultado.nome());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void teste07AtualizarCategoriaInexistente() {
        CategoriaRequest request = new CategoriaRequest("Romance");

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.atualizar(99L, request)
        );
        verify(categoriaRepository, never()).save(any(CategoriaLivro.class));
    }

    @Test
    void teste08ExcluirCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(livroRepository.existsByCategoriaId(1L))
                .thenReturn(false);

        categoriaService.excluir(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void teste09ExcluirCategoriaEmUso() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(livroRepository.existsByCategoriaId(1L))
                .thenReturn(true);

        assertThrows(
                RecursoEmUsoException.class,
                () -> categoriaService.excluir(1L)
        );
        verify(categoriaRepository, never()).delete(any(CategoriaLivro.class));
    }

    @Test
    void teste10ListarLivros() {
        Page<Livro> pagina = new PageImpl<>(List.of(livro), pageable, 1);
        when(livroRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(pagina);

        PaginaResponse<LivroResponse> resultado =
                livroService.listar(null, null, null, null, pageable);

        assertEquals(1, resultado.conteudo().size());
        assertEquals("Dom Casmurro", resultado.conteudo().get(0).titulo());
        verify(livroRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void teste11BuscarLivro() {
        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));

        LivroResponse resultado = livroService.buscar(1L);

        assertEquals("Dom Casmurro", resultado.titulo());
        assertEquals("9788535902778", resultado.isbn());
        verify(livroRepository).findById(1L);
    }

    @Test
    void teste12BuscarLivroInexistente() {
        when(livroRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.buscar(99L)
        );
        verify(livroRepository).findById(99L);
    }

    @Test
    void teste13CriarLivro() {
        LivroRequest request = new LivroRequest(
                "123456789",
                "Livro Novo",
                3,
                new BigDecimal("40.00"),
                true,
                1L
        );

        when(livroRepository.existsByIsbnIgnoreCase("123456789"))
                .thenReturn(false);
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(livroRepository.save(any(Livro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse resultado = livroService.criar(request);

        assertNotNull(resultado);
        assertEquals("Livro Novo", resultado.titulo());
        verify(livroRepository).existsByIsbnIgnoreCase("123456789");
        verify(categoriaRepository).findById(1L);
        verify(livroRepository).save(any(Livro.class));
    }

    @Test
    void teste14CriarLivroComIsbnDuplicado() {
        LivroRequest request = new LivroRequest(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                true,
                1L
        );

        when(livroRepository.existsByIsbnIgnoreCase("9788535902778"))
                .thenReturn(true);

        assertThrows(
                RecursoDuplicadoException.class,
                () -> livroService.criar(request)
        );
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void teste15CriarLivroComCategoriaInexistente() {
        LivroRequest request = new LivroRequest(
                "123456789",
                "Livro Novo",
                3,
                new BigDecimal("40.00"),
                true,
                99L
        );

        when(livroRepository.existsByIsbnIgnoreCase("123456789"))
                .thenReturn(false);
        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.criar(request)
        );
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void teste16AtualizarLivro() {
        LivroRequest request = new LivroRequest(
                "9788535902778",
                "Dom Casmurro Atualizado",
                10,
                new BigDecimal("60.00"),
                false,
                1L
        );

        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));
        when(livroRepository.findByIsbnIgnoreCase("9788535902778"))
                .thenReturn(Optional.of(livro));
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(livroRepository.save(any(Livro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse resultado = livroService.atualizar(1L, request);

        assertEquals("Dom Casmurro Atualizado", livro.getTitulo());
        assertEquals(10, livro.getQuantidadeExemplares());
        assertEquals(new BigDecimal("60.00"), livro.getValorReposicao());
        assertFalse(livro.getAtivo());
        assertNotNull(resultado);
        verify(livroRepository).save(livro);
    }

    @Test
    void teste17MovimentarEstoqueLivro() {
        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));
        when(livroRepository.save(any(Livro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LivroResponse aposEntrada = livroService.registrarEntrada(1L, new MovimentacaoEstoqueRequest(5));
        assertEquals(10, aposEntrada.quantidadeExemplares());

        LivroResponse aposSaida = livroService.registrarSaida(1L, new MovimentacaoEstoqueRequest(3));
        assertEquals(7, aposSaida.quantidadeExemplares());

        assertThrows(
                IllegalArgumentException.class,
                () -> livroService.registrarSaida(1L, new MovimentacaoEstoqueRequest(99))
        );
    }

    @Test
    void teste18AlterarStatusCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(CategoriaLivro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoriaResponse resultado = categoriaService.alterarStatus(1L, new StatusRequest(Status.INATIVO));

        assertEquals(Status.INATIVO, resultado.status());
    }

    @Test
    void teste19ExcluirLivro() {
        when(livroRepository.findById(1L))
                .thenReturn(Optional.of(livro));
        when(itemEmprestimoRepository.existsByLivroId(1L))
                .thenReturn(false);

        livroService.excluir(1L);

        verify(livroRepository).delete(livro);
    }
}