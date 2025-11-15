package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.relatorio.*;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository; 
import com.deliverytech.delivery.repository.auth.UsuarioRepository; 
import com.deliverytech.delivery.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementação do Serviço de Relatórios, focado em agregação de dados
 * para Dashboards e consultas históricas.
 */
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

    /**
     * Retorna a contagem total de usuários (contagem simples do repositório).
     * Idealmente, utiliza uma query nativa ou JPQL otimizada.
     */
   @Override
    @Transactional(readOnly = true)
    public Long contarTotalUsuarios() {
        // Assume-se que 'contarTodosUsuariosNative()' faz um SELECT COUNT(*) no banco.
        return usuarioRepository.contarTodosUsuariosNative(); 
    }
    
    /**
     * Retorna a contagem total de restaurantes.
     */
    @Override
    @Transactional(readOnly = true)
    public Long contarTotalRestaurantes() {
        // Assume-se que 'contarTodosRestaurantesNative()' faz um SELECT COUNT(*) no banco.
        return restauranteRepository.contarTodosRestaurantesNative(); 
    }

    /**
     * Calcula o valor total de vendas realizadas nos últimos 30 dias.
     * Utiliza o tipo BigDecimal para garantir precisão financeira.
     */
   @Override
    public BigDecimal calcularVendasUltimos30Dias() {
        // 1. Define o período: Hoje e 30 dias atrás
        LocalDateTime trintaDiasAtras = LocalDateTime.now().minusDays(30);
        LocalDateTime agora = LocalDateTime.now();
        
        // 2. Chama o método do Repositório (que executa a soma diretamente no banco)
        BigDecimal vendas = pedidoRepository.calcularVendasPorPeriodo(trintaDiasAtras, agora);
        
        // 3. Retorna o valor real (ou 0.00 se o resultado for NULL)
        return vendas != null ? vendas : BigDecimal.ZERO; 
    }
    // ==========================================================
    // --- RELATÓRIOS (Agregação de Dados) ---
    // ==========================================================
    
    /**
     * Gera um relatório de vendas agregado por restaurante dentro de um período.
     * OBS: O filtro e a agregação estão sendo feitos *em memória* (após findAll()),
     * o que pode gerar lentidão com muitos dados. Otimização seria mover a agregação para o Repositório (JPQL/Query Nativa).
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioVendasDTO> gerarRelatorioVendas(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        // 1. Filtra os pedidos relevantes em memória
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getRestaurante() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Agrupa os pedidos por restaurante (Map<Restaurante, List<Pedido>>)
        Map<Restaurante, List<Pedido>> pedidosPorRestaurante =
                pedidos.stream().collect(Collectors.groupingBy(Pedido::getRestaurante));

        // 3. Mapeia os dados agrupados para o DTO de relatório
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
     * Gera um relatório de produtos mais vendidos, calculando a receita (incluindo opcionais).
     * A agregação é complexa e feita em memória.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelatorioProdutosDTO> gerarRelatorioProdutos(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDia = inicio.atStartOfDay();
        LocalDateTime fimDia = fim.atTime(23, 59, 59);

        // 1. Filtra os pedidos relevantes (incluindo seus itens)
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getItens() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Mapas para agregar os dados dos produtos
        Map<Long, Integer> contagemProdutos = new HashMap<>(); // Qtd Total Vendida
        Map<Long, BigDecimal> receitaProdutos = new HashMap<>(); // Receita Total (base + opcionais)
        Map<Long, Produto> produtosMap = new HashMap<>(); // Mapeia o ID para o objeto Produto

        // 3. Itera sobre todos os itens de todos os pedidos filtrados
        for (Pedido pedido : pedidos) {
            if (pedido.getItens() != null) {
                for (ItemPedido item : pedido.getItens()) {
                    if (item != null && item.getProduto() != null && item.getQuantidade() != null) {
                        Long produtoId = item.getProduto().getId();
                        
                        contagemProdutos.merge(produtoId, item.getQuantidade(), Integer::sum);
                        // item.getSubtotal já inclui preço base * quantidade + opcionais * quantidade (se calculado corretamente na criação do pedido)
                        receitaProdutos.merge(produtoId, item.getSubtotal(), BigDecimal::add); 
                        produtosMap.put(produtoId, item.getProduto());
                    }
                }
            }
        }

        if (contagemProdutos.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. Monta a lista de DTOs de resposta e ordena
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
                .sorted(Comparator.comparing(RelatorioProdutosDTO::getTotalVendido).reversed()) // Ordena por mais vendido
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

        // 1. Filtra os pedidos relevantes em memória
        List<Pedido> pedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getDataPedido() != null &&
                        p.getCliente() != null &&
                        !p.getDataPedido().isBefore(inicioDia) &&
                        !p.getDataPedido().isAfter(fimDia))
                .collect(Collectors.toList());

        if (pedidos.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Agrupa os pedidos por cliente
        Map<Cliente, List<Pedido>> pedidosPorCliente =
                pedidos.stream().collect(Collectors.groupingBy(Pedido::getCliente));

        // 3. Mapeia os dados agrupados para o DTO de relatório e ordena
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
                .sorted(Comparator.comparing(RelatorioClientesDTO::getTotalGasto).reversed()) // Ordena por maior gasto
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

        // 1. Filtra os pedidos relevantes em memória
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

        // 2. Apenas mapeia os pedidos filtrados para o DTO
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