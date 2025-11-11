package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.deliverytech.delivery.dto.request.ItemPedidoDTO;

@Schema(description = "DTO de resposta com os dados de um pedido")
public class PedidoResponseDTO {

    // --- Identificação ---
    @Schema(description = "ID do pedido", example = "1")
    private Long id;

    // --- Cliente ---
    @Schema(description = "ID do cliente", example = "1")
    private Long clienteId;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String clienteNome;

    // --- Restaurante ---
    @Schema(description = "ID do restaurante", example = "2")
    private Long restauranteId;

    @Schema(description = "Nome do restaurante", example = "Pizza Express")
    private String restauranteNome;

    // --- Detalhes da Entrega e Status ---
    @Schema(description = "Endereço de entrega", example = "Rua das Flores, 123")
    private String enderecoEntrega;

    @Schema(description = "Status do pedido", example = "EM_PREPARO")
    private String status; 

    @Schema(description = "Valor total do pedido", example = "55.50")
    private BigDecimal total;

    @Schema(description = "Lista de itens do pedido")
    private List<ItemPedidoDTO> itens;
    
    // ==========================================================
    // --- CAMPOS FALTANTES (A CORREÇÃO) ---
    // ==========================================================
    @Schema(description = "ID do entregador atribuído (se houver)", example = "6", nullable = true)
    private Long entregadorId;
    
    @Schema(description = "Nome/Email do entregador (se houver)", example = "carlos@entrega.com", nullable = true)
    private String entregadorNome;

    @Schema(description = "Data e hora que o pedido foi feito", example = "2025-11-09T08:30:00")
    private LocalDateTime dataPedido;
    // ==========================================================
    // FIM DA CORREÇÃO
    // ==========================================================


    // ===================================================
    // GETTERS E SETTERS
    // ===================================================
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

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    @Schema(description = "Valor total dos itens (sem a entrega)", example = "50.00")
    private BigDecimal subtotal;

    @Schema(description = "Valor da taxa de entrega", example = "5.00")
    private BigDecimal taxaEntrega;

    // ==========================================================
    // --- GETTERS/SETTERS ADICIONADOS ---
    // ==========================================================
    public Long getEntregadorId() { return entregadorId; }
    public void setEntregadorId(Long entregadorId) { this.entregadorId = entregadorId; }

    public String getEntregadorNome() { return entregadorNome; }
    public void setEntregadorNome(String entregadorNome) { this.entregadorNome = entregadorNome; }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxaEntrega() {
        return taxaEntrega;
    }

    public void setTaxaEntrega(BigDecimal taxaEntrega) {
        this.taxaEntrega = taxaEntrega;
    }
}