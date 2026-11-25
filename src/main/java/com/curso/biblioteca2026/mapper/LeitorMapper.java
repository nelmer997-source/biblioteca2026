package com.curso.biblioteca2026.mapper;

import com.curso.biblioteca2026.dto.LeitorResponse;
import com.curso.biblioteca2026.model.Leitor;
import org.springframework.stereotype.Component;

@Component
public class LeitorMapper {

    public LeitorResponse toResponse(Leitor leitor) {
        return new LeitorResponse(
                leitor.getId(),
                leitor.getPessoa().getId(),
                leitor.getPessoa().getNome(),
                leitor.getPessoa().getEmail(),
                leitor.getPessoa().getCpf(),
                leitor.getTelefone(),
                leitor.getPessoa().getStatus()
        );
    }
}