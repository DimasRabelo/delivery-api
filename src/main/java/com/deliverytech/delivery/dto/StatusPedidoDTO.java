package com.deliverytech.delivery.dto;


import jakarta.validation.constraints.NotNull;

public class StatusPedidoDTO {

    @NotNull(message = "Status é obrigatório")
    private String status; // ou use seu Enum StatusPedido se tiver

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
