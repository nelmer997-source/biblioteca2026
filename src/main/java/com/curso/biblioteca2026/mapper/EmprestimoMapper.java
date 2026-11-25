package com.curso.biblioteca2026.mapper;

import com.curso.biblioteca2026.dto.EmprestimoResponse;
import com.curso.biblioteca2026.dto.EmprestimoResumoResponse;
import com.curso.biblioteca2026.dto.ItemEmprestimoResponse;
import com.curso.biblioteca2026.model.Emprestimo;
import com.curso.biblioteca2026.model.ItemEmprestimo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmprestimoMapper {

    public EmprestimoResponse toResponse(Emprestimo emprestimo) {
        List<ItemEmprestimoResponse> itens = emprestimo.getItens().stream()
                .map(this::toItemResponse)
                .toList();

        return new EmprestimoResponse(
                emprestimo.getId(),
                emprestimo.getLeitor().getId(),
                emprestimo.getLeitor().getPessoa().getNome(),
                emprestimo.getBibliotecario().getId(),
                emprestimo.getBibliotecario().getPessoa().getNome(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getStatus(),
                itens,
                emprestimo.calcularValorTotalReposicao()
        );
    }

    public EmprestimoResumoResponse toResumo(Emprestimo emprestimo) {
        return new EmprestimoResumoResponse(
                emprestimo.getId(),
                emprestimo.getLeitor().getId(),
                emprestimo.getLeitor().getPessoa().getNome(),
                emprestimo.getBibliotecario().getId(),
                emprestimo.getBibliotecario().getPessoa().getNome(),
                emprestimo.getDataEmprestimo(),
                emprestimo.getStatus()
        );
    }

    private ItemEmprestimoResponse toItemResponse(ItemEmprestimo item) {
        return new ItemEmprestimoResponse(
                item.getId(),
                item.getLivro().getId(),
                item.getLivro().getIsbn(),
                item.getLivro().getTitulo(),
                item.getQuantidade(),
                item.getValorReposicaoUnitario(),
                item.calcularSubtotalReposicao()
        );
    }
}