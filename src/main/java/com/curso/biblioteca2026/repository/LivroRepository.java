package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivroRepository extends JpaRepository<Livro, String> {

    List<Livro> findByCategoriaId(Long categoriaId);
}