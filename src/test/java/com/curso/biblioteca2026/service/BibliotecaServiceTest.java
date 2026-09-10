package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.dto.CategoriaRequest;
import com.curso.biblioteca2026.dto.LivroRequest;
import com.curso.biblioteca2026.dto.LivroResponse;
import com.curso.biblioteca2026.exception.RecursoNaoEncontradoException;
import com.curso.biblioteca2026.model.CategoriaLivro;
import com.curso.biblioteca2026.model.Livro;
import com.curso.biblioteca2026.repository.CategoriaLivroRepository;
import com.curso.biblioteca2026.repository.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BibliotecaServiceTest {

    @Mock
    private CategoriaLivroRepository categoriaRepository;

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private CategoriaLivroService categoriaService;

    @InjectMocks
    private LivroService livroService;

    private CategoriaLivro categoria;
    private Livro livro;

    @BeforeEach
    void prepararDados() {
        categoria = new CategoriaLivro("Literatura");

        livro = new Livro(
                "9788535902778",
                "Dom Casmurro",
                5,
                new BigDecimal("50.00"),
                LocalDate.now(),
                true,
                categoria
        );
    }

    @Test
    void teste01ListarCategorias() {
        when(categoriaRepository.findAll())
                .thenReturn(List.of(categoria));

        List<CategoriaLivro> resultado =
                categoriaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Literatura", resultado.get(0).getNome());

        verify(categoriaRepository).findAll();
    }

    @Test
    void teste02BuscarCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaLivro resultado =
                categoriaService.buscar(1L);

        assertEquals("Literatura", resultado.getNome());

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
        CategoriaRequest request =
                new CategoriaRequest("Computação");

        when(categoriaRepository.findByNomeIgnoreCase("Computação"))
                .thenReturn(Optional.empty());

        CategoriaLivro categoriaSalva =
                new CategoriaLivro("Computação");

        when(categoriaRepository.save(any(CategoriaLivro.class)))
                .thenReturn(categoriaSalva);

        CategoriaLivro resultado =
                categoriaService.criar(request);

        assertEquals("Computação", resultado.getNome());

        verify(categoriaRepository)
                .findByNomeIgnoreCase("Computação");

        verify(categoriaRepository)
                .save(any(CategoriaLivro.class));
    }

    @Test
    void teste05CriarCategoriaDuplicada() {
        CategoriaRequest request =
                new CategoriaRequest("Literatura");

        when(categoriaRepository.findByNomeIgnoreCase("Literatura"))
                .thenReturn(Optional.of(categoria));

        assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.criar(request)
        );

        verify(categoriaRepository, never())
                .save(any(CategoriaLivro.class));
    }

    @Test
    void teste06AtualizarCategoria() {
        CategoriaRequest request =
                new CategoriaRequest("Romance");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.findByNomeIgnoreCase("Romance"))
                .thenReturn(Optional.empty());

        when(categoriaRepository.save(any(CategoriaLivro.class)))
                .thenReturn(categoria);

        CategoriaLivro resultado =
                categoriaService.atualizar(1L, request);

        assertEquals("Romance", resultado.getNome());

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void teste07AtualizarCategoriaInexistente() {
        CategoriaRequest request =
                new CategoriaRequest("Romance");

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.atualizar(99L, request)
        );

        verify(categoriaRepository, never())
                .save(any(CategoriaLivro.class));
    }

    @Test
    void teste08ExcluirCategoria() {
        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        categoriaService.excluir(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void teste09ExcluirCategoriaInexistente() {
        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> categoriaService.excluir(99L)
        );

        verify(categoriaRepository, never())
                .delete(any(CategoriaLivro.class));
    }

    @Test
    void teste10ListarLivros() {
        when(livroRepository.findAll())
                .thenReturn(List.of(livro));

        List<LivroResponse> resultado =
                livroService.listar();

        assertEquals(1, resultado.size());
        assertEquals(
                "Dom Casmurro",
                resultado.get(0).titulo()
        );

        verify(livroRepository).findAll();
    }

    @Test
    void teste11BuscarLivro() {
        when(livroRepository.findById("9788535902778"))
                .thenReturn(Optional.of(livro));

        LivroResponse resultado =
                livroService.buscar("9788535902778");

        assertEquals(
                "Dom Casmurro",
                resultado.titulo()
        );

        assertEquals(
                "9788535902778",
                resultado.isbn()
        );

        verify(livroRepository)
                .findById("9788535902778");
    }

    @Test
    void teste12BuscarLivroInexistente() {
        when(livroRepository.findById("999"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.buscar("999")
        );

        verify(livroRepository)
                .findById("999");
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

        when(livroRepository.existsById("123456789"))
                .thenReturn(false);

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(livroRepository.save(any(Livro.class)))
                .thenReturn(livro);

        LivroResponse resultado =
                livroService.criar(request);

        assertNotNull(resultado);

        verify(livroRepository)
                .existsById("123456789");

        verify(categoriaRepository)
                .findById(1L);

        verify(livroRepository)
                .save(any(Livro.class));
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

        when(livroRepository.existsById("9788535902778"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> livroService.criar(request)
        );

        verify(livroRepository, never())
                .save(any(Livro.class));
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

        when(livroRepository.existsById("123456789"))
                .thenReturn(false);

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.criar(request)
        );

        verify(livroRepository, never())
                .save(any(Livro.class));
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

        when(livroRepository.findById("9788535902778"))
                .thenReturn(Optional.of(livro));

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(livroRepository.save(any(Livro.class)))
                .thenReturn(livro);

        LivroResponse resultado =
                livroService.atualizar(
                        "9788535902778",
                        request
                );

        assertEquals(
                "Dom Casmurro Atualizado",
                livro.getTitulo()
        );

        assertEquals(
                10,
                livro.getQuantidadeExemplares()
        );

        assertEquals(
                new BigDecimal("60.00"),
                livro.getValorReposicao()
        );

        assertFalse(livro.getAtivo());

        assertNotNull(resultado);

        verify(livroRepository)
                .save(livro);
    }

    @Test
    void teste17AtualizarLivroInexistente() {
        LivroRequest request = new LivroRequest(
                "999",
                "Livro",
                1,
                new BigDecimal("10.00"),
                true,
                1L
        );

        when(livroRepository.findById("999"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.atualizar("999", request)
        );

        verify(livroRepository, never())
                .save(any(Livro.class));
    }

    @Test
    void teste18ExcluirLivro() {
        when(livroRepository.findById("9788535902778"))
                .thenReturn(Optional.of(livro));

        livroService.excluir("9788535902778");

        verify(livroRepository).delete(livro);
    }

    @Test
    void teste19ExcluirLivroInexistente() {
        when(livroRepository.findById("999"))
                .thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> livroService.excluir("999")
        );

        verify(livroRepository, never())
                .delete(any(Livro.class));
    }

}