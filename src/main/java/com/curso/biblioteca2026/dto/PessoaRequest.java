package com.curso.biblioteca2026.dto;

import com.curso.biblioteca2026.validation.CpfValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados civis para cadastro ou atualização de pessoa")
public record PessoaRequest(
        @Schema(description = "Nome completo", example = "Ana Souza")
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve possuir no máximo 150 caracteres")
        String nome,

        @Schema(description = "Endereço de e-mail único", example = "ana@example.com")
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 180, message = "E-mail deve possuir no máximo 180 caracteres")
        String email,

        @Schema(description = "CPF com 11 dígitos numéricos", example = "52998224725")
        @NotBlank(message = "CPF é obrigatório")
        @CpfValido
        String cpf
) {
}