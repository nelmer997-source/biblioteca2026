package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.model.EventoSeguranca;
import com.curso.biblioteca2026.model.TipoEventoSeguranca;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Representação de resposta de um evento de segurança auditado")
public record EventoSegurancaResponse(
        @Schema(description = "Identificador do evento", example = "1")
        Long id,

        @Schema(description = "Tipo do evento de segurança", example = "LOGIN_FALHA")
        TipoEventoSeguranca tipo,

        @Schema(description = "Instante de ocorrência em UTC", example = "2026-11-26T16:00:00Z")
        Instant ocorridoEm,

        @Schema(description = "Identificador histórico da conta que iniciou a ação, quando conhecida", example = "1")
        Long atorContaId,

        @Schema(description = "Identificador histórico da conta afetada, quando conhecida", example = "2")
        Long alvoContaId,

        @Schema(description = "Endereço de origem observado na conexão", example = "127.0.0.1")
        String enderecoOrigem
) {

    public static EventoSegurancaResponse from(EventoSeguranca evento) {
        return new EventoSegurancaResponse(
                evento.getId(),
                evento.getTipo(),
                evento.getOcorridoEm(),
                evento.getAtorContaId(),
                evento.getAlvoContaId(),
                evento.getEnderecoOrigem()
        );
    }
}