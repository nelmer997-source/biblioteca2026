package com.curso.biblioteca2026.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({CredenciaisInvalidasException.class, RefreshTokenInvalidoException.class})
    public ResponseEntity<ApiError> tratarNaoAutorizado(
            RuntimeException exception,
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler({RecursoDuplicadoException.class, RecursoEmUsoException.class})
    public ResponseEntity<ApiError> tratarConflito(
            RuntimeException exception,
            HttpServletRequest request) {

        return construirResposta(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiError> tratarRequisicaoInvalida(
            Exception exception,
            HttpServletRequest request) {

        String mensagem = exception instanceof HttpMessageNotReadableException
                ? "Requisição inválida ou JSON malformado"
                : exception.getMessage();

        return construirResposta(
                HttpStatus.BAD_REQUEST,
                mensagem,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> campos = new LinkedHashMap<>();
        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));

        return construirResposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                request.getRequestURI(),
                campos
        );
    }

    private ResponseEntity<ApiError> construirResposta(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> fields) {

        ApiError corpo = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fields
        );

        return ResponseEntity.status(status).body(corpo);
    }
}