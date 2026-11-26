package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.EventoSeguranca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventoSegurancaRepository
        extends JpaRepository<EventoSeguranca, Long>, JpaSpecificationExecutor<EventoSeguranca> {
}