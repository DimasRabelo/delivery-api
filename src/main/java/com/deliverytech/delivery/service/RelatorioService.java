package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.relatorio.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface de serviço para geração de relatórios do sistema.
 * Contém métodos para relatórios de vendas, produtos, clientes e pedidos.
 */
public interface RelatorioService {

    // ==========================================================
    // --- RELATÓRIOS DE VENDAS ---
    // ==========================================================
    /**
     * Gera relatório de vendas por restaurante dentro de um período.
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return Lista de RelatorioVendasDTO com total de vendas por restaurante
     */
    List<RelatorioVendasDTO> gerarRelatorioVendas(LocalDate inicio, LocalDate fim);

    // ==========================================================
    // --- RELATÓRIOS DE PRODUTOS ---
    // ==========================================================
    /**
     * Gera relatório dos produtos mais vendidos em um período.
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return Lista de RelatorioProdutosDTO com os produtos e quantidades vendidas
     */
    List<RelatorioProdutosDTO> gerarRelatorioProdutos(LocalDate inicio, LocalDate fim);

    // ==========================================================
    // --- RELATÓRIOS DE CLIENTES ---
    // ==========================================================
    /**
     * Gera relatório de clientes mais ativos em um período.
     * Contabiliza quantidade de pedidos, valor total gasto, etc.
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return Lista de RelatorioClientesDTO com informações de cada cliente
     */
    List<RelatorioClientesDTO> gerarRelatorioClientes(LocalDate inicio, LocalDate fim);

    // ==========================================================
    // --- RELATÓRIOS DE PEDIDOS ---
    // ==========================================================
    /**
     * Gera relatório geral de pedidos realizados no período.
     * @param inicio Data inicial do período
     * @param fim Data final do período
     * @return Lista de RelatorioPedidosDTO com detalhes dos pedidos
     */
    List<RelatorioPedidosDTO> gerarRelatorioPedidos(LocalDate inicio, LocalDate fim);
}
