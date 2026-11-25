package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.ItemEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemEmprestimoRepository extends JpaRepository<ItemEmprestimo, Long> {

    boolean existsByLivroId(Long livroId);
}