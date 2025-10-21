
package com.deliverytech.delivery.dto.response;

import com.deliverytech.delivery.dto.ItemPedidoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

// ============================================
// DTO usado para enviar informações de um pedido
// para cálculo do total sem salvar no banco
// ============================================
@Schema(description = "DTO para cálculo do total de um pedido sem persistir no banco")
public class CalculoPedidoDTO {

    // ------------------------------------------------
    // ID do restaurante para o qual o cálculo será feito
    // @Schema → documenta no Swagger o que é esse campo
    // @NotNull → validação para garantir que não seja nulo
    // ------------------------------------------------
    @Schema(description = "ID do restaurante onde o pedido será calculado", example = "2", required = true)
    @NotNull(message = "ID do restaurante é obrigatório")
    private Long restauranteId;

    // ------------------------------------------------
    // Lista de itens do pedido a ser calculado
    // @NotEmpty → valida que a lista não pode estar vazia
    // @Valid → valida os itens internos da lista (ItemPedidoDTO)
    // ------------------------------------------------
    @Schema(description = "Lista de itens para cálculo do pedido", required = true)
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid
    private List<ItemPedidoDTO> itens = new ArrayList<>();

    // =======================
    // GETTERS E SETTERS
    // =======================

    // Retorna o ID do restaurante
    public Long getRestauranteId() { return restauranteId; }
    // Define o ID do restaurante
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    // Retorna a lista de itens do pedido
    public List<ItemPedidoDTO> getItens() { return itens; }
    // Define a lista de itens do pedido
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}
