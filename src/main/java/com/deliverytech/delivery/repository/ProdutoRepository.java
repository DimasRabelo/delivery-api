package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Produto}.
 * Gerencia operações CRUD, consultas por disponibilidade, nome, categoria
 * e carregamento completo de produtos com seus grupos e itens opcionais.
 */
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    // =================== PRODUTOS DISPONÍVEIS ===================
    List<Produto> findByDisponivelTrue();
    List<Produto> findByRestauranteIdAndDisponivel(Long restauranteId, Boolean disponivel);
    List<Produto> findByRestauranteIdAndDisponivelTrue(Long restauranteId);

    // =================== CONSULTAS POR NOME / CATEGORIA ===================
    List<Produto> findByNomeContainingIgnoreCaseAndDisponivelTrue(String nome);
    List<Produto> findByCategoriaAndDisponivelTrue(String categoria);

    // =================== VERIFICAÇÃO DE EXISTÊNCIA ===================
    boolean existsByNomeAndRestauranteId(String nome, Long restauranteId);

    // =================== PRODUTOS POR RESTAURANTE ===================
    List<Produto> findByRestauranteId(Long restauranteId);

    // ==========================================================
    // --- CONSULTA COMPLETA COM JOIN FETCH (CORREÇÃO ERRO 500) ---
    // ==========================================================
    /**
     * Busca um produto pelo ID, carregando toda a árvore de grupos e itens opcionais.
     * Evita LazyInitializationException ao acessar gruposOpcionais e itensOpcionais no Service/DTO.
     */
    @Query("SELECT p FROM Produto p " +
           "LEFT JOIN FETCH p.gruposOpcionais go " +
           "LEFT JOIN FETCH go.itensOpcionais io " +
           "WHERE p.id = :id")
    Optional<Produto> findProdutoCompletoById(@Param("id") Long id);
}
