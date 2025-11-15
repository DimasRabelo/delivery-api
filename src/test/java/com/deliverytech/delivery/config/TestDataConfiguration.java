package com.deliverytech.delivery.config; 

// Imports das suas entidades
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.enums.StatusPedido;

// Imports dos seus repositórios (Apenas os das entidades-RAIZ)
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;

// Imports do Spring e outros
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Configuração que roda APENAS com o profile "test".
 * Popula o banco de dados em memória (H2) com o cenário inicial
 * e o cenário específico do BUG de opcional duplicado (BUG-500).
 */
@Configuration
@Profile("test")
public class TestDataConfiguration {

    // --- REPOSITÓRIOS ---
    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PedidoRepository pedidoRepository;
    private final GrupoOpcionalRepository grupoOpcionalRepository;
    private final ItemOpcionalRepository itemOpcionalRepository;

    // --- CONSTRUTOR ---
    public TestDataConfiguration(
            ClienteRepository clienteRepository, 
            ProdutoRepository produtoRepository,
            RestauranteRepository restauranteRepository,
            UsuarioRepository usuarioRepository,
            EnderecoRepository enderecoRepository, 
            PasswordEncoder passwordEncoder,
            PedidoRepository pedidoRepository,
            GrupoOpcionalRepository grupoOpcionalRepository,
            ItemOpcionalRepository itemOpcionalRepository
    ) {
        
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.pedidoRepository = pedidoRepository;
        this.grupoOpcionalRepository = grupoOpcionalRepository;
        this.itemOpcionalRepository = itemOpcionalRepository;
    }

    /**
     * Método executado assim que a aplicação (de teste) sobe.
     */
    @PostConstruct
    public void setupTestData() {

        // --- 1. CRIAÇÃO DO CLIENTE (Usuário Joao) ---
        Usuario usuarioCliente = new Usuario();
        usuarioCliente.setEmail("joao.teste@email.com");
        usuarioCliente.setSenha(passwordEncoder.encode("123456"));
        usuarioCliente.setRole(Role.CLIENTE);
        usuarioCliente.setAtivo(true);
        usuarioCliente.setNome("João Cliente"); 

        Cliente cliente = new Cliente();
        cliente.setNome("João Cliente");
       cliente.setCpf("39053344705");
        cliente.setTelefone("11999999999");

        Endereco enderecoCliente = new Endereco();
        enderecoCliente.setApelido("Casa Teste");
        enderecoCliente.setRua("Rua dos Testes");
        enderecoCliente.setNumero("123");
        enderecoCliente.setCep("01001000");
        enderecoCliente.setBairro("Centro");
        enderecoCliente.setCidade("Cidade Teste");
        enderecoCliente.setEstado("SP");

        // Associações
        cliente.setUsuario(usuarioCliente);
        enderecoCliente.setUsuario(usuarioCliente);
        usuarioCliente.setCliente(cliente);
        usuarioCliente.getEnderecos().add(enderecoCliente);

        Usuario usuarioClienteSalvo = usuarioRepository.save(usuarioCliente);
        Endereco enderecoClienteSalvo = usuarioClienteSalvo.getEnderecos().get(0);


        // --- 2. CRIAÇÃO DO RESTAURANTE (Pizzaria) ---
        Usuario usuarioRestaurante = new Usuario();
        usuarioRestaurante.setEmail("restaurante.dono@email.com");
        usuarioRestaurante.setSenha(passwordEncoder.encode("123456"));
        usuarioRestaurante.setRole(Role.RESTAURANTE);
        usuarioRestaurante.setAtivo(true);
        usuarioRestaurante.setNome("Dono Restaurante Teste");
        Usuario donoSalvo = usuarioRepository.save(usuarioRestaurante);

        Endereco endRestaurante = new Endereco();
        endRestaurante.setApelido("Restaurante Teste");
        endRestaurante.setRua("Rua Fictícia");
        endRestaurante.setNumero("456");
        endRestaurante.setCep("02002000");
        endRestaurante.setBairro("Bairro Central");
        endRestaurante.setCidade("São Paulo");
        endRestaurante.setEstado("SP");

        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Teste");
        restaurante.setCategoria("Pizzaria");
        restaurante.setAtivo(true);
        restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restaurante.setTelefone("11888889999");
        
        // Associações
        endRestaurante.setUsuario(donoSalvo);
        restaurante.setEndereco(endRestaurante);
        donoSalvo.setRestaurante(restaurante); 

        Restaurante restauranteSalvo = restauranteRepository.save(restaurante);
        usuarioRepository.save(donoSalvo); 

        // --- 3. CRIAÇÃO DO PRODUTO ---
        Produto produto = new Produto();
        produto.setNome("Pizza Teste");
        produto.setDescricao("Pizza de calabresa de teste");
        produto.setPrecoBase(BigDecimal.valueOf(29.90));
        produto.setEstoque(20);
        produto.setDisponivel(true);
        produto.setRestaurante(restauranteSalvo);
        Produto produtoSalvo = produtoRepository.save(produto);


        // ================================================================
        // 🔥 INÍCIO DO CENÁRIO DO BUG (Opcional Duplicado - Para Testes de Integração)
        // ================================================================
        System.out.println("Criando cenário do BUG-500 (Opcional Duplicado)...");

        // --- 4. CRIA GRUPO E ITEM OPCIONAL ---
        GrupoOpcional grupoMolho = new GrupoOpcional();
        grupoMolho.setNome("Molho Extra");
        grupoMolho.setProduto(produtoSalvo);
        grupoMolho.setMinSelecao(0);
        grupoMolho.setMaxSelecao(3);
        grupoMolho = grupoOpcionalRepository.save(grupoMolho);

        ItemOpcional itemMaionese = new ItemOpcional();
        itemMaionese.setNome("Maionese Teste");
        itemMaionese.setPrecoAdicional(new BigDecimal("2.50"));
        itemMaionese.setGrupoOpcional(grupoMolho);
        itemMaionese = itemOpcionalRepository.save(itemMaionese);

        // --- 5. CRIA OS OBJETOS EM MEMÓRIA (Pedido, Item, Opcionais) ---

        // Preços (base + 2x maionese)
        BigDecimal precoOpcionais = new BigDecimal("5.00"); 
        BigDecimal precoBase = produtoSalvo.getPrecoBase();
        BigDecimal precoUnitarioCalculado = precoBase.add(precoOpcionais); 

        // --- Opcional 1 (Maionese)
        ItemPedidoOpcional opcional1 = new ItemPedidoOpcional();
        opcional1.setItemOpcional(itemMaionese);
        opcional1.setPrecoRegistrado(itemMaionese.getPrecoAdicional());

        // --- 🔥 Opcional 2 (Maionese) - A DUPLICATA
        ItemPedidoOpcional opcional2 = new ItemPedidoOpcional();
        opcional2.setItemOpcional(itemMaionese); 
        opcional2.setPrecoRegistrado(itemMaionese.getPrecoAdicional());

        // --- Item do Pedido (A "Pizza")
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setProduto(produtoSalvo);
        itemPedido.setQuantidade(1);
        itemPedido.setPrecoUnitario(precoUnitarioCalculado); 
        itemPedido.setSubtotal(precoUnitarioCalculado); 
        
        // Conecta os opcionais ao item
        opcional1.setItemPedido(itemPedido);
        opcional2.setItemPedido(itemPedido);
        itemPedido.getOpcionaisSelecionados().add(opcional1);
        itemPedido.getOpcionaisSelecionados().add(opcional2); 

        // --- O Pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(UUID.randomUUID().toString());
        pedido.setCliente(usuarioClienteSalvo.getCliente());
        pedido.setRestaurante(restauranteSalvo);
        pedido.setEnderecoEntrega(enderecoClienteSalvo);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setMetodoPagamento("PIX"); 
        pedido.setTaxaEntrega(restauranteSalvo.getTaxaEntrega()); 
        pedido.setSubtotal(precoUnitarioCalculado); 
        pedido.setValorTotal(precoUnitarioCalculado.add(pedido.getTaxaEntrega())); 
        
        // Conecta o item ao pedido
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido); 

        // --- 6. SALVA O PEDIDO (e salva TUDO em cascata) ---
        pedidoRepository.save(pedido);
        donoSalvo.setRestaurante(null); 
        usuarioRepository.save(donoSalvo);

        System.out.println("✅ Dados de teste e Cenário do BUG-500 criados com sucesso!");
    }
}