package com.deliverytech.delivery.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção personalizada usada para representar erros de negócio.
 * Inclui código de erro e status HTTP opcionais para controle preciso de respostas.
 */
public class BusinessException extends RuntimeException {

    private String errorCode;
    private HttpStatus status = HttpStatus.BAD_REQUEST; // padrão 400

    // ------------------------------------------------------
    // Construtores
    // ------------------------------------------------------

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // ------------------------------------------------------
    // Getters e Setters
    // ------------------------------------------------------
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
