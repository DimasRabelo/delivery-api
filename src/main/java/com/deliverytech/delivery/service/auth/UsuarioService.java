package com.deliverytech.delivery.service.auth;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO;
import com.deliverytech.delivery.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interface de serviço para operações de gerenciamento (CRUD) de Usuários.
 * Define o "contrato" que o UsuarioController irá consumir.
 * * (Diferente do AuthService, que lida com registro e login).
 */
public interface UsuarioService {

    /**
     * Busca todos os usuários de forma paginada.
     * (Usado pelo UsuarioController - GET /api/usuarios)
     *
     * @param pageable Informações de paginação.
     * @return Uma página (Page) de UserResponse.
     */
    Page<UserResponse> buscarTodos(Pageable pageable);

    /**
     * Busca um usuário pela entidade.
     * Útil para uso interno por outros serviços (ex: AuthService).
     *
     * @param id O ID do usuário.
     * @return A entidade Usuario.
     * @throws com.deliverytech.delivery.exception.EntityNotFoundException Se não encontrado.
     */
    Usuario buscarPorId(Long id);

    /**
     * Busca um usuário e o retorna como DTO (UserResponse).
     * (Usado pelo UsuarioController - GET /api/usuarios/{id})
     *
     * @param id O ID do usuário.
     * @return O DTO UserResponse.
     * @throws com.deliverytech.delivery.exception.EntityNotFoundException Se não encontrado.
     */
    UserResponse buscarPorIdResponse(Long id);

    /**
     * Atualiza os dados de um usuário (apenas campos permitidos).
     * (Usado pelo UsuarioController - PUT /api/usuarios/{id})
     *
     * @param id O ID do usuário a ser atualizado.
     * @param dto O DTO com os dados (nome, email).
     * @return O UserResponse do usuário atualizado.
     * @throws com.deliverytech.delivery.exception.EntityNotFoundException Se não encontrado.
     * @throws com.deliverytech.delivery.exception.ConflictException Se o email já estiver em uso.
     */
    UserResponse atualizar(Long id, UsuarioUpdateDTO dto);

    /**
     * Deleta (logicamente) um usuário.
     * (Usado pelo UsuarioController - DELETE /api/usuarios/{id})
     *
     * @param id O ID do usuário a ser deletado.
     * @throws com.deliverytech.delivery.exception.EntityNotFoundException Se não encontrado.
     */
    void deletar(Long id);

}