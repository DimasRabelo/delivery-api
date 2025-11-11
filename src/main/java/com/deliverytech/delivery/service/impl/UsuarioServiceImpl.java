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

// --- 1. ADICIONAR IMPORTS NECESSÁRIOS ---
import com.deliverytech.delivery.enums.Role;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * (Seu método original - Está OK)
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
     * (Seu método original - Está OK)
     */
    @Override
    public UserResponse atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = this.buscarPorId(id);

        if (!dto.getEmail().equalsIgnoreCase(usuario.getEmail()) && 
             usuarioRepository.existsByEmail(dto.getEmail())) {
            
            throw new ConflictException("Email já está em uso por outro usuário");
        }

        usuario.setEmail(dto.getEmail());
        
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return new UserResponse(usuarioSalvo);
    }

    /**
     * (Seu método original - Está OK)
     */
    @Override
    public void deletar(Long id) {
        Usuario usuario = this.buscarPorId(id);
        usuario.setAtivo(false); 
        usuarioRepository.save(usuario);
    }

    // ==========================================================
    // --- 2. IMPLEMENTAÇÃO DO NOVO MÉTODO ---
    // ==========================================================
    /**
     * Busca todos os usuários que são ENTREGADORES e estão ATIVOS.
     * Chama o novo método do repositório e mapeia para DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> buscarEntregadoresAtivos() {
        // 1. Busca as entidades do banco usando o novo método do repo
        // (Ele busca por Role.ENTREGADOR e Ativo = true)
        List<Usuario> entregadores = usuarioRepository.findByRoleAndAtivo(Role.ENTREGADOR, true);
        
        // 2. Converte a lista de Entidade (Usuario) para DTO (UserResponse)
        return entregadores.stream()
                .map(UserResponse::new) // Usa o construtor 'new UserResponse(usuario)'
                .collect(Collectors.toList());
    }
}