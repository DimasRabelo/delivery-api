package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Buscar cliente por email
    Optional<Cliente> findByEmail(String email);

    // Verificar se email já existe
    boolean existsByEmail(String email);

    // Buscar apenas clientes ativos
    List<Cliente> findByAtivoTrue();

    // Buscar clientes por nome (parcial, ignorando case)
    List<Cliente> findByNomeContainingIgnoreCase(String nome);

    // Buscar clientes por telefone
    Optional<Cliente> findByTelefone(String telefone);

    // Query customizada - clientes ativos que possuem pedidos
    @Query("SELECT DISTINCT c FROM Cliente c JOIN c.pedidos p WHERE c.ativo = true")
    List<Cliente> findClientesComPedidos();

    // Query nativa - buscar clientes ativos por cidade (campo endereco)
    @Query(value = "SELECT * FROM cliente WHERE endereco LIKE %:cidade% AND ativo = true", nativeQuery = true)
    List<Cliente> findByCidade(@Param("cidade") String cidade);

    // Contar clientes ativos
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.ativo = true")
    Long countClientesAtivos();

//   @Query(value = "SELECT c.nome, COALESCE(SUM(ip.quantidade), 0) as total_produtos " +
//                "FROM cliente c " +
//                "LEFT JOIN pedido p ON c.id = p.cliente_id " +
//                "LEFT JOIN itens_pedido ip ON p.id = ip.pedido_id " +
//                "GROUP BY c.id, c.nome " +
//                "ORDER BY total_produtos DESC " +
//                "LIMIT 10", nativeQuery = true)
// List<Object[]> rankingClientesPorPedidos();




}
