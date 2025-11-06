package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>, JpaSpecificationExecutor<Produto> {

    // Buscar apenas produtos disponíveis
    List<Produto> findByDisponivelTrue();

    // Buscar produtos por restaurante com filtro de disponibilidade
    // (Seu método original - OK)
    List<Produto> findByRestauranteIdAndDisponivel(Long restauranteId, Boolean disponivel);

    // Buscar produtos por nome (parcial, case insensitive) apenas disponíveis
    // (Seu método original - OK)
    List<Produto> findByNomeContainingIgnoreCaseAndDisponivelTrue(String nome);

    // Buscar produtos por categoria apenas disponíveis
    // (Seu método original - OK)
    List<Produto> findByCategoriaAndDisponivelTrue(String categoria);

    // Verificar se já existe um produto com mesmo nome em um restaurante
    // (Seu método original - OK)
    boolean existsByNomeAndRestauranteId(String nome, Long restauranteId);
    
    // --- MÉTODO FALTANTE (GARGALO) ---
    /**
     * Busca todos os produtos de um restaurante (independentemente da disponibilidade).
     * Usado pelo 'buscarProdutosPorRestaurante' quando 'disponivel == null'.
     */
    List<Produto> findByRestauranteId(Long restauranteId); // <-- MÉTODO ADICIONADO
}