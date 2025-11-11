package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.GrupoOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repositório responsável pelas operações de acesso a dados da entidade GrupoOpcional.
 */
public interface GrupoOpcionalRepository extends JpaRepository<GrupoOpcional, Long> {

    /** Retorna todos os grupos de opcionais vinculados a um produto. */
    List<GrupoOpcional> findByProdutoId(Long produtoId);
}
