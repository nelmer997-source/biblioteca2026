package com.curso.biblioteca2026.service;

import com.curso.biblioteca2026.model.EventoSeguranca;
import com.curso.biblioteca2026.model.TipoEventoSeguranca;
import com.curso.biblioteca2026.repository.EventoSegurancaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class AuditoriaSegurancaService {

    private static final Sort ORDENACAO_PADRAO = Sort.by(
            Sort.Order.desc("ocorridoEm"),
            Sort.Order.desc("id")
    );

    private final EventoSegurancaRepository repository;
    private final Clock clock;

    public AuditoriaSegurancaService(EventoSegurancaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public EventoSeguranca registrar(
            TipoEventoSeguranca tipo,
            Long atorContaId,
            Long alvoContaId,
            String identificadorHash,
            String enderecoOrigem) {
        EventoSeguranca evento = new EventoSeguranca(
                tipo,
                clock.instant(),
                atorContaId,
                alvoContaId,
                identificadorHash,
                enderecoOrigem
        );
        return repository.save(evento);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Transactional(readOnly = true)
    public Page<EventoSeguranca> consultar(TipoEventoSeguranca tipo, Pageable pageable) {
        Specification<EventoSeguranca> spec = (root, query, cb) -> cb.conjunction();
        if (tipo != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipo"), tipo));
        }
        return repository.findAll(spec, garantirOrdenacaoCronologicaDecrescente(pageable));
    }

    private Pageable garantirOrdenacaoCronologicaDecrescente(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, 20, ORDENACAO_PADRAO);
        }
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort().and(Sort.by(Sort.Order.desc("id")))
                : ORDENACAO_PADRAO;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}