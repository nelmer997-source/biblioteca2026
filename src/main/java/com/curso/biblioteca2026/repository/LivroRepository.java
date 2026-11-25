package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.Livro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.Nullable;

import java.util.Optional;

public interface LivroRepository
        extends JpaRepository<Livro, Long>, JpaSpecificationExecutor<Livro> {

    Optional<Livro> findByIsbnIgnoreCase(String isbn);

    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsByCategoriaId(Long categoriaId);

    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Livro> findAll(@Nullable Specification<Livro> spec, Pageable pageable);
}
