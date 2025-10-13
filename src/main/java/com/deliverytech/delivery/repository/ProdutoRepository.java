package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

   // Apenas produtos disponíveis
    List<Produto> findByDisponivelTrue();

    // Buscar produtos por restaurante (usando entidade)
    List<Produto> findByRestauranteAndDisponivelTrue(Restaurante restaurante);

    // Buscar produtos por restaurante ID
    List<Produto> findByRestauranteIdAndDisponivelTrue(Long restauranteId);

    // Buscar por categoria (apenas disponíveis)
    List<Produto> findByCategoria(String categoria);

    // Buscar por nome (parcial, ignorando case)
    List<Produto> findByNomeContainingIgnoreCaseAndDisponivelTrue(String nome);

    // Buscar por faixa de preço
    List<Produto> findByPrecoBetweenAndDisponivelTrue(BigDecimal precoMin, BigDecimal precoMax);

    // Buscar produtos até determinado preço
    List<Produto> findByPrecoLessThanEqualAndDisponivelTrue(BigDecimal preco);

    // Ordenar por preço (ascendente e descendente)
    List<Produto> findByDisponivelTrueOrderByPrecoAsc();
    List<Produto> findByDisponivelTrueOrderByPrecoDesc();
    
    // Query customizada - produtos mais vendidos

    // Certifique-se de que Produto tem um mapeamento @OneToMany para ItemPedido
    @Query("SELECT p FROM Produto p JOIN p.itensPedido ip GROUP BY p ORDER BY COUNT(ip) DESC")
    List<Produto> findProdutosMaisVendidos();

    // Buscar produtos disponíveis por restaurante e categoria
    @Query("SELECT p FROM Produto p WHERE p.restaurante.id = :restauranteId " +
           "AND p.categoria = :categoria AND p.disponivel = true")
    List<Produto> findByRestauranteAndCategoria(@Param("restauranteId") Long restauranteId,
                                                @Param("categoria") String categoria);

    // Contar produtos disponíveis por restaurante
    @Query("SELECT COUNT(p) FROM Produto p WHERE p.restaurante.id = :restauranteId AND p.disponivel = true")
    Long countByRestauranteId(@Param("restauranteId") Long restauranteId);

     // ADICIONADO: buscar produtos pelo nome do restaurante
    List<Produto> findByRestauranteNome(String nomeRestaurante);

    // Buscar produto pelo nome exato
    Optional<Produto> findByNome(String nome);    

}
