package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.ItemPedido; // <-- IMPORT NECESSÁRIO
import com.deliverytech.delivery.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido>{

    // =================== PEDIDOS POR CLIENTE ===================
    List<Pedido> findByClienteIdOrderByDataPedidoDesc(Long clienteId);
    List<Pedido> findByClienteOrderByDataPedidoDesc(com.deliverytech.delivery.entity.Cliente cliente);
    Page<Pedido> findByClienteId(Long clienteId, Pageable pageable);

    // =================== PEDIDOS POR STATUS ===================
    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);
    Page<Pedido> findByStatusAndDataPedidoBetween(StatusPedido status, LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // =================== PEDIDOS POR PERÍODO ===================
    Page<Pedido> findByDataPedidoBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    // =================================================================
    // --- CORREÇÃO DE MULTIPLE BAG FETCH EXCEPTION (JÁ FEITA) ---
    // =================================================================
    
    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.restaurante r " +
           "LEFT JOIN FETCH p.itens i " + // 
           "WHERE p.restaurante.id = :restauranteId " +
           "AND (:status IS NULL OR p.status = :status)")
    List<Pedido> findPedidosByRestauranteIdAndStatusComItens(
            @Param("restauranteId") Long restauranteId,
            @Param("status") StatusPedido status
    );

    @Query("SELECT DISTINCT ip FROM ItemPedido ip " +
       "LEFT JOIN FETCH ip.opcionaisSelecionados iso " +
       "LEFT JOIN FETCH iso.itemOpcional " +
       "WHERE ip IN :itens")
List<ItemPedido> fetchOpcionaisParaItens(@Param("itens") List<ItemPedido> itens);

    // =================================================================
    // FIM DA CORREÇÃO
    // =================================================================

    // Métodos antigos
    List<Pedido> findByStatusOrderByDataPedidoDesc(StatusPedido status);
    List<Pedido> findByDataPedidoBetweenOrderByDataPedidoDesc(LocalDateTime inicio, LocalDateTime fim);
    // (A linha 'List<Pedido> findByRestauranteId' foi removida pois era duplicada)

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

           
    boolean existsByEntregadorAndStatus(Usuario entregador, StatusPedido status);


    // ==========================================================
    // --- NOVO MÉTODO PARA A CORREÇÃO DO @PreAuthorize (ERRO 403) ---
    // (Esta é a linha que estava faltando)
    // ==========================================================
    /**
     * Verifica eficientemente se um pedido pertence a um cliente, restaurante OU entregador.
     * Usado pelo 'canAccess' para evitar LazyInitializationException.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Pedido p " +
           "WHERE p.id = :pedidoId AND (" +
           "p.cliente.id = :clienteId OR " +
           "p.restaurante.id = :restauranteId OR " +
           "(p.entregador IS NOT NULL AND p.entregador.id = :entregadorId)" +
           ")")
    boolean isPedidoOwnedBy( 
            @Param("pedidoId") Long pedidoId, 
            @Param("clienteId") Long clienteId, 
            @Param("restauranteId") Long restauranteId,
            @Param("entregadorId") Long entregadorId 
    );
}