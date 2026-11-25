package com.curso.biblioteca2026.mapper;

import com.curso.biblioteca2026.dto.PessoaResponse;
import com.curso.biblioteca2026.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class PessoaMapper {

    public PessoaResponse toResponse(Pessoa pessoa) {
        return new PessoaResponse(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getCpf(),
                pessoa.getDataCadastro(),
                pessoa.getStatus()
        );
    }
}