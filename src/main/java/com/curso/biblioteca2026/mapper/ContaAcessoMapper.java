package com.curso.biblioteca2026.mapper;

import com.curso.biblioteca2026.dto.ContaAcessoResponse;
import com.curso.biblioteca2026.model.ContaAcesso;
import org.springframework.stereotype.Component;

@Component
public class ContaAcessoMapper {

    public ContaAcessoResponse toResponse(ContaAcesso conta) {
        return new ContaAcessoResponse(
                conta.getId(),
                conta.getPessoa().getId(),
                conta.getPessoa().getNome(),
                conta.getLogin(),
                conta.getStatus(),
                conta.getPerfis()
        );
    }
}