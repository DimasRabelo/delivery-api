package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.GrupoOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // <-- Adicione este import se estiver faltando

public interface GrupoOpcionalRepository extends JpaRepository<GrupoOpcional, Long> {

    // 👇 ADICIONE ESTA LINHA 👇
    /**
     * Encontra todos os grupos de opcionais associados a um ID de produto específico.
     * O Spring Data JPA cria a query automaticamente pelo nome do método.
     */
    List<GrupoOpcional> findByProdutoId(Long produtoId);
    
}