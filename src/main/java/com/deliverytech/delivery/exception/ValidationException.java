package com.deliverytech.delivery.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO (Data Transfer Object) que representa a resposta de erro
 * estruturada para falhas de validação (HTTP 400 Bad Request).
 * <p>
 * ATENÇÃO: Esta classe NÃO é uma Exceção (não estende RuntimeException),
 * mas sim o DTO de Resposta enviado ao cliente quando uma exceção
 * de validação (como MethodArgumentNotValidException) é capturada.
 */
public class ValidationException { // Nota: Este é um DTO, não uma Exceção.

    /**
     * O código de status HTTP (ex: 400).
     */
    private int status;

    /**
     * A descrição do status HTTP (ex: "Bad Request").
     */
    private String error;

    /**
     * Um mapa contendo os campos que falharam na validação
     * e as respectivas mensagens de erro (ex: "email": "deve ser válido").
     */
    private Map<String, String> validationErrors;

    /**
     * Data e hora em que o erro de validação ocorreu.
     */
    private LocalDateTime timestamp;

    /**
     * Construtor completo para a resposta de erro de validação.
     *
     * @param status O status HTTP (ex: 400).
     * @param error A descrição do erro (ex: "Bad Request").
     * @param validationErrors O mapa de erros de campo.
     * @param timestamp A data/hora do erro.
     */
    public ValidationException(int status, String error, Map<String, String> validationErrors, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.validationErrors = validationErrors;
        this.timestamp = timestamp;
    }

    // Getters e Setters
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Map<String, String> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(Map<String, String> validationErrors) { this.validationErrors = validationErrors; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}