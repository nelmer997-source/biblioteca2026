package com.curso.biblioteca2026.repository;

import com.curso.biblioteca2026.model.ContaAcesso;
import com.curso.biblioteca2026.model.PerfilAcesso;
import com.curso.biblioteca2026.model.Status;
import com.curso.biblioteca2026.model.StatusContaAcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContaAcessoRepository extends JpaRepository<ContaAcesso, Long> {

    boolean existsByLoginIgnoreCase(String login);

    boolean existsByPessoaId(Long pessoaId);

    @EntityGraph(attributePaths = {"pessoa", "perfis"})
    Optional<ContaAcesso> findOneById(Long id);

    @EntityGraph(attributePaths = {"pessoa", "perfis"})
    Optional<ContaAcesso> findByLoginIgnoreCase(String login);

    @Query("""
            select count(distinct c)
            from ContaAcesso c
            join c.perfis p
            where c.status = :statusConta
              and c.pessoa.status = :statusPessoa
              and p = :perfil
            """)
    long countAtivasPorPerfil(
            @Param("statusConta") StatusContaAcesso statusConta,
            @Param("statusPessoa") Status statusPessoa,
            @Param("perfil") PerfilAcesso perfil);
}