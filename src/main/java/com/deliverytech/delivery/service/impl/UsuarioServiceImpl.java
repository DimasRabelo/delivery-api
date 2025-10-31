package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.service.auth.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementação dos serviços de gerenciamento (CRUD) da entidade Usuario.
 * Implementa a interface UsuarioService.
 */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Injeção de dependência via construtor
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca todos os usuários de forma paginada e os converte para UserResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> buscarTodos(Pageable pageable) {
        Page<Usuario> paginaUsuarios = usuarioRepository.findAll(pageable);
        // Converte Page<Usuario> para Page<UserResponse> usando o construtor do DTO
        return paginaUsuarios.map(UserResponse::new);
    }

    /**
     * Busca um usuário pelo seu ID.
     * Lança uma exceção customizada se não encontrar.
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + id));
    }

    /**
     * Busca um usuário pelo ID e já o converte para o DTO UserResponse.
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse buscarPorIdResponse(Long id) {
        // Reutiliza o método buscarPorId para evitar duplicação
        Usuario usuario = this.buscarPorId(id);
        return new UserResponse(usuario);
    }

    /**
     * Atualiza um usuário de forma segura, usando um DTO.
     * Apenas 'nome' e 'email' são atualizados.
     */
    @Override
    public UserResponse atualizar(Long id, UsuarioUpdateDTO dto) {
        // 1. Busca o usuário existente no banco
        Usuario usuario = this.buscarPorId(id);

        // 2. Validação: Verifica se o novo email já está em uso por OUTRO usuário
        if (!dto.getEmail().equalsIgnoreCase(usuario.getEmail()) && 
             usuarioRepository.existsByEmail(dto.getEmail())) {
            
            throw new ConflictException("Email já está em uso por outro usuário");
        }

        // 3. Copia os campos permitidos do DTO para a entidade
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getEmail());
        
        // (Não mexemos em senha, role ou status aqui por segurança)

        // 4. Salva o usuário atualizado
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // 5. Retorna o DTO de resposta
        return new UserResponse(usuarioSalvo);
    }

    /**
     * Realiza a "exclusão lógica" (soft delete) de um usuário.
     * O usuário não é removido do banco, apenas marcado como inativo.
     */
    @Override
    public void deletar(Long id) {
        Usuario usuario = this.buscarPorId(id);
        
        // Soft Delete: Apenas desativa o usuário
        usuario.setAtivo(false); 
        
        usuarioRepository.save(usuario);
    }
}