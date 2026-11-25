package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.CategoriaLivro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoriaLivroRepository
        extends JpaRepository<CategoriaLivro, Long>, JpaSpecificationExecutor<CategoriaLivro> {

    Optional<CategoriaLivro> findByNomeIgnoreCase(String nome);
}
