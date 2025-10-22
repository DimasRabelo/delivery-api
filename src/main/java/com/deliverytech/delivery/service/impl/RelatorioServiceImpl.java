package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.relatorio.*;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    // ======================================================
    @Override
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
            throw new EntityNotFoundException("Nenhum pedido encontrado no período informado");
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
    // RELATÓRIO DE PRODUTOS MAIS VENDIDOS
    // ======================================================
    @Override
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
            throw new EntityNotFoundException("Nenhum pedido encontrado no período informado");
        }

        Map<Produto, Integer> contagemProdutos = new HashMap<>();

       for (Pedido pedido : pedidos) {
    for (ItemPedido item : pedido.getItens()) {
        if (item.getProduto() != null) {  // removeu item.getQuantidade() != null
            contagemProdutos.merge(item.getProduto(), item.getQuantidade(), Integer::sum);
        }
    }
}

        if (contagemProdutos.isEmpty()) {
            throw new EntityNotFoundException("Nenhum produto vendido no período informado");
        }

        return contagemProdutos.entrySet().stream()
                .map(entry -> {
                    Produto produto = entry.getKey();
                    Integer totalVendido = entry.getValue();
                    BigDecimal receitaTotal = produto.getPreco()
                            .multiply(BigDecimal.valueOf(totalVendido));
                    return new RelatorioProdutosDTO(
                            produto.getNome(),
                            produto.getCategoria(),
                            totalVendido,
                            receitaTotal
                    );
                })
                .sorted(Comparator.comparing(RelatorioProdutosDTO::getTotalVendido).reversed())
                .collect(Collectors.toList());
    }

    // ======================================================
    // RELATÓRIO DE CLIENTES COM MAIS PEDIDOS
    // ======================================================
    @Override
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
            throw new EntityNotFoundException("Nenhum pedido encontrado no período informado");
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
                            c.getNome(),
                            pedidosCliente.size(),
                            totalGasto
                    );
                })
                .sorted(Comparator.comparing(RelatorioClientesDTO::getTotalGasto).reversed())
                .collect(Collectors.toList());
    }

    // ======================================================
    // RELATÓRIO DE PEDIDOS (GERAL)
    // ======================================================
    @Override
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
            throw new EntityNotFoundException("Nenhum pedido encontrado no período informado");
        }

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
