package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.GrupoOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoOpcionalRepository extends JpaRepository<GrupoOpcional, Long> {
}