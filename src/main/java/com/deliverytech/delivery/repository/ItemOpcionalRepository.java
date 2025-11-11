package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.ItemOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemOpcionalRepository extends JpaRepository<ItemOpcional, Long> {
}