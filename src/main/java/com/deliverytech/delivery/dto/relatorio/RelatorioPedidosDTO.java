package com.deliverytech.delivery.dto.relatorio;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) que representa uma linha individual
 * no relatório de pedidos por período.
 */
@Schema(description = "Relatório de pedidos por período")
public class RelatorioPedidosDTO {

    @Schema(description = "ID do pedido")
    private Long pedidoId;

    @Schema(description = "Número do pedido")
    private String numeroPedido;

    @Schema(description = "Nome do restaurante")
    private String restauranteNome;

    @Schema(description = "Nome do cliente")
    private String clienteNome;

    @Schema(description = "Valor total do pedido")
    private BigDecimal valorTotal;

    @Schema(description = "Status do pedido")
    private String status;

    @Schema(description = "Data e hora do pedido")
    private LocalDateTime dataPedido;

    public RelatorioPedidosDTO() {}

    public RelatorioPedidosDTO(Long pedidoId, String numeroPedido, String restauranteNome,
                               String clienteNome, BigDecimal valorTotal, Object status, LocalDateTime dataPedido) {
        this.pedidoId = pedidoId;
        this.numeroPedido = numeroPedido;
        this.restauranteNome = restauranteNome;
        this.clienteNome = clienteNome;
        this.valorTotal = valorTotal;
        // Converte o status (provavelmente um Enum) para String
        this.status = status != null ? status.toString() : null;
        this.dataPedido = dataPedido;
    }

    // getters e setters
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public String getRestauranteNome() { return restauranteNome; }
    public void setRestauranteNome(String restauranteNome) { this.restauranteNome = restauranteNome; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }
}