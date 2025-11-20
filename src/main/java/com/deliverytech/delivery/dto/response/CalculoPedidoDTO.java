package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import com.deliverytech.delivery.dto.request.ItemPedidoDTO;

/**
 * DTO (Data Transfer Object) que encapsula os dados necessários
 * para calcular o total de um pedido (itens + taxa) sem
 * a necessidade de salvá-lo no banco de dados.
 */
@Schema(description = "DTO para cálculo do total de um pedido sem persistir no banco")
public class CalculoPedidoDTO {

    @Schema(description = "ID do restaurante onde o pedido será calculado", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "ID do restaurante é obrigatório")
    private Long restauranteId;

    @Schema(description = "Lista de itens para cálculo do pedido", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid // Valida os DTOs (ItemPedidoDTO) dentro da lista
    private List<ItemPedidoDTO> itens = new ArrayList<>();

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}