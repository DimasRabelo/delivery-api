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

    /**
     * Gera um relatório de vendas agregado por restaurante dentro de um período.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioVendasDTO> gerarRelatorioVendas(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        // Filtra os pedidos relevantes em memória
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getRestaurante() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // Agrupa os pedidos por restaurante
        Map<Restaurante, List<Pedido>> pedidosPorRestaurante =
                pedidos.stream().collect(Collectors.groupingBy(Pedido::getRestaurante));

        // Mapeia os dados agrupados para o DTO de relatório
        return pedidosPorRestaurante.entrySet().stream()
                .map(entry -> {
                    Restaurante r = entry.getKey();
                    List<Pedido> pedidosRestaurante = entry.getValue();
                    // Soma o valor total dos pedidos para aquele restaurante
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

    /**
     * Gera um relatório de produtos mais vendidos, calculando a receita
     * real (incluindo opcionais) de cada item.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioProdutosDTO> gerarRelatorioProdutos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        // Filtra os pedidos relevantes em memória
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getItens() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // Mapas para agregar os dados dos produtos
        Map<Long, Integer> contagemProdutos = new HashMap<>(); // ID do Produto -> Qtd Total
        Map<Long, BigDecimal> receitaProdutos = new HashMap<>(); // ID do Produto -> Receita Total (com opcionais)
        Map<Long, Produto> produtosMap = new HashMap<>(); // ID do Produto -> Objeto Produto (para dados como nome)

        // Itera sobre todos os itens de todos os pedidos filtrados
        for (Pedido pedido : pedidos) {
            if (pedido.getItens() != null) {
                for (ItemPedido item : pedido.getItens()) {
                    if (item != null && item.getProduto() != null && item.getQuantidade() != null) {
                        Long produtoId = item.getProduto().getId();
                        
                        // Soma a quantidade total vendida do produto
                        contagemProdutos.merge(produtoId, item.getQuantidade(), Integer::sum);
                        
                        // Soma a receita real (subtotal do item, que inclui opcionais)
                        receitaProdutos.merge(produtoId, item.getSubtotal(), BigDecimal::add);
                        
                        // Armazena a referência ao produto para buscar o nome/categoria
                        produtosMap.put(produtoId, item.getProduto());
                    }
                }
            }
        }

        if (contagemProdutos.isEmpty()) {
            return Collections.emptyList();
        }

        // Monta a lista de DTOs de resposta
        return contagemProdutos.entrySet().stream()
                .map(entry -> {
                    Long produtoId = entry.getKey();
                    Produto produto = produtosMap.get(produtoId);
                    Integer totalVendido = entry.getValue();
                    
                    // Busca a receita total real que foi somada
                    BigDecimal receitaTotal = receitaProdutos.getOrDefault(produtoId, BigDecimal.ZERO);
                    
                    return new RelatorioProdutosDTO(
                            produto.getNome(),
                            produto.getCategoria(),
                            totalVendido,
                            receitaTotal // Usa a receita real calculada
                    );
                })
                .sorted(Comparator.comparing(RelatorioProdutosDTO::getTotalVendido).reversed()) // Ordena por mais vendidos
                .collect(Collectors.toList());
    }

    /**
     * Gera um relatório de clientes que mais gastaram no período.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioClientesDTO> gerarRelatorioClientes(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        // Filtra os pedidos relevantes em memória
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getCliente() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // Agrupa os pedidos por cliente
        Map<Cliente, List<Pedido>> pedidosPorCliente =
                pedidos.stream().collect(Collectors.groupingBy(Pedido::getCliente));

        // Mapeia os dados agrupados para o DTO de relatório
        return pedidosPorCliente.entrySet().stream()
                .map(entry -> {
                    Cliente c = entry.getKey();
                    List<Pedido> pedidosCliente = entry.getValue();
                    // Soma o valor total gasto por aquele cliente
                    BigDecimal totalGasto = pedidosCliente.stream()
                            .map(Pedido::getValorTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new RelatorioClientesDTO(
                            c.getNome(),
                            pedidosCliente.size(),
                            totalGasto
                    );
                })
                .sorted(Comparator.comparing(RelatorioClientesDTO::getTotalGasto).reversed()) // Ordena por quem gastou mais
                .collect(Collectors.toList());
    }

    /**
     * Gera um relatório simples (lista) de todos os pedidos no período.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioPedidosDTO> gerarRelatorioPedidos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        // Filtra os pedidos relevantes em memória
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

        // Apenas mapeia os pedidos filtrados para o DTO
        return pedidos.stream()
                .map(p -> new RelatorioPedidosDTO(
                        p.getId(),
                        p.getNumeroPedido(),
                        p.getRestaurante().getNome(),
                        p.getCliente().getNome(),
                        p.getValorTotal(),
                        p.getStatus(),
                        p.getDataPedido()
                ))
                .collect(Collectors.toList());
    }
}