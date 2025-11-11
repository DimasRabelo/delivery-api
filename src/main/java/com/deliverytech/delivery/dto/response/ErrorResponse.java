package com.deliverytech.delivery.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Representa a estrutura de resposta para erros da API.
 * Inclui informações detalhadas como status, mensagem, caminho e código de erro.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Não inclui campos nulos no JSON de resposta
public class ErrorResponse {

    /**
     * Data e hora em que o erro ocorreu.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * O código de status HTTP (ex: 404, 400, 500).
     */
    private int status;

    /**
     * A descrição do status HTTP (ex: "Not Found", "Bad Request").
     */
    private String error;

    /**
     * A mensagem de erro principal, amigável para o usuário ou desenvolvedor.
     */
    private String message;

    /**
     * O caminho (endpoint) da API onde o erro ocorreu.
     */
    private String path;

    /**
     * Um código de erro interno opcional para rastreabilidade.
     */
    private String errorCode;

    /**
     * Um mapa opcional com detalhes de erros de validação (ex: "campo" : "mensagem de erro").
     */
    private Map<String, String> details;

    /**
     * Construtor padrão. Define o timestamp para o momento atual.
     */
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Construtor principal para criar uma resposta de erro básica.
     * @param status O código de status HTTP.
     * @param error O nome do erro HTTP.
     * @param message A mensagem descritiva.
     * @param path O caminho da requisição.
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this(); // Chama o construtor padrão para definir o timestamp
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // Getters e Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}