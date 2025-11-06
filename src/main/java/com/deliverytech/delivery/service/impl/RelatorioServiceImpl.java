package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.relatorio.*;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioServiceImpl implements RelatorioService {

    @Autowired
    private PedidoRepository pedidoRepository;

    // ======================================================
    // RELATÓRIO DE VENDAS POR RESTAURANTE
    // (Este método estava OK)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioVendasDTO> gerarRelatorioVendas(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getRestaurante() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Restaurante, List<Pedido>> pedidosPorRestaurante =
                pedidos.stream().collect(Collectors.groupingBy(Pedido::getRestaurante));

        return pedidosPorRestaurante.entrySet().stream()
                .map(entry -> {
                    Restaurante r = entry.getKey();
                    List<Pedido> pedidosRestaurante = entry.getValue();
                    BigDecimal totalVendas = pedidosRestaurante.stream()
                            .map(Pedido::getValorTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RelatorioVendasDTO(
                            r.getNome(),
                            pedidosRestaurante.size(),
                            totalVendas
                    );
                })
                .collect(Collectors.toList());
    }

    // ======================================================
    // RELATÓRIO DE PRODUTOS MAIS VENDIDOS (MÉTODO REFATORADO)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioProdutosDTO> gerarRelatorioProdutos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getItens() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // --- LÓGICA REFATORADA ---
        Map<Long, Integer> contagemProdutos = new HashMap<>(); // (ID do Produto -> Qtd Total)
        Map<Long, BigDecimal> receitaProdutos = new HashMap<>(); // (ID do Produto -> Receita Total)
        Map<Long, Produto> produtosMap = new HashMap<>(); // (ID do Produto -> Objeto Produto)

        for (Pedido pedido : pedidos) {
            if (pedido.getItens() != null) {
                for (ItemPedido item : pedido.getItens()) {
                    if (item != null && item.getProduto() != null && item.getQuantidade() != null) {
                        Long produtoId = item.getProduto().getId();
                        
                        // 1. Soma a quantidade vendida (como antes)
                        contagemProdutos.merge(produtoId, item.getQuantidade(), Integer::sum);
                        
                        // 2. SOMA A RECEITA REAL (do ItemPedido, que inclui opcionais)
                        receitaProdutos.merge(produtoId, item.getSubtotal(), BigDecimal::add);
                        
                        produtosMap.put(produtoId, item.getProduto());
                    }
                }
            }
        }

        if (contagemProdutos.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. Monta a resposta usando a RECEITA REAL (não o preço base)
        return contagemProdutos.entrySet().stream()
                .map(entry -> {
                    Long produtoId = entry.getKey();
                    Produto produto = produtosMap.get(produtoId);
                    Integer totalVendido = entry.getValue();
                    
                    // CORREÇÃO: Pega a receita total somada (que já inclui os opcionais)
                    BigDecimal receitaTotal = receitaProdutos.getOrDefault(produtoId, BigDecimal.ZERO);
                    
                    // (A linha 'produto.getPreco()' foi removida, corrigindo o erro)

                    return new RelatorioProdutosDTO(
                            produto.getNome(),
                            produto.getCategoria(),
                            totalVendido,
                            receitaTotal // <-- Usa a receita correta
                    );
                })
                .sorted(Comparator.comparing(RelatorioProdutosDTO::getTotalVendido).reversed())
                .collect(Collectors.toList());
    }

    // ======================================================
    // RELATÓRIO DE CLIENTES COM MAIS PEDIDOS
    // (Este método estava OK - 'c.getNome()' está correto na Decisão 1)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioClientesDTO> gerarRelatorioClientes(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getCliente() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Cliente, List<Pedido>> pedidosPorCliente =
                pedidos.stream().collect(Collectors.groupingBy(Pedido::getCliente));

        return pedidosPorCliente.entrySet().stream()
                .map(entry -> {
                    Cliente c = entry.getKey();
                    List<Pedido> pedidosCliente = entry.getValue();
                    BigDecimal totalGasto = pedidosCliente.stream()
                            .map(Pedido::getValorTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RelatorioClientesDTO(
                            c.getNome(), // <-- Está CORRETO (Nome está em Cliente)
                            pedidosCliente.size(),
                            totalGasto
                    );
                })
                .sorted(Comparator.comparing(RelatorioClientesDTO::getTotalGasto).reversed())
                .collect(Collectors.toList());
    }

    // ======================================================
    // RELATÓRIO DE PEDIDOS (GERAL)
    // (Este método estava OK - 'p.getCliente().getNome()' está correto na Decisão 1)
    // ======================================================
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioPedidosDTO> gerarRelatorioPedidos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getCliente() != null &&
                        p.getRestaurante() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        return pedidos.stream()
                .map(p -> new RelatorioPedidosDTO(
                        p.getId(),
                        p.getNumeroPedido(),
                        p.getRestaurante().getNome(),
                        p.getCliente().getNome(), // <-- Está CORRETO
                        p.getValorTotal(),
                        p.getStatus(),
                        p.getDataPedido()
                ))
                .collect(Collectors.toList());
    }
}