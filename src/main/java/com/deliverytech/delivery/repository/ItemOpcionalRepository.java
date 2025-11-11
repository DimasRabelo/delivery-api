package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.ItemOpcional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório responsável pelo acesso a dados da entidade ItemOpcional.
 * Permite CRUD e consultas automáticas via Spring Data JPA.
 */
@Repository
public interface ItemOpcionalRepository extends JpaRepository<ItemOpcional, Long> {
}
