package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO usado para atualizar o status de um pedido.
 * Documentado no Swagger para que a API deixe claro quais campos são esperados.
 */
@Schema(description = "DTO para atualizar o status de um pedido")
public class StatusPedidoDTO {

    // ---------------------------------------------------
    // Campo status
    // Exemplo: "ENTREGUE"
    // Representa o novo status do pedido.
    // Pode ser uma String ou, idealmente, um Enum StatusPedido.
    // Obrigatório (annotated com @NotNull)
    // ---------------------------------------------------
    @Schema(description = "Novo status do pedido", example = "ENTREGUE", required = true)
    @NotNull(message = "Status é obrigatório")
    private String status;

    // =======================
    // GETTER E SETTER
    // =======================
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
