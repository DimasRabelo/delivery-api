package com.deliverytech.delivery.repository.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role; // <-- 1. IMPORTAR O 'Role'
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- 2. IMPORTAR 'List'
import java.util.Optional;

/**
 * Repositório Spring Data JPA para a entidade {@link Usuario}.
 *
 * Esta interface gerencia todas as operações de banco de dados (CRUD) para a
 * entidade Usuario, além de fornecer métodos de consulta personalizados.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo seu endereço de email.
     * (seu método original)
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica de forma otimizada se já existe um usuário cadastrado com o email fornecido.
     * (seu método original)
     */
    boolean existsByEmail(String email);

    /**
     * Busca um usuário pelo email, mas somente se ele estiver com o status "ativo".
     * (seu método original)
     */
    Optional<Usuario> findByEmailAndAtivo(String email, Boolean ativo);

    // ==========================================================
    // --- 3. MÉTODO ADICIONADO PARA O PAINEL DO RESTAURANTE ---
    // ==========================================================
    /**
     * Busca todos os usuários que possuem uma role específica e estão ativos.
     * Usado pelo painel do restaurante para listar os entregadores disponíveis.
     *
     * @param role O {@link Role} a ser buscado (ex: Role.ENTREGADOR).
     * @param ativo O status de ativação (sempre 'true').
     * @return Uma lista de {@link Usuario} que correspondem aos critérios.
     */
    List<Usuario> findByRoleAndAtivo(Role role, Boolean ativo);
}