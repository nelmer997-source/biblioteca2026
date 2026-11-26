package com.curso.biblioteca2026.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ContaAcessoTest {

    @Test
    void deveCriarContaAtivaComLoginTrimmedEPerfilOperador() {
        Pessoa pessoa = novaPessoa();

        ContaAcesso conta = new ContaAcesso(
                pessoa,
                "  ana.2026  ",
                "{bcrypt}hash-seguro"
        );

        assertAll(
                () -> assertEquals("ana.2026", conta.getLogin()),
                () -> assertEquals("{bcrypt}hash-seguro", conta.getSenhaHash()),
                () -> assertEquals(StatusContaAcesso.ATIVA, conta.getStatus()),
                () -> assertEquals(Set.of(PerfilAcesso.OPERADOR), conta.getPerfis()),
                () -> assertTrue(conta.podeAutenticar())
        );
    }

    @ParameterizedTest(name = "login inválido: [{0}]")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void naoDeveAceitarLoginNuloOuEmBranco(String loginInvalido) {
        Pessoa pessoa = novaPessoa();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new ContaAcesso(pessoa, loginInvalido, "{bcrypt}hash")
        );

        assertEquals("Login é obrigatório", erro.getMessage());
    }

    @ParameterizedTest(name = "login fora do padrão: [{0}]")
    @ValueSource(strings = {"ab", "usuario invalido", "user@domain"})
    void naoDeveAceitarLoginForaDoPadrao(String loginForaPadrao) {
        Pessoa pessoa = novaPessoa();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> new ContaAcesso(pessoa, loginForaPadrao, "{bcrypt}hash")
        );

        assertEquals("Login inválido", erro.getMessage());
    }

    @Test
    void deveAlterarSenhaSomenteQuandoOHashForValido() {
        ContaAcesso conta = novaConta();

        conta.alterarSenha("{bcrypt}novo-hash");

        assertEquals("{bcrypt}novo-hash", conta.getSenhaHash());
        assertThrows(
                IllegalArgumentException.class,
                () -> conta.alterarSenha(" ")
        );
    }

    @Test
    void deveAdministrarPerfisSemPermitirContaSemPerfil() {
        ContaAcesso conta = novaConta();

        conta.concederPerfil(PerfilAcesso.ADMINISTRADOR);
        conta.substituirPerfis(Set.of(PerfilAcesso.ADMINISTRADOR));

        assertTrue(conta.possuiPerfil(PerfilAcesso.ADMINISTRADOR));
        assertFalse(conta.possuiPerfil(PerfilAcesso.OPERADOR));
        assertThrows(
                IllegalArgumentException.class,
                () -> conta.substituirPerfis(Set.of())
        );
    }

    @Test
    void deveConsiderarOStatusDaContaEDaPessoaParaAutenticar() {
        Pessoa pessoa = novaPessoa();
        ContaAcesso conta = new ContaAcesso(pessoa, "ana.2026", "{bcrypt}hash");

        conta.bloquear();
        assertFalse(conta.podeAutenticar());

        conta.ativar();
        pessoa.inativar();
        assertFalse(conta.podeAutenticar());

        pessoa.ativar();
        assertTrue(conta.podeAutenticar());
    }

    private ContaAcesso novaConta() {
        return new ContaAcesso(novaPessoa(), "ana.2026", "{bcrypt}hash");
    }

    private Pessoa novaPessoa() {
        return new Pessoa(
                "Ana Souza",
                "ana.teste@example.com",
                "52998224725",
                LocalDate.of(2026, 11, 25)
        );
    }
}