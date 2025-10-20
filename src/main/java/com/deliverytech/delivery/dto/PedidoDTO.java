package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

// =============================================
// DTO para criar um pedido na API
// =============================================
@Schema(description = "Dados para criação de um pedido")
public class PedidoDTO {

    // -------------------------------------------------
    // ID do cliente que está realizando o pedido
    // Obrigatório, validado pelo @NotNull
    // Documentado no Swagger com exemplo "1"
    // -------------------------------------------------
    @Schema(description = "ID do cliente que está realizando o pedido", example = "1", required = true)
    @NotNull(message = "Cliente é obrigatório")
    private Long clienteId;

    // -------------------------------------------------
    // ID do restaurante onde o pedido será realizado
    // Obrigatório, validado pelo @NotNull
    // Documentado no Swagger com exemplo "2"
    // -------------------------------------------------
    @Schema(description = "ID do restaurante onde o pedido será realizado", example = "2", required = true)
    @NotNull(message = "Restaurante é obrigatório")
    private Long restauranteId;

    // -------------------------------------------------
    // Endereço completo de entrega
    // Obrigatório, validado pelo @NotBlank
    // Tamanho máximo 200 caracteres (@Size)
    // Documentado no Swagger com exemplo
    // -------------------------------------------------
    @Schema(description = "Endereço completo para entrega", example = "Rua das Flores, 123 - Centro", required = true)
    @NotBlank(message = "Endereço de entrega é obrigatório")
    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    private String enderecoEntrega;

    // -------------------------------------------------
    // Lista de itens do pedido
    // Obrigatória (@NotEmpty) e cada item validado (@Valid)
    // Documentada no Swagger
    // -------------------------------------------------
    @Schema(description = "Lista de itens do pedido", required = true)
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid
    private List<ItemPedidoDTO> itens = new ArrayList<>();

    // =======================
    // GETTERS E SETTERS
    // =======================
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}
