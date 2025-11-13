package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.relatorio.*;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository; // Adicionado
import com.deliverytech.delivery.repository.auth.UsuarioRepository; // Adicionado
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

    // --- DEPENDÊNCIAS ADICIONADAS PARA O DASHBOARD ---
    @Autowired 
    private UsuarioRepository usuarioRepository; 
    @Autowired 
    private RestauranteRepository restauranteRepository;
    // ---------------------------------------------------

    // ==========================================================
    // --- MÉTODOS DO DASHBOARD (Contagem NATIVA) ---
    // ==========================================================

   @Override
    @Transactional(readOnly = true)
    public Long contarTotalUsuarios() {
        // CHAMA O MÉTODO NATIVO CORRETO (Implementado no Repository)
        return usuarioRepository.contarTodosUsuariosNative(); 
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long contarTotalRestaurantes() {
        // CHAMA O MÉTODO NATIVO CORRETO (Implementado no Repository)
        return restauranteRepository.contarTodosRestaurantesNative(); 
    }

   @Override
    public BigDecimal calcularVendasUltimos30Dias() {
        // 1. Define o período: Hoje e 30 dias atrás
        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        LocalDateTime agora = LocalDateTime.now();
        
        // 2. Chama o método do Repositório
        BigDecimal vendas = pedidoRepository.calcularVendasPorPeriodo(trintaDiasAtras, agora);
        
        // 3. Retorna o valor real (ou 0.00 se o resultado for NULL, o que é comum quando não há vendas)
        return vendas != null ? vendas : BigDecimal.ZERO; 
    }
    // ==========================================================
    // --- RELATÓRIO DE VENDAS (Existente) ---
    // ==========================================================
    
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
     * Gera um relatório de produtos mais vendidos, calculando a receita.
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
        Map<Long, Produto> produtosMap = new HashMap<>(); // ID do Produto -> Objeto Produto

        // Itera sobre todos os itens de todos os pedidos filtrados
        for (Pedido pedido : pedidos) {
            if (pedido.getItens() != null) {
                for (ItemPedido item : pedido.getItens()) {
                    if (item != null && item.getProduto() != null && item.getQuantidade() != null) {
                        Long produtoId = item.getProduto().getId();
                        
                        contagemProdutos.merge(produtoId, item.getQuantidade(), Integer::sum);
                        receitaProdutos.merge(produtoId, item.getSubtotal(), BigDecimal::add);
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
                    
                    BigDecimal receitaTotal = receitaProdutos.getOrDefault(produtoId, BigDecimal.ZERO);
                    
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
                .sorted(Comparator.comparing(RelatorioClientesDTO::getTotalGasto).reversed())
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