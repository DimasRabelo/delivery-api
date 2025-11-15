package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
// Importação do Lombok para uso opcional, mantendo o código conciso.
// Caso opte por usar @Data em vez de getters/setters explícitos, remova os métodos ao final.

/**
 * DTO (Data Transfer Object) de resposta que encapsula
 * todos os dados de um Pedido a serem exibidos ao cliente após uma consulta.
 */
@Schema(description = "DTO de resposta com os dados completos de um pedido")
public class PedidoResponseDTO {

    // --- Identificação e Entidades Relacionadas ---

    @Schema(description = "ID único do pedido", example = "1")
    private Long id;

    @Schema(description = "ID do cliente que realizou o pedido", example = "1")
    private Long clienteId;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String clienteNome;

    @Schema(description = "ID do restaurante que preparará o pedido", example = "2")
    private Long restauranteId;

    @Schema(description = "Nome do restaurante", example = "Pizza Express")
    private String restauranteNome;

    // --- Detalhes da Entrega e Status ---

    @Schema(description = "Endereço de entrega completo formatado", example = "Rua das Flores, 123")
    private String enderecoEntrega;

    @Schema(description = "Status atual do pedido (ex: PENDENTE, EM_PREPARO, ENTREGUE)", example = "EM_PREPARO")
    private String status; 

    // --- Entregador (Opcional) ---
    
    @Schema(description = "ID do entregador atribuído (se houver)", example = "6", nullable = true)
    private Long entregadorId;
    
    @Schema(description = "Nome/Email do entregador (se houver)", example = "carlos@entrega.com", nullable = true)
    private String entregadorNome;

    // --- Detalhes de Valores e Tempo ---

    @Schema(description = "Data e hora que o pedido foi feito", example = "2025-11-09T08:30:00")
    private LocalDateTime dataPedido;
    
    @Schema(description = "Valor total dos itens (sem a taxa de entrega)", example = "50.00")
    private BigDecimal subtotal;

    @Schema(description = "Valor da taxa de entrega", example = "5.00")
    private BigDecimal taxaEntrega;

    @Schema(description = "Valor total final do pedido (subtotal + taxaEntrega)", example = "55.50")
    private BigDecimal total;

    // --- Itens do Pedido ---

    @Schema(description = "Lista de itens detalhados do pedido, usando ItemPedidoResponseDTO")
   private List<ItemPedidoResponseDTO> itens;
    
    // --- Getters e Setters (Métodos de Acesso) ---
    
    // Métodos para acesso e manipulação dos campos (necessários por não usar @Data do Lombok)

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

    public List<ItemPedidoResponseDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoResponseDTO> itens) { this.itens = itens; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }
    
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