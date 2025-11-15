package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.service.auth.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do Serviço de Usuários. Gerencia o acesso e a manipulação
 * da entidade principal de segurança (Usuario).
 */
@Service
@Transactional // Define o gerenciamento transacional para a classe
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Dependência de criptografia

    // 1. CORREÇÃO: Injeção de dependência via construtor (agora incluindo PasswordEncoder)
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Busca uma lista paginada de todos os usuários e a mapeia para DTOs.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> buscarTodos(Pageable pageable) {
        Page<Usuario> paginaUsuarios = usuarioRepository.findAll(pageable);
        return paginaUsuarios.map(UserResponse::new); 
    }

    /**
     * Busca a entidade Usuario completa pelo ID. É um método auxiliar interno.
     */
    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com id: " + id));
    }

    /**
     * Busca um usuário pelo ID e o retorna como um DTO (UserResponse).
     */
    @Override
    @Transactional(readOnly = true)
    public UserResponse buscarPorIdResponse(Long id) {
        Usuario usuario = this.buscarPorId(id);
        return new UserResponse(usuario);
    }

    /**
     * 2. CORREÇÃO: Atualiza os dados de um usuário (e-mail, nome e/ou senha).
     * A atualização é condicional (apenas se o campo for fornecido no DTO).
     */
    @Override
    public UserResponse atualizar(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = this.buscarPorId(id);

        // --- ATUALIZAÇÃO DO EMAIL ---
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty() &&
            !dto.getEmail().equalsIgnoreCase(usuario.getEmail())) {
            
            // Validação de Conflito: Checa se o novo e-mail já está em uso por OUTRO usuário
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new ConflictException("Email já está em uso por outro usuário");
            }
            usuario.setEmail(dto.getEmail());
        }

        // --- ATUALIZAÇÃO DO NOME ---
        if (dto.getNome() != null && !dto.getNome().trim().isEmpty()) {
            usuario.setNome(dto.getNome());
        }

        // --- ATUALIZAÇÃO DA SENHA ---
        // A senha só deve ser atualizada se for fornecida e não vazia (e deve ser encriptada)
        if (dto.getSenha() != null && !dto.getSenha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }
        
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return new UserResponse(usuarioSalvo);
    }

    /**
     * Realiza uma "deleção lógica" de um usuário, marcando-o como inativo.
     */
    @Override
    public void deletar(Long id) {
        Usuario usuario = this.buscarPorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    /**
     * Busca todos os usuários que têm a Role 'ENTREGADOR' e estão 'ATIVOS'.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> buscarEntregadoresAtivos() {
        List<Usuario> entregadores = usuarioRepository.findByRoleAndAtivo(Role.ENTREGADOR, true);
        return entregadores.stream()
                .map(UserResponse::new) 
                .collect(Collectors.toList());
    }
}