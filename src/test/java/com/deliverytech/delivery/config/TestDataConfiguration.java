package com.deliverytech.delivery.config; // (Mude para o seu pacote, se for diferente)

// Imports das suas entidades
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.enums.StatusPedido;

// Imports dos seus repositórios (Note que SÓ temos repositórios das entidades-RAIZ)
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
 * Popula o banco de dados em memória (H2) com o cenário exato
 * necessário para recriar o BUG-500.
 *
 * ESTA VERSÃO USA O CASCADE PARA SALVAR OS ITENS E OPCIONAIS.
 */
@Configuration
@Profile("test")
public class TestDataConfiguration {

    // --- REPOSITÓRIOS ---
    // (Apenas os repositórios das "raízes" dos agregados)
   
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
     * Assumindo que spring.jpa.hibernate.ddl-auto=create-drop
     * está no application-test.properties, o banco já está limpo.
     */
    @PostConstruct
    public void setupTestData() {

        // --- 1. CRIAÇÃO DO CLIENTE ---
        Usuario usuarioCliente = new Usuario();
        usuarioCliente.setEmail("joao.teste@email.com");
        usuarioCliente.setSenha(passwordEncoder.encode("123456"));
        usuarioCliente.setRole(Role.CLIENTE);
        usuarioCliente.setAtivo(true);

        Cliente cliente = new Cliente();
        cliente.setNome("João Cliente");
        cliente.setCpf("51613751036");
        cliente.setTelefone("11999999999");

        Endereco enderecoCliente = new Endereco();
        enderecoCliente.setApelido("Casa Teste");
        enderecoCliente.setRua("Rua dos Testes");
        enderecoCliente.setNumero("123");
        enderecoCliente.setCep("01001000");
        enderecoCliente.setBairro("Centro");
        enderecoCliente.setCidade("Cidade Teste");
        enderecoCliente.setEstado("SP");

        cliente.setUsuario(usuarioCliente);
        enderecoCliente.setUsuario(usuarioCliente);
        usuarioCliente.setCliente(cliente);
        usuarioCliente.getEnderecos().add(enderecoCliente);

        Usuario usuarioClienteSalvo = usuarioRepository.save(usuarioCliente);
        Endereco enderecoClienteSalvo = usuarioClienteSalvo.getEnderecos().get(0);


        // --- 2. CRIAÇÃO DO RESTAURANTE ---
        Usuario usuarioRestaurante = new Usuario();
        usuarioRestaurante.setEmail("restaurante.dono@email.com");
        usuarioRestaurante.setSenha(passwordEncoder.encode("123456"));
        usuarioRestaurante.setRole(Role.RESTAURANTE);
        usuarioRestaurante.setAtivo(true);
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

        endRestaurante.setUsuario(donoSalvo);
        restaurante.setEndereco(endRestaurante);

        Restaurante restauranteSalvo = restauranteRepository.save(restaurante);

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
        // 🔥 INÍCIO DO CENÁRIO DO BUG (Opcional Duplicado)
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
        BigDecimal precoOpcionais = new BigDecimal("5.00"); // 2.50 + 2.50
        BigDecimal precoBase = produtoSalvo.getPrecoBase();
        BigDecimal precoUnitarioCalculado = precoBase.add(precoOpcionais);

        // --- Opcional 1 (Maionese)
        ItemPedidoOpcional opcional1 = new ItemPedidoOpcional();
        opcional1.setItemOpcional(itemMaionese);
        opcional1.setPrecoRegistrado(itemMaionese.getPrecoAdicional());

        // --- 🔥 Opcional 2 (Maionese) - A DUPLICATA
        ItemPedidoOpcional opcional2 = new ItemPedidoOpcional();
        opcional2.setItemOpcional(itemMaionese); // Mesmo item opcional
        opcional2.setPrecoRegistrado(itemMaionese.getPrecoAdicional());

        // --- Item do Pedido (A "Pizza")
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setProduto(produtoSalvo);
        itemPedido.setQuantidade(1);
        itemPedido.setPrecoUnitario(precoUnitarioCalculado); // 34.90
        itemPedido.setSubtotal(precoUnitarioCalculado); // 34.90
        
        // Conecta os opcionais ao item
        opcional1.setItemPedido(itemPedido);
        opcional2.setItemPedido(itemPedido);
        itemPedido.getOpcionaisSelecionados().add(opcional1);
        itemPedido.getOpcionaisSelecionados().add(opcional2); // Adiciona a duplicata na List

        // --- O Pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(UUID.randomUUID().toString());
        pedido.setCliente(usuarioClienteSalvo.getCliente());
        pedido.setRestaurante(restauranteSalvo);
        pedido.setEnderecoEntrega(enderecoClienteSalvo);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setMetodoPagamento("PIX"); // Corrigido para String
        pedido.setTaxaEntrega(restauranteSalvo.getTaxaEntrega());
        pedido.setSubtotal(precoUnitarioCalculado); // 34.90
        pedido.setValorTotal(precoUnitarioCalculado.add(pedido.getTaxaEntrega())); // 44.90
        
        // Conecta o item ao pedido
        itemPedido.setPedido(pedido);
        pedido.getItens().add(itemPedido); // Assumindo que Pedido tem getItens().add() e Cascade.ALL

        // --- 6. SALVA O PEDIDO (e salva TUDO em cascata) ---
        pedidoRepository.save(pedido);

        System.out.println("✅ Dados de teste e Cenário do BUG-500 criados com sucesso!");
    }
}