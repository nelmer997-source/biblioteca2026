package com.curso.biblioteca2026.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Resposta paginada padrão da API (índice de página inicia em 0)")
public record PaginaResponse<T>(
        @Schema(description = "Itens da página atual")
        List<T> conteudo,

        @Schema(description = "Número da página atual (inicia em 0)", example = "0")
        int pagina,

        @Schema(description = "Tamanho da página", example = "20")
        int tamanho,

        @Schema(description = "Total de registros encontrados", example = "1")
        long totalElementos,

        @Schema(description = "Total de páginas disponíveis", example = "1")
        int totalPaginas,

        @Schema(description = "Indica se é a primeira página", example = "true")
        boolean primeira,

        @Schema(description = "Indica se é a última página", example = "true")
        boolean ultima
) {

    public static <T> PaginaResponse<T> from(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
