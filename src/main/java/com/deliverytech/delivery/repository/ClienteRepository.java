package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelas operações de acesso a dados da entidade Cliente.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Verifica se existe um cliente com o e-mail informado (via usuário vinculado). */
    boolean existsByUsuarioEmail(String email);

    /** Verifica se existe um cliente com o CPF informado. */
    boolean existsByCpf(String cpf);

    /** Busca um cliente pelo e-mail vinculado ao seu usuário. */
    Optional<Cliente> findByUsuarioEmail(String email);

    /** Lista todos os clientes cujo usuário está ativo. */
    List<Cliente> findByUsuarioAtivoTrue();

    /** Retorna clientes ativos de forma paginada. */
    Page<Cliente> findByUsuarioAtivoTrue(Pageable pageable);
}
