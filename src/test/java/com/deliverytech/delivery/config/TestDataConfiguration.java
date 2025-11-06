package com.deliverytech.delivery.config;

// (Imports... OK)
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

@Configuration
@Profile("test")
public class TestDataConfiguration {

    // (Repositórios e Construtor... OK)
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;
    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataConfiguration(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            RestauranteRepository restauranteRepository,
            UsuarioRepository usuarioRepository,
            EnderecoRepository enderecoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void setupTestData() {

        // 1️⃣ Limpa os dados na ordem correta
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        enderecoRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        // ----------------------------------------------------
        // CRIAÇÃO DO CLIENTE (MÉTODO CASCADE - FUNCIONOU)
        // ----------------------------------------------------

        // 2️⃣ Cria o USUÁRIO CLIENTE (em memória)
        Usuario usuarioCliente = new Usuario();
        usuarioCliente.setEmail("joao.teste@email.com");
        usuarioCliente.setSenha(passwordEncoder.encode("123456"));
        usuarioCliente.setRole(Role.CLIENTE);
        usuarioCliente.setAtivo(true);

        // 3️⃣ Cria o CLIENTE (em memória)
        Cliente cliente = new Cliente();
        cliente.setNome("João Cliente");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");

        // 4️⃣ Cria o ENDEREÇO (em memória, COM VALIDAÇÃO)
        Endereco enderecoCliente = new Endereco();
        enderecoCliente.setApelido("Casa Teste");
        enderecoCliente.setRua("Rua dos Testes");
        enderecoCliente.setNumero("123");
        enderecoCliente.setCep("01001000");
        enderecoCliente.setBairro("Centro");
        enderecoCliente.setCidade("Cidade Teste");
        enderecoCliente.setEstado("SP");

        // 5️⃣ CONECTA TUDO (Bidirecional)
        cliente.setUsuario(usuarioCliente);
        enderecoCliente.setUsuario(usuarioCliente);
        usuarioCliente.setCliente(cliente);
        usuarioCliente.getEnderecos().add(enderecoCliente);

        // 6️⃣ SALVA SÓ O PAI (USUÁRIO)
        usuarioRepository.save(usuarioCliente); // 🔥 Salva o usuário e seus filhos (Cliente e Endereco)

        // ----------------------------------------------------
        // CRIAÇÃO DO RESTAURANTE (MÉTODO HÍBRIDO - V15)
        // (Isso corrige o 'TransientPropertyValueException' E o 'Telefone é obrigatório')
        // ----------------------------------------------------

        // 7A 🚀 Cria e SALVA o USUÁRIO DONO (para que ele não seja "transient")
        Usuario usuarioRestaurante = new Usuario();
        usuarioRestaurante.setEmail("restaurante.dono@email.com");
        usuarioRestaurante.setSenha(passwordEncoder.encode("123456"));
        usuarioRestaurante.setRole(Role.RESTAURANTE);
        usuarioRestaurante.setAtivo(true);
        Usuario donoSalvo = usuarioRepository.save(usuarioRestaurante); // Salva e pega a instância

        // 7B 🚀 Cria o ENDEREÇO (em memória, COM VALIDAÇÃO)
        Endereco endRestaurante = new Endereco();
        endRestaurante.setApelido("Restaurante Teste");
        endRestaurante.setRua("Rua Fictícia");
        endRestaurante.setNumero("456");
        endRestaurante.setCep("02002000");
        endRestaurante.setBairro("Bairro Central");
        endRestaurante.setCidade("São Paulo");
        endRestaurante.setEstado("SP");
        
        // 7C 🚀 Cria o RESTAURANTE (em memória, COM VALIDAÇÃO)
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Teste");
        restaurante.setCategoria("Pizzaria");
        restaurante.setAtivo(true);
        restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restaurante.setTelefone("11888889999"); // <-- 🔥 CORREÇÃO FINAL (Telefone obrigatório)

        // 7D 🚀 CONECTA TUDO (Restaurante)
        endRestaurante.setUsuario(donoSalvo); // Endereço aponta para o Dono (que já está salvo)
        restaurante.setEndereco(endRestaurante); // Restaurante aponta para o Endereço (que é novo/transient)

        // 7E 🚀 SALVA SÓ O RESTAURANTE
        // O cascade do Restaurante deve salvar o endRestaurante.
        // O endRestaurante será salvo com o link para donoSalvo (que já existe no BD).
        Restaurante restauranteSalvo = restauranteRepository.save(restaurante); // 🔥 Salva Restaurante -> Endereco

        // 7F 🚀 Cria o PRODUTO
        Produto produto = new Produto();
        produto.setNome("Pizza Teste");
        produto.setDescricao("Pizza de calabresa de teste");
        produto.setPrecoBase(BigDecimal.valueOf(29.90));
        produto.setEstoque(20);
        produto.setDisponivel(true);
		produto.setRestaurante(restauranteSalvo); // Usa a instância salva pelo cascade
        produtoRepository.save(produto);

        System.out.println("✅ Dados de teste (VERSÃO V15 - FINAL) criados com sucesso!");
    }
}