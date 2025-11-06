package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    // (Opcional, mas útil) Método para listar todos os endereços de um usuário
    List<Endereco> findByUsuarioId(Long usuarioId);
}