package com.deliverytech.delivery.exception;

/**
 * Exceção personalizada usada para representar erros de negócio.
 * Inclui um código de erro opcional para identificação.
 */
public class BusinessException extends RuntimeException {

    private String errorCode;

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
}
