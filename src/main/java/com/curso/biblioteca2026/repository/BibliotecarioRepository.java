package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.Bibliotecario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface BibliotecarioRepository extends JpaRepository<Bibliotecario, Long>, JpaSpecificationExecutor<Bibliotecario> {

    boolean existsByPessoaId(Long pessoaId);

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByMatriculaIgnoreCaseAndIdNot(String matricula, Long id);

    @EntityGraph(attributePaths = "pessoa")
    Optional<Bibliotecario> findOneById(Long id);

    @Override
    @EntityGraph(attributePaths = "pessoa")
    Page<Bibliotecario> findAll(@Nullable Specification<Bibliotecario> specification, Pageable pageable);
}