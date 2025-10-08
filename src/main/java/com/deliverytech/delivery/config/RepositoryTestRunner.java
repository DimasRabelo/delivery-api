package com.deliverytech.delivery.config;

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;

//import java.math.BigDecimal;
//import java.time.LocalDateTime;
import java.util.List;

// ATENÇÃO: Classe de teste dos repositories.
// O "@Component" foi comentado para não interferir nos dados da aplicação
// durante a execução do DataLoader ou em produção.
public class RepositoryTestRunner implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public RepositoryTestRunner(ClienteRepository clienteRepository,
                                RestauranteRepository restauranteRepository,
                                ProdutoRepository produtoRepository,
                                PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.restauranteRepository = restauranteRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== TESTE BÁSICO DOS REPOSITORIES ===");

        // --- ClienteRepository ---
        System.out.println("--- ClienteRepository ---");
        clienteRepository.findByEmail("teste@email.com")
                .ifPresentOrElse(
                        c -> System.out.println("Cliente: " + c.getNome() + ", Email: " + c.getEmail()),
                        () -> System.out.println("findByEmail: Optional.empty")
                );

        boolean exists = clienteRepository.existsByEmail("teste@email.com");
        System.out.println("existsByEmail: " + exists);

        List<Cliente> clientesAtivos = clienteRepository.findByAtivoTrue();
        clientesAtivos.forEach(c -> System.out.println("Cliente ativo: " + c.getNome() + ", Email: " + c.getEmail()));

        // --- RestauranteRepository ---
        System.out.println("--- RestauranteRepository ---");
        List<Restaurante> restaurantesAtivos = restauranteRepository.findByAtivoTrue();
        restaurantesAtivos.forEach(r -> System.out.println("Restaurante: " + r.getNome() + ", Categoria: " + r.getCategoria()));

        // --- ProdutoRepository ---
        System.out.println("--- ProdutoRepository ---");
        List<Produto> produtos = produtoRepository.findAll();
        produtos.forEach(p -> System.out.println("Produto: " + p.getNome() + ", Preço: " + p.getPreco()));

        // --- PedidoRepository ---
        System.out.println("--- PedidoRepository ---");
        List<Pedido> pedidos = pedidoRepository.findAll();
        pedidos.forEach(p -> System.out.println("Pedido: " + p.getNumeroPedido() + ", Valor total: " + p.getValorTotal()));

        System.out.println("=== FIM DOS TESTES ===");
    }
}
