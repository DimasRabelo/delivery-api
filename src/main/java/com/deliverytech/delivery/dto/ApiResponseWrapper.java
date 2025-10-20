package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema; 
import java.time.LocalDateTime;

// ============================================
// Classe genérica para padronizar respostas da API
// ============================================
@Schema(description = "Wrapper padrão para respostas da API")
public class ApiResponseWrapper<T> {

    // ------------------------------------------------
    // Indica se a operação foi bem-sucedida (true/false)
    // ------------------------------------------------
    @Schema(description = "Indica se a operação foi bem-sucedida", example = "true")
    private boolean success;

    // ------------------------------------------------
    // Guarda os dados retornados pela API (pode ser qualquer tipo)
    // Ex: ProdutoResponseDTO, ClienteResponseDTO, lista de pedidos, etc.
    // ------------------------------------------------
    @Schema(description = "Dados da resposta")
    private T data;

    // ------------------------------------------------
    // Mensagem descritiva sobre a operação
    // Ex: "Produto criado com sucesso", "Cliente não encontrado"
    // ------------------------------------------------
    @Schema(description = "Mensagem descritiva", example = "Operação realizada com sucesso")
    private String message;

    // ------------------------------------------------
    // Data e hora em que a resposta foi gerada
    // Útil para logs, auditoria e rastreabilidade
    // ------------------------------------------------
    @Schema(description = "Timestamp da resposta", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    // ------------------------------------------------
    // Construtor padrão: inicializa apenas o timestamp
    // ------------------------------------------------
    public ApiResponseWrapper() { 
        this.timestamp = LocalDateTime.now();
    }

    // ------------------------------------------------
    // Construtor principal: define sucesso, dados e mensagem
    // Também inicializa o timestamp
    // ------------------------------------------------
    public ApiResponseWrapper(boolean success, T data, String message) { 
        this.success = success;
        this.data = data; 
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // ------------------------------------------------
    // Método estático para criar respostas de sucesso
    // Ex: ApiResponseWrapper.success(produto, "Produto criado")
    // ------------------------------------------------
    public static <T> ApiResponseWrapper<T> success(T data, String message) { 
        return new ApiResponseWrapper<>(true, data, message);
    }

    // ------------------------------------------------
    // Método estático para criar respostas de erro
    // Ex: ApiResponseWrapper.error("Produto não encontrado")
    // ------------------------------------------------
    public static <T> ApiResponseWrapper<T> error(String message) { 
        return new ApiResponseWrapper<>(false, null, message);
    }

    // =======================
    // GETTERS E SETTERS
    // =======================
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
