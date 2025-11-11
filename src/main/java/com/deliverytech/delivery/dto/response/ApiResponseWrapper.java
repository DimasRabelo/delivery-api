package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema; 
import java.time.LocalDateTime;

/**
 * Um wrapper genérico padronizado para todas as respostas da API.
 * Isso garante que o front-end sempre receba um JSON com a mesma
 * estrutura (success, data, message, timestamp).
 *
 * @param <T> O tipo de dado (DTO) que está sendo retornado no campo 'data'.
 */
@Schema(description = "Wrapper padrão para respostas da API")
public class ApiResponseWrapper<T> {

    /**
     * Indica se a operação foi bem-sucedida.
     */
    @Schema(description = "Indica se a operação foi bem-sucedida", example = "true")
    private boolean success;

    /**
     * Os dados da resposta (ex: um ProdutoResponseDTO, uma Lista de Pedidos, etc.).
     * Pode ser nulo em caso de erro.
     */
    @Schema(description = "Dados da resposta")
    private T data;

    /**
     * Uma mensagem descritiva (ex: "Produto criado com sucesso" ou "Cliente não encontrado").
     */
    @Schema(description = "Mensagem descritiva", example = "Operação realizada com sucesso")
    private String message;

    /**
     * Data e hora em que a resposta foi gerada, útil para logs e rastreabilidade.
     */
    @Schema(description = "Timestamp da resposta", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    /**
     * Construtor padrão. Inicializa o timestamp.
     */
    public ApiResponseWrapper() { 
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Construtor principal.
     * @param success true se a operação foi bem-sucedida.
     * @param data Os dados de resposta.
     * @param message A mensagem descritiva.
     */
    public ApiResponseWrapper(boolean success, T data, String message) { 
        this.success = success;
        this.data = data; 
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Método helper estático para criar uma resposta de sucesso.
     * @param data Os dados de resposta.
     * @param message A mensagem de sucesso.
     * @param <T> O tipo dos dados.
     * @return Um ApiResponseWrapper com success=true.
     */
    public static <T> ApiResponseWrapper<T> success(T data, String message) { 
        return new ApiResponseWrapper<>(true, data, message);
    }

    /**
     * Método helper estático para criar uma resposta de erro.
     * @param message A mensagem de erro.
     * @param <T> O tipo dos dados (será nulo).
     * @return Um ApiResponseWrapper com success=false e data=null.
     */
    public static <T> ApiResponseWrapper<T> error(String message) { 
        return new ApiResponseWrapper<>(false, null, message);
    }

    // Getters e Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}