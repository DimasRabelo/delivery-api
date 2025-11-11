package com.deliverytech.delivery.exception;

import org.springframework.http.HttpStatus;

/**
 * Exceção personalizada usada para representar erros de regra de negócio.
 * <p>
 * Deve ser lançada quando uma operação viola uma regra de negócio específica
 * (ex: "Estoque insuficiente", "Transição de status inválida").
 * Por padrão, retorna um status HTTP 400 (Bad Request).
 */
public class BusinessException extends RuntimeException {

    /**
     * Um código de erro interno opcional para rastreabilidade.
     */
    private String errorCode;

    /**
     * O HttpStatus que esta exceção deve retornar.
     * O padrão é 400 (BAD_REQUEST).
     */
    private HttpStatus status = HttpStatus.BAD_REQUEST;

    /**
     * Construtor básico.
     * @param message A mensagem de erro.
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Construtor com código de erro.
     * @param message A mensagem de erro.
     * @param errorCode O código de erro interno.
     */
    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Construtor com status HTTP customizado.
     * @param message A mensagem de erro.
     * @param status O HttpStatus a ser retornado (ex: CONFLICT, NOT_FOUND).
     */
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Construtor completo com status e código de erro.
     * @param message A mensagem de erro.
     * @param status O HttpStatus a ser retornado.
     * @param errorCode O código de erro interno.
     */
    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    /**
     * Construtor com causa (para encadeamento de exceções).
     * @param message A mensagem de erro.
     * @param cause A exceção original.
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construtor com causa e código de erro.
     * @param message A mensagem de erro.
     * @param errorCode O código de erro interno.
     * @param cause A exceção original.
     */
    public BusinessException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    // Getters e Setters
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