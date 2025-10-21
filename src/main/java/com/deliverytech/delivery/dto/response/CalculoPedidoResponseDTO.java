package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

// ==================================================
// DTO de resposta usado para retornar o resultado
// do cálculo do total de um pedido (sem salvar no banco)
// ==================================================
@Schema(description = "DTO de resposta do cálculo do total de um pedido")
public class CalculoPedidoResponseDTO {

    // --------------------------------------------------
    // Subtotal dos itens do pedido (somatória dos produtos)
    // @Schema → documenta no Swagger o campo com exemplo
    // BigDecimal → usado para valores monetários (precisão)
    // --------------------------------------------------
    @Schema(description = "Subtotal dos itens do pedido", example = "50.00")
    private BigDecimal subtotal;

    // --------------------------------------------------
    // Taxa de entrega associada ao pedido
    // Ex.: valor fixo ou calculado conforme o restaurante
    // --------------------------------------------------
    @Schema(description = "Taxa de entrega do pedido", example = "5.50")
    private BigDecimal taxaEntrega;

    // --------------------------------------------------
    // Valor total do pedido: subtotal + taxa de entrega
    // --------------------------------------------------
    @Schema(description = "Valor total do pedido (subtotal + taxa de entrega)", example = "55.50")
    private BigDecimal total;

    // =======================
    // GETTERS E SETTERS
    // =======================

    // Retorna o subtotal
    public BigDecimal getSubtotal() { return subtotal; }
    // Define o subtotal
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    // Retorna a taxa de entrega
    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    // Define a taxa de entrega
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    // Retorna o valor total
    public BigDecimal getTotal() { return total; }
    // Define o valor total
    public void setTotal(BigDecimal total) { this.total = total; }
}
