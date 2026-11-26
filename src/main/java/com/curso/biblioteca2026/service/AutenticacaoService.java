package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.exception.CredenciaisInvalidasException;
import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.repository.ContaAcessoRepository;
import com.curso.biblioteca2026.security.TokensAutenticacao;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService {

    private final AuthenticationManager authenticationManager;
    private final SessaoAutenticacaoService sessaoAutenticacaoService;
    private final ProtecaoAutenticacaoService protecaoService;
    private final ContaAcessoRepository contaAcessoRepository;

    public AutenticacaoService(
            AuthenticationManager authenticationManager,
            SessaoAutenticacaoService sessaoAutenticacaoService,
            ProtecaoAutenticacaoService protecaoService,
            ContaAcessoRepository contaAcessoRepository) {
        this.authenticationManager = authenticationManager;
        this.sessaoAutenticacaoService = sessaoAutenticacaoService;
        this.protecaoService = protecaoService;
        this.contaAcessoRepository = contaAcessoRepository;
    }

    public TokensAutenticacao autenticar(String login, String senha) {
        return autenticar(login, senha, null);
    }

    public TokensAutenticacao autenticar(String login, String senha, String enderecoOrigem) {
        if (!protecaoService.permitirTentativa(login, enderecoOrigem)) {
            throw new CredenciaisInvalidasException("Login ou senha inválidos");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(login, senha));
            TokensAutenticacao tokens = sessaoAutenticacaoService.iniciar(authentication);
            Long contaId = contaAcessoRepository.findByLoginIgnoreCase(authentication.getName())
                    .map(ContaAcesso::getId)
                    .orElse(null);
            protecaoService.registrarSucesso(login, contaId, enderecoOrigem);
            return tokens;
        } catch (AuthenticationException exception) {
            protecaoService.registrarFalha(login, enderecoOrigem);
            throw new CredenciaisInvalidasException("Login ou senha inválidos");
        }
    }
}