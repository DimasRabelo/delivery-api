package com.deliverytech.delivery.service.auth;

// --- IMPORTS ADICIONADOS ---
import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.exception.ConflictException; 
import org.modelmapper.ModelMapper; 
import org.springframework.transaction.annotation.Transactional; 
// --- FIM DOS IMPORTS ADICIONADOS ---

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor 
public class AuthService implements UserDetailsService {

    // --- DEPENDÊNCIAS ---
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    
    // (Não precisamos mais dos repositórios Cliente/Endereco aqui)
    // private final ClienteRepository clienteRepository; 
    // private final EnderecoRepository enderecoRepository;


    /**
     * Carrega o usuário pelo email para o Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailAndAtivo(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    
    /**
     * Verifica se um e-mail já existe.
     */
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Registra um novo CLIENTE no sistema (VERSÃO CORRIGIDA).
     * Salva Usuário, Cliente e Endereço em uma única operação
     * usando o cascade do JPA.
     *
     * @param dto O DTO 'RegisterRequest' refatorado.
     * @return A entidade 'Usuario' que foi salva.
     */
    @Transactional // Garante que tudo (Usuário, Cliente, Endereço) seja salvo, ou nada.
    public Usuario registrarCliente(RegisterRequest dto) {
        
        // 1. Validação de E-mail
        if (existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email já está em uso", "email", dto.getEmail());
        }

        // ==========================================================
        // --- CORREÇÃO DO ERRO 500 (ObjectOptimisticLocking) ---
        // ==========================================================

        // 2. Criar a entidade de Autenticação (Usuario) - EM MEMÓRIA
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);
        
        // 3. Criar a entidade de Perfil (Cliente) - EM MEMÓRIA
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setCpf(dto.getCpf());
        cliente.setTelefone(dto.getTelefone());
        
        // 4. Criar a entidade de Endereço - EM MEMÓRIA
        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);
        
        // 5. CONECTAR TUDO (Bidirecional)
        
        // Conecta Cliente ao Usuário (e vice-versa)
        cliente.setUsuario(usuario);
        usuario.setCliente(cliente);
        
        // Conecta Endereço ao Usuário (e vice-versa)
        endereco.setUsuario(usuario);
        usuario.getEnderecos().add(endereco); // Adiciona na lista

        // 6. SALVAR (APENAS O PAI)
        // O @Transactional e o CascadeType.ALL cuidarão de salvar 
        // o 'cliente' e o 'endereco' automaticamente.
        return usuarioRepository.save(usuario);
    }
    
    
    /**
     * (Seu método original - Está OK)
     * Busca um usuário pelo seu ID.
     */
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    /**
     * (Seu método original - Está OK)
     * Busca um usuário pelo seu email.
     */
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}