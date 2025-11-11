package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // --- MÉTODOS CORRIGIDOS (BUSCANDO PELO 'USUARIO' ASSOCIADO) ---

    /**
     * Verifica se existe um Cliente cujo Usuario associado tenha este email.
     * (Substitui o seu 'existsByEmail')
     */
    boolean existsByUsuarioEmail(String email);

    /**
     * Verifica se existe um Cliente com este CPF.
     * (Este método estava correto no seu arquivo).
     */
    boolean existsByCpf(String cpf);

    /**
     * Busca um Cliente cujo Usuario associado tenha este email.
     * (Substitui o seu 'findByEmail')
     */
    Optional<Cliente> findByUsuarioEmail(String email);

    /**
     * Busca Clientes cujo Usuario associado esteja ativo.
     * (Substitui o seu 'findByAtivoTrue')
     */
    List<Cliente> findByUsuarioAtivoTrue();

    /**
     * Busca Clientes (paginado) cujo Usuario associado esteja ativo.
     * (Substitui o seu 'findByAtivoTrue(Pageable)')
     */
    Page<Cliente> findByUsuarioAtivoTrue(Pageable pageable);
    
    // (Os outros métodos customizados como 'findByCidade' e 'findClientesComPedidos' 
    // foram removidos pois a lógica antiga (String 'endereco') não é mais válida.
    // Eles precisarão ser reescritos com a nova arquitetura se forem necessários.)
}