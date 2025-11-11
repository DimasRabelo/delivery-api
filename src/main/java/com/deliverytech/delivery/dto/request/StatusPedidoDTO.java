package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) usado para atualizar o status de um pedido.
 *
 * @implNote O 'entregadorId' é opcional, mas torna-se obrigatório
 * se o 'status' enviado for "SAIU_PARA_ENTREGA".
 */
@Schema(description = "DTO para atualizar o status de um pedido")
public class StatusPedidoDTO {

    @Schema(description = "Novo status do pedido", example = "ENTREGUE", required = true)
    @NotNull(message = "Status é obrigatório")
    private String status;

    @Schema(description = "ID do entregador (Obrigatório ao mudar status para 'SAIU_PARA_ENTREGA')", example = "5", nullable = true)
    private Long entregadorId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getEntregadorId() {
        return entregadorId;
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId;
    }
}