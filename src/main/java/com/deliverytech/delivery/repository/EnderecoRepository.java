package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositório responsável pelas operações de acesso a dados da entidade Endereco.
 */
@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    /** Retorna todos os endereços vinculados a um usuário. */
    List<Endereco> findByUsuarioId(Long usuarioId);

    /** Retorna apenas os endereços ativos de um usuário. */
    List<Endereco> findByUsuarioIdAndAtivoIsTrue(Long usuarioId);
}
