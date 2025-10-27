package com.deliverytech.delivery.service.auth;

import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável pela lógica de negócios de autenticação e gerenciamento de usuários.
 *
 * Esta classe tem duas responsabilidades principais:
 * 1. Implementa {@link UserDetailsService}: a interface central do Spring Security
 * para carregar dados do usuário (pelo email) durante o processo de login.
 * 2. Expõe métodos de negócio para o {@link com.deliverytech.delivery.controller.auth.AuthController},
 * como a criação de novos usuários (criptografando a senha) e verificações.
 */
@Service
@RequiredArgsConstructor // Injeta as dependências 'final' via construtor (Lombok)
public class AuthService implements UserDetailsService {

    /**
     * Repositório para acesso aos dados da entidade {@link Usuario}.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Codificador de senhas (BCrypt) injetado pelo {@link com.deliverytech.delivery.config.SecurityConfig}.
     * Usado para criptografar senhas no registro.
     */
    private final PasswordEncoder passwordEncoder;

    // -------------------------------------------------------------------------
    // Implementação do UserDetailsService (Spring Security)
    // -------------------------------------------------------------------------

    /**
     * Método principal do {@link UserDetailsService}, chamado pelo Spring Security
     * (via AuthenticationManager) quando um usuário tenta se logar.
     *
     * @param email O email (username) fornecido na tentativa de login.
     * @return Um objeto {@link UserDetails} (a nossa própria entidade {@link Usuario})
     * se o usuário for encontrado e estiver ativo.
     * @throws UsernameNotFoundException Se o usuário não for encontrado ou
     * não estiver ativo ({@code ativo = false}).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Busca o usuário pelo email E garante que ele esteja ativo
        return usuarioRepository.findByEmailAndAtivo(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    // -------------------------------------------------------------------------
    // Métodos de Negócio (usados pelo AuthController)
    // -------------------------------------------------------------------------

    /**
     * Verifica de forma otimizada se um email já existe no banco de dados.
     * Usado pelo `AuthController` para validar o registro de novos usuários.
     *
     * @param email O email a ser verificado.
     * @return 'true' se o email já estiver em uso, 'false' caso contrário.
     */
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Cria e persiste uma nova entidade {@link Usuario} a partir de um DTO de registro.
     *
     * @param request O DTO {@link RegisterRequest} vindo do controller.
     * @return A entidade {@link Usuario} recém-salva (já com ID).
     */
    public Usuario criarUsuario(RegisterRequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        
        // Etapa de segurança crucial: criptografa a senha antes de salvar
        usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        
        usuario.setRole(request.getRole());
        usuario.setRestauranteId(request.getRestauranteId());
        usuario.setAtivo(true); // Novos usuários são ativos por padrão
        
        return usuarioRepository.save(usuario);
    }

    /**
     * Busca um usuário pelo seu ID.
     *
     * @param id O ID do usuário.
     * @return A entidade {@link Usuario} correspondente.
     * @throws RuntimeException (ou idealmente, uma exceção customizada) se o usuário não for encontrado.
     */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Busca um usuário pelo seu email.
     *
     * @param email O email do usuário.
     * @return A entidade {@link Usuario} correspondente.
     * @throws RuntimeException (ou idealmente, uma exceção customizada) se o usuário não for encontrado.
     */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}