package com.deliverytech.delivery.dto;

import java.math.BigDecimal;

public class CalculoPedidoResponseDTO {

    private BigDecimal subtotal;
    private BigDecimal taxaEntrega;
    private BigDecimal total;

    // Getters e Setters
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
