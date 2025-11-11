package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) de resposta que retorna o resultado
 * do cálculo do total de um pedido (subtotal + taxa + total),
 * geralmente usado pelo endpoint de cálculo.
 */
@Schema(description = "DTO de resposta do cálculo do total de um pedido")
public class CalculoPedidoResponseDTO {

    @Schema(description = "Subtotal dos itens do pedido", example = "50.00")
    private BigDecimal subtotal;

    @Schema(description = "Taxa de entrega do pedido", example = "5.50")
    private BigDecimal taxaEntrega;

    @Schema(description = "Valor total do pedido (subtotal + taxa de entrega)", example = "55.50")
    private BigDecimal total;

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}