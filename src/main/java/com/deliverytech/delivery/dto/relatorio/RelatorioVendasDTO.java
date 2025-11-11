package com.deliverytech.delivery.dto.relatorio;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Relatório de vendas por restaurante")
public class RelatorioVendasDTO {

    @Schema(description = "Nome do restaurante")
    private String restauranteNome;

    @Schema(description = "Quantidade total de pedidos realizados")
    private int totalPedidos; // int porque service usa size()

    @Schema(description = "Valor total das vendas")
    private BigDecimal totalVendas;

    public RelatorioVendasDTO() {}

    public RelatorioVendasDTO(String restauranteNome, int totalPedidos, BigDecimal totalVendas) {
        this.restauranteNome = restauranteNome;
        this.totalPedidos = totalPedidos;
        this.totalVendas = totalVendas;
    }

    public String getRestauranteNome() { return restauranteNome; }
    public void setRestauranteNome(String restauranteNome) { this.restauranteNome = restauranteNome; }

    public int getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(int totalPedidos) { this.totalPedidos = totalPedidos; }

    public BigDecimal getTotalVendas() { return totalVendas; }
    public void setTotalVendas(BigDecimal totalVendas) { this.totalVendas = totalVendas; }
}
