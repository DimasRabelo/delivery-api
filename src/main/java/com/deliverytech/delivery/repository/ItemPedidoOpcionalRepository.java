package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.ItemPedidoOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoOpcionalRepository extends JpaRepository<ItemPedidoOpcional, Long> {
}