package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // =================== PEDIDOS POR CLIENTE ===================
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);
    List<Pedido> findByClienteOrderByDataPedidoDesc(com.deliverytech.delivery.entity.Cliente cliente);

    // =================== PEDIDOS POR STATUS ===================
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);
    Page<Pedido> findByStatusAndDataPedidoBetween(StatusPedido status, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // =================== PEDIDOS POR RESTAURANTE ===================
    Page<Pedido> findByRestauranteId(Long restauranteId, Pageable pageable);
    Page<Pedido> findByRestauranteIdAndStatus(Long restauranteId, StatusPedido status, Pageable pageable);

    // =================== PEDIDOS POR PERÍODO ===================
    Page<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // Métodos antigos ainda podem existir, se precisar sem paginação
    List<Pedido> findByStatusOrderByDataPedidoDesc(StatusPedido status);
    List<Pedido> findByDataPedidoBetweenOrderByDataPedidoDesc(LocalDateTime inicio, LocalDateTime fim);
    List<Pedido> findByRestauranteId(@Param("restauranteId") Long restauranteId);

    // =================== OUTROS ===================
    Pedido findByNumeroPedido(String numeroPedido);
    List<Pedido> findTop10ByOrderByDataPedidoDesc();

    @Query("SELECT p.status, COUNT(p) FROM Pedido p GROUP BY p.status")
    List<Object[]> countPedidosByStatus();

    @Query("SELECT p FROM Pedido p WHERE p.status IN ('PENDENTE', 'CONFIRMADO', 'PREPARANDO') ORDER BY p.dataPedido ASC")
    List<Pedido> findPedidosPendentes();

    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.dataPedido BETWEEN :inicio AND :fim AND p.status NOT IN ('CANCELADO')")
    BigDecimal calcularVendasPorPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT p.restaurante.nome, SUM(p.valorTotal) " +
            "FROM Pedido p " +
            "GROUP BY p.restaurante.id, p.restaurante.nome " +
            "ORDER BY SUM(p.valorTotal) DESC")
    List<Object[]> calcularTotalVendasPorRestaurante();

    @Query("SELECT p FROM Pedido p WHERE p.valorTotal > :valor ORDER BY p.valorTotal DESC")
    List<Pedido> buscarPedidosComValorAcimaDe(@Param("valor") BigDecimal valor);

    @Query("SELECT p FROM Pedido p " +
            "WHERE p.dataPedido BETWEEN :inicio AND :fim " +
            "AND p.status = :status " +
            "ORDER BY p.dataPedido DESC")
    List<Pedido> relatorioPedidosPorPeriodoEStatus(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("status") StatusPedido status);
}
