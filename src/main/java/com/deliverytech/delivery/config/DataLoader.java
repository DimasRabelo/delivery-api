package com.deliverytech.delivery.config;

import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== INICIANDO CARGA DE DADOS DE TESTE ===");

        // Limpar dados existentes
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        clienteRepository.deleteAll();

        // Inserir dados de teste
        inserirClientes();
        inserirRestaurantes();
        inserirProdutos();
        inserirPedidos();

        // Testar consultas
        testarConsultas();

        System.out.println("=== CARGA DE DADOS CONCLUÍDA ===");
    }

    // ==========================
    // Inserir Clientes
    // ==========================
    private void inserirClientes() {
        System.out.println("--- Inserindo Clientes ---");

        Cliente cliente1 = new Cliente();
        cliente1.setNome("João Silva");
        cliente1.setEmail("joao@email.com");
        cliente1.setTelefone("11999999999");
        cliente1.setEndereco("Rua A, 123");
        cliente1.setAtivo(true);

        Cliente cliente2 = new Cliente();
        cliente2.setNome("Maria Santos");
        cliente2.setEmail("maria@email.com");
        cliente2.setTelefone("11888888888");
        cliente2.setEndereco("Rua B, 456");
        cliente2.setAtivo(true);

        Cliente cliente3 = new Cliente();
        cliente3.setNome("Pedro Oliveira");
        cliente3.setEmail("pedro@email.com");
        cliente3.setTelefone("11777777777");
        cliente3.setEndereco("Rua C, 789");
        cliente3.setAtivo(false);

        clienteRepository.saveAll(Arrays.asList(cliente1, cliente2, cliente3));
        System.out.println("✓ 3 clientes inseridos");
    }

    // ==========================
    // Inserir Restaurantes
    // ==========================
    private void inserirRestaurantes() {
        System.out.println("--- Inserindo Restaurantes ---");

        Restaurante restaurante1 = new Restaurante();
        restaurante1.setNome("Pizza Express");
        restaurante1.setCategoria("Italiana");
        restaurante1.setEndereco("Av. Principal, 100");
        restaurante1.setTelefone("1133333333");
        restaurante1.setTaxaEntrega(new BigDecimal("3.50"));
        restaurante1.setAvaliacao(new BigDecimal("4.5"));
        restaurante1.setAtivo(true);

        Restaurante restaurante2 = new Restaurante();
        restaurante2.setNome("Burger King");
        restaurante2.setCategoria("Fast Food");
        restaurante2.setEndereco("Rua Central, 200");
        restaurante2.setTelefone("1144444444");
        restaurante2.setTaxaEntrega(new BigDecimal("5.00"));
        restaurante2.setAvaliacao(new BigDecimal("4.0"));
        restaurante2.setAtivo(true);

        restauranteRepository.saveAll(Arrays.asList(restaurante1, restaurante2));
        System.out.println("✓ 2 restaurantes inseridos");
    }

    // ==========================
    // Inserir Produtos
    // ==========================
    private void inserirProdutos() {
        System.out.println("--- Inserindo Produtos ---");

        Restaurante pizzaExpress = restauranteRepository.findByNome("Pizza Express").orElseThrow();
        Restaurante burgerKing = restauranteRepository.findByNome("Burger King").orElseThrow();

        List<Produto> produtos = Arrays.asList(
    criarProduto("Pizza Margherita", new BigDecimal("25.00"), true, pizzaExpress, "Pizza de mussarela com manjericão"),
    criarProduto("Pizza Calabresa", new BigDecimal("28.00"), true, pizzaExpress, "Pizza de calabresa com cebola"),
    criarProduto("Hambúrguer Clássico", new BigDecimal("20.00"), true, burgerKing, "Hambúrguer com alface, tomate e queijo"),
    criarProduto("Batata Frita", new BigDecimal("10.00"), true, burgerKing, "Batata frita crocante"),
    criarProduto("Refrigerante", new BigDecimal("5.00"), true, burgerKing, "Refrigerante lata 350ml")
);

        produtoRepository.saveAll(produtos);
        System.out.println("✓ 5 produtos inseridos");
    }

   private Produto criarProduto(String nome, BigDecimal preco, boolean disponivel, Restaurante restaurante, String descricao) {
    Produto produto = new Produto();
    produto.setNome(nome);
    produto.setPreco(preco);
    produto.setDisponivel(disponivel);
    produto.setRestaurante(restaurante);
    produto.setCategoria(restaurante.getCategoria()); 
    produto.setDescricao(descricao); // <- adicionando descrição
    
    return produto;
}
    // ==========================
    // Inserir Pedidos
    // ==========================
    private void inserirPedidos() {
        System.out.println("--- Inserindo Pedidos ---");

        Cliente joao = clienteRepository.findByEmail("joao@email.com").orElseThrow();
        Cliente maria = clienteRepository.findByEmail("maria@email.com").orElseThrow();

        Restaurante pizzaExpress = restauranteRepository.findByNome("Pizza Express").orElseThrow();
        Restaurante burgerKing = restauranteRepository.findByNome("Burger King").orElseThrow();

        Produto pizzaMargherita = produtoRepository.findByNome("Pizza Margherita").orElseThrow();
        Produto batataFrita = produtoRepository.findByNome("Batata Frita").orElseThrow();
        Produto refrigerante = produtoRepository.findByNome("Refrigerante").orElseThrow();

        // Pedido 1
        Pedido pedido1 = new Pedido();
        pedido1.setCliente(joao);
        pedido1.setRestaurante(pizzaExpress);
        pedido1.setStatus(StatusPedido.PENDENTE);
        pedido1.setEnderecoEntrega(joao.getEndereco());
        pedido1.setTaxaEntrega(pizzaExpress.getTaxaEntrega());
        pedido1.setNumeroPedido("PED001");
        pedido1.setDataPedido(LocalDateTime.now());
        pedido1.setObservacoes("Sem cebola, por favor");

        ItemPedido item1 = new ItemPedido(pizzaMargherita, 1);
        pedido1.adicionarItem(item1);

        // Pedido 2
        Pedido pedido2 = new Pedido();
        pedido2.setCliente(maria);
        pedido2.setRestaurante(burgerKing);
        pedido2.setStatus(StatusPedido.CONFIRMADO);
        pedido2.setEnderecoEntrega(maria.getEndereco());
        pedido2.setTaxaEntrega(burgerKing.getTaxaEntrega());
        pedido2.setNumeroPedido("PED002");
        pedido2.setDataPedido(LocalDateTime.now());
        pedido2.setObservacoes("Entrega rápida, se possível");

        ItemPedido item2 = new ItemPedido(batataFrita, 2);
        ItemPedido item3 = new ItemPedido(refrigerante, 2);

        pedido2.adicionarItem(item2);
        pedido2.adicionarItem(item3);

        pedidoRepository.saveAll(Arrays.asList(pedido1, pedido2));
        System.out.println("✓ 2 pedidos inseridos");
    }

    // ==========================
    // Testar Consultas
    // ==========================
    private void testarConsultas() {
        System.out.println("\n=== TESTANDO CONSULTAS DOS REPOSITORIES ===");

        // ClienteRepository
        System.out.println("\n--- Testes ClienteRepository ---");
        var clientePorEmail = clienteRepository.findByEmail("joao@email.com");
        System.out.println("Cliente por email: " + (clientePorEmail.isPresent() ? clientePorEmail.get().getNome() : "Não encontrado"));

        var clientesAtivos = clienteRepository.findByAtivoTrue();
        System.out.println("Clientes ativos: " + clientesAtivos.size());

        var clientesPorNome = clienteRepository.findByNomeContainingIgnoreCase("silva");
        System.out.println("Clientes com 'silva' no nome: " + clientesPorNome.size());

        boolean emailExiste = clienteRepository.existsByEmail("maria@email.com");
        System.out.println("Email maria@email.com existe: " + emailExiste);

        // RestauranteRepository
        System.out.println("\n--- Testes RestauranteRepository ---");
        var restaurantesAtivos = restauranteRepository.findByAtivoTrue();
        System.out.println("Restaurantes ativos: " + restaurantesAtivos.size());

        var restaurantesItaliana = restauranteRepository.findByCategoria("Italiana");
        System.out.println("Restaurantes categoria 'Italiana': " + restaurantesItaliana.size());

        // ProdutoRepository
        System.out.println("\n--- Testes ProdutoRepository ---");
        var produtosPizzaExpress = produtoRepository.findByRestauranteNome("Pizza Express");
        System.out.println("Produtos Pizza Express: " + produtosPizzaExpress.size());

        var produtosDisponiveis = produtoRepository.findByDisponivelTrue();
        System.out.println("Produtos disponíveis: " + produtosDisponiveis.size());

        var produtosPreco20 = produtoRepository.findByPrecoLessThanEqualAndDisponivelTrue(new BigDecimal("20"));
        System.out.println("Produtos com preço <= 20: " + produtosPreco20.size());

        // PedidoRepository
        System.out.println("\n--- Testes PedidoRepository ---");
        var pedidosPendentes = pedidoRepository.findByStatusOrderByDataPedidoDesc(StatusPedido.PENDENTE);
        System.out.println("Pedidos pendentes: " + pedidosPendentes.size());

        var pedidosJoao = pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clientePorEmail.get().getId());
        System.out.println("Pedidos do João: " + pedidosJoao.size());

        var top10Pedidos = pedidoRepository.findTop10ByOrderByDataPedidoDesc();
        System.out.println("Top 10 pedidos: " + top10Pedidos.size());
    }
}
