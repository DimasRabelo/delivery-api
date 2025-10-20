package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

// =======================================================
// DTO de resposta de um pedido, enviado pelo backend
// =======================================================
@Schema(description = "DTO de resposta com os dados de um pedido")
public class PedidoResponseDTO {

    // ---------------------------------------------------
    // ID do pedido
    // Exemplo no Swagger: 1
    // ---------------------------------------------------
    @Schema(description = "ID do pedido", example = "1")
    private Long id;

    // ---------------------------------------------------
    // ID do cliente que realizou o pedido
    // Exemplo: 1
    // ---------------------------------------------------
    @Schema(description = "ID do cliente", example = "1")
    private Long clienteId;

    // ---------------------------------------------------
    // Nome do cliente
    // Exemplo: "João Silva"
    // ---------------------------------------------------
    @Schema(description = "Nome do cliente", example = "João Silva")
    private String clienteNome;

    // ---------------------------------------------------
    // ID do restaurante
    // Exemplo: 2
    // ---------------------------------------------------
    @Schema(description = "ID do restaurante", example = "2")
    private Long restauranteId;

    // ---------------------------------------------------
    // Nome do restaurante
    // Exemplo: "Pizza Express"
    // ---------------------------------------------------
    @Schema(description = "Nome do restaurante", example = "Pizza Express")
    private String restauranteNome;

    // ---------------------------------------------------
    // Endereço completo de entrega
    // Exemplo: "Rua das Flores, 123"
    // ---------------------------------------------------
    @Schema(description = "Endereço de entrega", example = "Rua das Flores, 123")
    private String enderecoEntrega;

    // ---------------------------------------------------
    // Status do pedido (EM_PREPARO, ENTREGUE, CANCELADO...)
    // Exemplo: "EM_PREPARO"
    // ---------------------------------------------------
    @Schema(description = "Status do pedido", example = "EM_PREPARO")
    private String status;

    // ---------------------------------------------------
    // Valor total do pedido
    // Exemplo: 55.50
    // ---------------------------------------------------
    @Schema(description = "Valor total do pedido", example = "55.50")
    private BigDecimal total;

    // ---------------------------------------------------
    // Lista de itens do pedido
    // Cada item é representado pelo ItemPedidoDTO
    // ---------------------------------------------------
    @Schema(description = "Lista de itens do pedido")
    private List<ItemPedidoDTO> itens;

    // =======================
    // GETTERS E SETTERS
    // =======================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getRestauranteNome() { return restauranteNome; }
    public void setRestauranteNome(String restauranteNome) { this.restauranteNome = restauranteNome; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}
