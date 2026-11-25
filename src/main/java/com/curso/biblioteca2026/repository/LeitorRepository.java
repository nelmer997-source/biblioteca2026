package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.Leitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface LeitorRepository extends JpaRepository<Leitor, Long>, JpaSpecificationExecutor<Leitor> {

    boolean existsByPessoaId(Long pessoaId);

    @EntityGraph(attributePaths = "pessoa")
    Optional<Leitor> findOneById(Long id);

    @Override
    @EntityGraph(attributePaths = "pessoa")
    Page<Leitor> findAll(@Nullable Specification<Leitor> specification, Pageable pageable);
}