package com.curso.biblioteca2026.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarNaoEncontrado(
            RecursoNaoEncontradoException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(resposta(
                        HttpStatus.NOT_FOUND.value(),
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> tratarConflito(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(resposta(
                        HttpStatus.CONFLICT.value(),
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(
            MethodArgumentNotValidException exception) {

        Map<String, Object> resposta = resposta(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos"
        );

        Map<String, String> erros = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        erros.put(erro.getField(), erro.getDefaultMessage())
                );

        resposta.put("erros", erros);

        return ResponseEntity
                .badRequest()
                .body(resposta);
    }

    private Map<String, Object> resposta(int status, String mensagem) {
        Map<String, Object> resposta = new HashMap<>();

        resposta.put("timestamp", LocalDateTime.now());
        resposta.put("status", status);
        resposta.put("mensagem", mensagem);

        return resposta;
    }
}