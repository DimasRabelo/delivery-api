package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull; // (ou javax.validation.constraints.NotNull)

/**
 * DTO usado para atualizar o status de um pedido.
 * Documentado no Swagger para que a API deixe claro quais campos são esperados.
 */
@Schema(description = "DTO para atualizar o status de um pedido")
public class StatusPedidoDTO {

    // ---------------------------------------------------
    // Campo status
    // ---------------------------------------------------
    @Schema(description = "Novo status do pedido", example = "ENTREGUE", required = true)
    @NotNull(message = "Status é obrigatório")
    private String status;

    // ==========================================================
    // --- CAMPO FALTANDO (ESTA É A CORREÇÃO) ---
    // ==========================================================
    @Schema(description = "ID do entregador (Obrigatório ao mudar status para 'SAIU_PARA_ENTREGA')", example = "5", nullable = true)
    private Long entregadorId;
    // ==========================================================


    // =======================
    // GETTER E SETTER
    // =======================
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getEntregadorId() {
        return entregadorId; // <-- Agora 'entregadorId' existe
    }

    public void setEntregadorId(Long entregadorId) {
        this.entregadorId = entregadorId; // <-- Agora 'this.entregadorId' existe
    }
}