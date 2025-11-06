package com.deliverytech.delivery.config;

// --- IMPORTS ADICIONADOS ---
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
// --- FIM DOS IMPORTS ADICIONADOS ---

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Configuration
@Profile("test")
public class TestDataConfiguration {

    // --- REPOSITÓRIOS ANTIGOS (OK) ---
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;

    // --- NOVOS REPOSITÓRIOS (NECESSÁRIOS) ---
    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Injeção de dependência via construtor (ATUALIZADO).
     */
    public TestDataConfiguration(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            RestauranteRepository restauranteRepository,
            UsuarioRepository usuarioRepository, // <-- ADICIONADO
            EnderecoRepository enderecoRepository, // <-- ADICIONADO
            PasswordEncoder passwordEncoder // <-- ADICIONADO
    ) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Método de setup (VERSÃO REFATORADA).
     * Cria os dados de teste seguindo a nova arquitetura (Gargalos 1, 2 e 4).
     */
    @PostConstruct
    @Transactional
    public void setupTestData() {
        
        // 1. Limpa os dados na ordem correta
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll(); // (O cascade deve limpar o Endereco do restaurante)
        // (O cascade deve limpar Cliente e Enderecos do cliente)
        usuarioRepository.deleteAll(); 
        
        // (Limpa explicitamente Endereco e Cliente caso cascade não funcione nos testes)
        enderecoRepository.deleteAll(); 
        clienteRepository.deleteAll();


        // --- 2. Cria e salva um Cliente (Gargalos 1 e 4 CORRIGIDOS) ---
        
        // 2a. Cria o Usuário (Autenticação)
        Usuario usuario = new Usuario();
        usuario.setEmail("joao.teste@email.com");
        usuario.setSenha(passwordEncoder.encode("123456")); // Senha deve ser criptografada
        usuario.setRole(Role.CLIENTE);
        usuario.setAtivo(true);
        
        // 2b. Cria o Cliente (Perfil)
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");
        
        // 2c. Cria o Endereço (Entrega)
        Endereco endCliente = new Endereco();
        endCliente.setApelido("Casa");
        endCliente.setCep("01001000");
        endCliente.setRua("Praça da Sé");
        endCliente.setNumero("100");
        endCliente.setBairro("Sé");
        endCliente.setCidade("São Paulo");
        endCliente.setEstado("SP");

        // 2d. Conecta todos eles
        cliente.setUsuario(usuario);
        cliente.setId(usuario.getId()); // (Necessário se não houver @MapsId)
        
        endCliente.setUsuario(usuario);
        
        // (Assume que @OneToOne e @OneToMany em Usuario têm cascade = ALL)
        usuario.setCliente(cliente); 
        usuario.getEnderecos().add(endCliente);
        
        // 2e. Salva o Usuário (o Cascade deve salvar Cliente e Endereco juntos)
        usuarioRepository.save(usuario);


        // --- 3. Cria e salva um Restaurante (Gargalo 1 CORRIGIDO) ---
        
        // 3a. Cria o Endereço do Restaurante
        Endereco endRestaurante = new Endereco();
        endRestaurante.setCep("02002000");
        endRestaurante.setRua("Rua Fictícia de Teste");
        endRestaurante.setNumero("123");
        endRestaurante.setBairro("Bairro Teste");
        endRestaurante.setCidade("São Paulo");
        endRestaurante.setEstado("SP");
        
        // 3b. Cria o Restaurante
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Teste");
        restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restaurante.setAtivo(true);
        restaurante.setTelefone("999999999");
        restaurante.setCategoria("Pizzaria");
        
        // 3c. Conecta o Restaurante ao seu Endereço
        restaurante.setEndereco(endRestaurante);
        
        // 3d. Salva o Restaurante (o Cascade deve salvar o Endereço junto)
        restauranteRepository.save(restaurante);


        // --- 4. Cria e salva um Produto (Gargalo 2 CORRIGIDO) ---
        Produto produto = new Produto();
        produto.setNome("Pizza Teste");
        produto.setDescricao("Pizza para testes");
        produto.setEstoque(50);
        produto.setDisponivel(true);
        produto.setRestaurante(restaurante); 
        
        // 4a. CORREÇÃO: Usa 'setPrecoBase'
        produto.setPrecoBase(BigDecimal.valueOf(29.90));
        
        produtoRepository.save(produto);
    }
}