package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.relatorio.*;
import java.time.LocalDate;
import java.util.List;

public interface RelatorioService {

    // Relatório de vendas por restaurante dentro de um período
    List<RelatorioVendasDTO> gerarRelatorioVendas(LocalDate inicio, LocalDate fim);

    // Relatório de produtos mais vendidos em um período
    List<RelatorioProdutosDTO> gerarRelatorioProdutos(LocalDate inicio, LocalDate fim);

    // Relatório de clientes mais ativos (quantidade de pedidos, valor total gasto etc.)
    List<RelatorioClientesDTO> gerarRelatorioClientes(LocalDate inicio, LocalDate fim);

    // Relatório geral de pedidos realizados no período
    List<RelatorioPedidosDTO> gerarRelatorioPedidos(LocalDate inicio, LocalDate fim);
}
