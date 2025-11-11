package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.ItemPedidoOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade ItemPedidoOpcional.
 * Gerencia os opcionais selecionados em cada item de pedido.
 * Permite operações CRUD automáticas pelo Spring Data JPA.
 */
@Repository
public interface ItemPedidoOpcionalRepository extends JpaRepository<ItemPedidoOpcional, Long> {
}
