package com.deliverytech.delivery.dto.relatorio;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para agrupar os dados do relatório
 * de clientes mais ativos (que mais gastaram).
 */
@Schema(description = "Relatório dos clientes mais ativos")
public class RelatorioClientesDTO {

    @Schema(description = "Nome do cliente")
    private String clienteNome;

    @Schema(description = "Quantidade de pedidos realizados")
    private int totalPedidos;

    @Schema(description = "Valor total gasto pelo cliente")
    private BigDecimal totalGasto;

    public RelatorioClientesDTO() {}

    public RelatorioClientesDTO(String clienteNome, int totalPedidos, BigDecimal totalGasto) {
        this.clienteNome = clienteNome;
        this.totalPedidos = totalPedidos;
        this.totalGasto = totalGasto;
    }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public int getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(int totalPedidos) { this.totalPedidos = totalPedidos; }

    public BigDecimal getTotalGasto() { return totalGasto; }
    public void setTotalGasto(BigDecimal totalGasto) { this.totalGasto = totalGasto; }
}