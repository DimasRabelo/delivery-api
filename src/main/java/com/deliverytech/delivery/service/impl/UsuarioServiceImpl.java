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

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * (Seu método original - Está OK)
     * O 'UserResponse::new' funciona porque já corrigimos o UserResponse.java
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> buscarTodos(Pageable pageable) {
        Page<Usuario> paginaUsuarios = usuarioRepository.findAll(pageable);
        return paginaUsuarios.map(UserResponse::new);
    }

    /**
     * (Seu método original - Está OK)
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + id));
    }

    /**
     * (Seu método original - Está OK)
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse buscarPorIdResponse(Long id) {
        Usuario usuario = this.buscarPorId(id);
        return new UserResponse(usuario);
    }

    /**
     * Atualiza os dados de autenticação de um usuário (VERSÃO REFATORADA).
     * Agora atualiza APENAS os campos que pertencem ao 'Usuario' (email).
     * A atualização do 'nome' é feita pelo ClienteService.
     */
    @Override
    public UserResponse atualizar(Long id, UsuarioUpdateDTO dto) {
        // 1. Busca o usuário existente
        Usuario usuario = this.buscarPorId(id);

        // 2. Validação: Verifica se o novo email já está em uso
        if (!dto.getEmail().equalsIgnoreCase(usuario.getEmail()) && 
             usuarioRepository.existsByEmail(dto.getEmail())) {
            
            throw new ConflictException("Email já está em uso por outro usuário");
        }

        // 3. --- CORREÇÃO (GARGALO 4 / DECISÃO 1) ---
        // A linha 'usuario.setNome(dto.getNome());' FOI REMOVIDA.
        // Este serviço só atualiza o 'email'.
        usuario.setEmail(dto.getEmail());
        
        // 4. Salva o usuário atualizado
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // 5. Retorna o DTO de resposta
        // (O construtor do UserResponse já sabe buscar o 'nome' no Cliente)
        return new UserResponse(usuarioSalvo);
    }

    /**
     * (Seu método original - Está OK)
     * Realiza a "exclusão lógica" (soft delete) de um usuário.
     */
    @Override
    public void deletar(Long id) {
        Usuario usuario = this.buscarPorId(id);
        
        // Soft Delete: Apenas desativa o usuário
        usuario.setAtivo(false); 
        
        usuarioRepository.save(usuario);
    }
}