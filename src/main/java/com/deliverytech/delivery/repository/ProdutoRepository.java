package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Buscar apenas produtos disponíveis
    List<Produto> findByDisponivelTrue();

    // Buscar produtos por restaurante com filtro de disponibilidade
    List<Produto> findByRestauranteIdAndDisponivel(Long restauranteId, Boolean disponivel);

    // Buscar produtos por nome (parcial, case insensitive) apenas disponíveis
    List<Produto> findByNomeContainingIgnoreCaseAndDisponivelTrue(String nome);

    // Buscar produtos por categoria apenas disponíveis
    List<Produto> findByCategoriaAndDisponivelTrue(String categoria);

    // Verificar se já existe um produto com mesmo nome em um restaurante
    boolean existsByNomeAndRestauranteId(String nome, Long restauranteId);
}
