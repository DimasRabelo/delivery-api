package com.deliverytech.delivery.service.auth;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO;
import com.deliverytech.delivery.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List; // <-- 1. IMPORTAR 'List'

/**
 * Interface de serviço para operações de gerenciamento (CRUD) de Usuários.
 * Define o "contrato" que o UsuarioController irá consumir.
 * (Diferente do AuthService, que lida com registro e login).
 */
public interface UsuarioService {

    /**
     * Busca todos os usuários de forma paginada.
     * (Usado pelo UsuarioController - GET /api/usuarios)
     */
    Page<UserResponse> buscarTodos(Pageable pageable);

    /**
     * Busca um usuário pela entidade.
     * (Usado internamente)
     */
    Usuario buscarPorId(Long id);

    /**
     * Busca um usuário e o retorna como DTO (UserResponse).
     * (Usado pelo UsuarioController - GET /api/usuarios/{id})
     */
    UserResponse buscarPorIdResponse(Long id);

    /**
     * Atualiza os dados de um usuário (apenas campos permitidos).
     * (Usado pelo UsuarioController - PUT /api/usuarios/{id})
     */
    UserResponse atualizar(Long id, UsuarioUpdateDTO dto);

    /**
     * Deleta (logicamente) um usuário.
     * (Usado pelo UsuarioController - DELETE /api/usuarios/{id})
     */
    void deletar(Long id);

    // ==========================================================
    // --- 2. MÉTODO ADICIONADO PARA O PAINEL DO RESTAURANTE ---
    // ==========================================================
    /**
     * Busca todos os usuários que são ENTREGADORES e estão ATIVOS.
     * (Usado pelo painel do restaurante para listar entregadores).
     *
     * @return Uma lista de UserResponse (DTOs) dos entregadores ativos.
     */
    List<UserResponse> buscarEntregadoresAtivos();
}