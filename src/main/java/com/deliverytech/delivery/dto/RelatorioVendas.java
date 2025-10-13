package com.deliverytech.delivery.dto;

import java.math.BigDecimal;

public interface RelatorioVendas {
    String getNomeRestaurante();
    BigDecimal getTotalVendas();
    Long getQuantidadePedidos();
}