package com.curso.biblioteca2026.mapper;

import com.curso.biblioteca2026.dto.BibliotecarioResponse;
import com.curso.biblioteca2026.model.Bibliotecario;
import org.springframework.stereotype.Component;

@Component
public class BibliotecarioMapper {

    public BibliotecarioResponse toResponse(Bibliotecario bibliotecario) {
        return new BibliotecarioResponse(
                bibliotecario.getId(),
                bibliotecario.getPessoa().getId(),
                bibliotecario.getPessoa().getNome(),
                bibliotecario.getPessoa().getCpf(),
                bibliotecario.getMatricula(),
                bibliotecario.getFuncao(),
                bibliotecario.getDataAdmissao(),
                bibliotecario.getPessoa().getStatus()
        );
    }
}