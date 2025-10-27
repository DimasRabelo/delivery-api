package com.deliverytech.delivery.repository.auth;

import com.deliverytech.delivery.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * Este método é útil para consultas gerais que não envolvem login.
     *
     * @param email O email a ser buscado.
     * @return Um {@link Optional} contendo o {@link Usuario} se encontrado, ou vazio caso contrário.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica de forma otimizada se já existe um usuário cadastrado com o email fornecido.
     * É mais performático do que buscar a entidade inteira.
     *
     * @param email O email a ser verificado.
     * @return 'true' se o email já estiver em uso, 'false' caso contrário.
     */
    boolean existsByEmail(String email);

    /**
     * Busca um usuário pelo email, mas somente se ele estiver com o status "ativo".
     * Este é o método preferencial para autenticação (usado pelo {@link UserDetailsService}),
     * garantindo que usuários desativados não consigam logar.
     *
     * @param email O email do usuário.
     * @param ativo O status de ativação (geralmente 'true' para login).
     * @return Um {@link Optional} contendo o {@link Usuario} ativo se encontrado, ou vazio caso contrário.
     */
    Optional<Usuario> findByEmailAndAtivo(String email, Boolean ativo);
}