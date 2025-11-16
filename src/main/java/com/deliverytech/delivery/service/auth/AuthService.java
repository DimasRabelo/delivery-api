package com.deliverytech.delivery.service.auth;

import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.exception.ConflictException;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço de autenticação e registro de usuários.
 * Implementa UserDetailsService para integração com Spring Security.
 */
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    // ==========================================================
    // --- DEPENDÊNCIAS ---
    // ==========================================================
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    // ==========================================================
    // --- MÉTODOS DO SPRING SECURITY ---
    // ==========================================================
    
    /**
     * Carrega o usuário pelo email para autenticação no Spring Security.
     * @param email Email do usuário
     * @return UserDetails do Spring Security
     * @throws UsernameNotFoundException Se o usuário não existir ou estiver inativo
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailAndAtivo(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    // ==========================================================
    // --- MÉTODOS AUXILIARES ---
    // ==========================================================
    
    /**
     * Verifica se o email já está cadastrado no sistema.
     */
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // ==========================================================
    // --- MÉTODOS DE REGISTRO ---
    // ==========================================================
    
    /**
     * Registra um novo cliente no sistema.
     *
     * @param dto DTO com os dados de registro
     * @return Usuário salvo
     * @throws ConflictException Se o email já estiver em uso
     */
    @Transactional
    public Usuario registrarCliente(RegisterRequest dto) {

        // 1. Validação de e-mail
        if (existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email já está em uso", "email", dto.getEmail());
        }

        // 2. Criar a entidade Usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);

        // 3. LIMPEZA DOS CAMPOS ESTREITOS (CPF e Telefone)
        String cpfLimpo = dto.getCpf().replaceAll("[^0-9]", ""); 
        String telefoneLimpo = dto.getTelefone() != null ? dto.getTelefone().replaceAll("[^0-9]", "") : null;
        
        // 4. Criar a entidade Cliente
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(cpfLimpo); // <-- CORRIGIDO: Usar o CPF LIMPO
        cliente.setTelefone(telefoneLimpo); // Usar o telefone limpo

        // 5. Criar a entidade Endereco
        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);

        // CORREÇÃO: Limpar o CEP mapeado (se houver hífen, a validação falhará)
        if (endereco.getCep() != null) {
            endereco.setCep(endereco.getCep().replaceAll("[^0-9]", ""));
        }
        
        // 6. Conectar tudo (bidirecional)
        cliente.setUsuario(usuario);
        usuario.setCliente(cliente);

        endereco.setUsuario(usuario);
        usuario.getEnderecos().add(endereco);

        // 7. Salvar apenas o "pai" (Usuario). Cascade salva Cliente e Endereco.
        return usuarioRepository.save(usuario);
    }

    // ==========================================================
    // --- MÉTODOS DE BUSCA ---
    // ==========================================================
    
    /**
     * Busca um usuário pelo ID.
     * @param id ID do usuário
     * @return Usuario encontrado
     */
   
    public Usuario buscarPorId(@NonNull Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * Busca um usuário pelo email.
     * @param email Email do usuário
     * @return Usuario encontrado
     */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }


// ... (mantenha o restante das importações)

// ... Dentro da classe AuthService

// ==========================================================
// --- MÉTODOS DE LOGIN/VERIFICAÇÃO ---
// ==========================================================

// 🔑 NOVO MÉTODO: GUARDA DE SEGURANÇA NA API
/**
 * Verifica se o usuário tem permissão para usar o endpoint de login geral.
 * Se a Role for RESTAURANTE, lança uma exceção para barrar o acesso.
 * @param email Email do usuário
 * @return Usuario se for permitido (ADMIN, CLIENTE, ENTREGADOR).
 * @throws BadCredentialsException se a Role for RESTAURANTE.
 */
public Usuario checkRoleForGeneralLogin(String email) {
    // 1. Carrega o usuário. O Spring Security já garantiu que a senha está correta
    // no momento em que seu Controller chama o AuthService.
    Usuario usuario = (Usuario) loadUserByUsername(email);

    // 2. CORREÇÃO CRÍTICA: BARRAR RESTAURANTE NO LOGIN GERAL
    if (usuario.getRole() == Role.RESTAURANTE) {
        // Lançar exceção com a mensagem correta. Isso será traduzido para 401/403.
        throw new BadCredentialsException("Acesso Negado: Use o Login Administrativo do Restaurante.");
    }

    // 3. Retorna o usuário, permitindo o prosseguimento do login para as Roles permitidas.
    return usuario;
}

// ... (métodos de registro e busca existentes)
}