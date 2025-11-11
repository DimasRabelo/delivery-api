package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // <-- IMPORT NECESSÁRIO
import org.springframework.data.repository.query.Param; // <-- IMPORT NECESSÁRIO
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional; // <-- IMPORT NECESSÁRIO

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    // (Seus métodos existentes... OK)
    List<Produto> findByDisponivelTrue();
    List<Produto> findByRestauranteIdAndDisponivel(Long restauranteId, Boolean disponivel);
    List<Produto> findByNomeContainingIgnoreCaseAndDisponivelTrue(String nome);
    List<Produto> findByCategoriaAndDisponivelTrue(String categoria);
    boolean existsByNomeAndRestauranteId(String nome, Long restauranteId);
    List<Produto> findByRestauranteId(Long restauranteId); 
    List<Produto> findByRestauranteIdAndDisponivelTrue(Long restauranteId);


    // ==========================================================
    // --- NOVO MÉTODO (A CORREÇÃO DO ERRO 500) ---
    // ==========================================================
    /**
     * Busca um produto pelo ID, forçando o carregamento (JOIN FETCH)
     * de toda a árvore de grupos e itens opcionais.
     * Isso evita a LazyInitializationException no Service/DTO.
     */
    @Query("SELECT p FROM Produto p " +
           "LEFT JOIN FETCH p.gruposOpcionais go " +
           "LEFT JOIN FETCH go.itensOpcionais io " +
           "WHERE p.id = :id")
    Optional<Produto> findProdutoCompletoById(@Param("id") Long id);
}