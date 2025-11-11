package com.deliverytech.delivery.repository.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelas operações de acesso a dados da entidade Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca um usuário pelo e-mail. */
    Optional<Usuario> findByEmail(String email);

    /** Verifica se já existe um usuário cadastrado com o e-mail informado. */
    boolean existsByEmail(String email);

    /** Busca um usuário ativo pelo e-mail. */
    Optional<Usuario> findByEmailAndAtivo(String email, Boolean ativo);

    /** Lista usuários ativos por função (ex: ENTREGADOR, CLIENTE, etc.). */
    List<Usuario> findByRoleAndAtivo(Role role, Boolean ativo);
}
