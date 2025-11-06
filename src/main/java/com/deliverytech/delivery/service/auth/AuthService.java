package com.deliverytech.delivery.service.auth;

// --- IMPORTS ADICIONADOS ---
import com.deliverytech.delivery.dto.auth.RegisterRequest; // (O DTO refatorado)
import com.deliverytech.delivery.dto.EnderecoDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.exception.ConflictException; // (Para validar e-mail)
import org.modelmapper.ModelMapper; // (Para mapear o EnderecoDTO)
import org.springframework.transaction.annotation.Transactional; // (Para o @Transactional)
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
@RequiredArgsConstructor // O Lombok injeta todos os campos 'final'
public class AuthService implements UserDetailsService {

    // --- DEPENDÊNCIAS ANTIGAS (OK) ---
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // --- NOVAS DEPENDÊNCIAS (NECESSÁRIAS) ---
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ModelMapper modelMapper; // (Certifique-se que este Bean está configurado)

    
    /**
     * (Seu método original - Está OK)
     * Carrega o usuário pelo email para o Spring Security.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailAndAtivo(email, true)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    
    /**
     * (Seu método original - Está OK)
     * Verifica se um e-mail já existe.
     */
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Registra um novo CLIENTE no sistema (VERSÃO REFATORADA).
     * Cria o Usuário (autenticação), o Cliente (perfil) e o Endereço (entrega)
     * e os conecta corretamente usando @OneToOne e @ManyToOne.
     *
     * @param dto O DTO 'RegisterRequest' refatorado (com dados de perfil e endereço).
     * @return A entidade 'Usuario' que foi salva.
     */
    @Transactional // Garante que tudo (Usuário, Cliente, Endereço) seja salvo, ou nada.
    public Usuario registrarCliente(RegisterRequest dto) {
        
        // 1. Validação de E-mail
        if (existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email já está em uso", "email", dto.getEmail());
        }

        // 2. Criar a entidade de Autenticação (Usuario)
        Usuario usuario = new Usuario();
        usuario.setEmail(dto.getEmail());
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setRole(Role.CLIENTE); // Define a Role direto (este método é só para clientes)
        usuario.setAtivo(true);
        // (O campo 'nome' FOI REMOVIDO daqui - CORRIGE O ERRO `setNome`)
        
        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        // 3. Criar a entidade de Perfil (Cliente)
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuarioSalvo); // <-- Conecta ao Usuário
        cliente.setId(usuarioSalvo.getId()); // <-- Define a PK/FK (@MapsId)
        
        cliente.setNome(dto.getNome()); // <-- 'nome' agora fica no Cliente
        cliente.setCpf(dto.getCpf());
        cliente.setTelefone(dto.getTelefone());
        
        // (Não setamos email/ativo aqui - CORRIGE OS ERROS `setEmail`/`setAtivo`)
        
        clienteRepository.save(cliente);

        // 4. Criar a entidade de Endereço (Gargalo 1)
        EnderecoDTO enderecoDTO = dto.getEndereco();
        Endereco endereco = modelMapper.map(enderecoDTO, Endereco.class);
        endereco.setUsuario(usuarioSalvo); // <-- Conecta o endereço ao Usuário
        endereco.setApelido("Principal"); // Define o primeiro endereço como 'Principal'
        
        enderecoRepository.save(endereco);

        // Retorna o Usuário salvo (agora completo com perfil e endereço)
        return usuarioSalvo;
    }

    // O método 'criarUsuario(RegisterRequest)' antigo (que recebia Role) 
    // foi substituído por 'registrarCliente(RegisterRequest)'.
    // Se você precisar de um método para criar ADMIN/RESTAURANTE, 
    // ele deve ser um método separado.

    
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