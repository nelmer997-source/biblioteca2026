package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.Emprestimo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long>, JpaSpecificationExecutor<Emprestimo> {

    @EntityGraph(attributePaths = {"leitor.pessoa", "bibliotecario.pessoa", "itens.livro"})
    Optional<Emprestimo> findOneById(Long id);

    @Override
    @EntityGraph(attributePaths = {"leitor.pessoa", "bibliotecario.pessoa"})
    Page<Emprestimo> findAll(@Nullable Specification<Emprestimo> specification, Pageable pageable);
}