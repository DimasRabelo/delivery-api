package com.deliverytech.delivery.service.auth;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO;
import com.deliverytech.delivery.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface de serviço para operações de gerenciamento (CRUD) de Usuários.
 * Define o "contrato" que o UsuarioController irá consumir.
 * Diferente do AuthService, que lida com registro e login.
 */
public interface UsuarioService {

    // ==========================================================
    // --- MÉTODOS DE BUSCA E PAGINAÇÃO ---
    // ==========================================================
    
    /**
     * Busca todos os usuários de forma paginada.
     * Usado pelo UsuarioController - GET /api/usuarios
     *
     * @param pageable Informações de paginação
     * @return Página de UserResponse
     */
    Page<UserResponse> buscarTodos(Pageable pageable);

    /**
     * Busca um usuário pela entidade.
     * Usado internamente pelos outros services
     *
     * @param id ID do usuário
     * @return Entidade Usuario
     */
    Usuario buscarPorId(Long id);

    /**
     * Busca um usuário e retorna como DTO (UserResponse)
     * Usado pelo UsuarioController - GET /api/usuarios/{id}
     *
     * @param id ID do usuário
     * @return DTO UserResponse
     */
    UserResponse buscarPorIdResponse(Long id);

    // ==========================================================
    // --- MÉTODOS DE ATUALIZAÇÃO E REMOÇÃO ---
    // ==========================================================
    
    /**
     * Atualiza os dados de um usuário (apenas campos permitidos).
     * Usado pelo UsuarioController - PUT /api/usuarios/{id}
     *
     * @param id ID do usuário
     * @param dto DTO com dados atualizados
     * @return DTO UserResponse atualizado
     */
    UserResponse atualizar(Long id, UsuarioUpdateDTO dto);

    /**
     * Deleta (logicamente) um usuário.
     * Usado pelo UsuarioController - DELETE /api/usuarios/{id}
     *
     * @param id ID do usuário
     */
    void deletar(Long id);

    // ==========================================================
    // --- MÉTODOS ESPECÍFICOS PARA FUNCIONALIDADES EXTRAS ---
    // ==========================================================
    
    /**
     * Busca todos os usuários que são ENTREGADORES e estão ATIVOS.
     * Usado pelo painel do restaurante para listar entregadores disponíveis.
     *
     * @return Lista de UserResponse (DTOs) dos entregadores ativos
     */
    List<UserResponse> buscarEntregadoresAtivos();


  
}
