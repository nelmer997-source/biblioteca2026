package com.curso.biblioteca2026.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "evento_seguranca")
public class EventoSeguranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEventoSeguranca tipo;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @Column(name = "ator_conta_id")
    private Long atorContaId;

    @Column(name = "alvo_conta_id")
    private Long alvoContaId;

    @Column(name = "identificador_hash", length = 64)
    private String identificadorHash;

    @Column(name = "endereco_origem", length = 45)
    private String enderecoOrigem;

    protected EventoSeguranca() {
    }

    public EventoSeguranca(
            TipoEventoSeguranca tipo,
            Instant ocorridoEm,
            Long atorContaId,
            Long alvoContaId,
            String identificadorHash,
            String enderecoOrigem) {
        this.tipo = Objects.requireNonNull(tipo, "Tipo do evento é obrigatório");
        this.ocorridoEm = Objects.requireNonNull(ocorridoEm, "Instante do evento é obrigatório");
        this.atorContaId = atorContaId;
        this.alvoContaId = alvoContaId;
        this.identificadorHash = validarIdentificadorHash(identificadorHash);
        this.enderecoOrigem = validarEnderecoOrigem(enderecoOrigem);
    }

    public Long getId() {
        return id;
    }

    public TipoEventoSeguranca getTipo() {
        return tipo;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }

    public Long getAtorContaId() {
        return atorContaId;
    }

    public Long getAlvoContaId() {
        return alvoContaId;
    }

    public String getIdentificadorHash() {
        return identificadorHash;
    }

    public String getEnderecoOrigem() {
        return enderecoOrigem;
    }

    private static String validarIdentificadorHash(String hash) {
        if (hash == null) {
            return null;
        }
        if (!hash.matches("^[0-9a-f]{64}$")) {
            throw new IllegalArgumentException("Identificador hash inválido");
        }
        return hash;
    }

    private static String validarEnderecoOrigem(String origem) {
        if (origem == null) {
            return null;
        }
        if (origem.contains("\r") || origem.contains("\n")) {
            throw new IllegalArgumentException("Endereço de origem inválido");
        }
        String valor = origem.trim();
        if (valor.isEmpty()) {
            return null;
        }
        if (valor.length() > 45) {
            throw new IllegalArgumentException("Endereço de origem inválido");
        }
        return valor;
    }
}