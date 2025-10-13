package com.deliverytech.delivery.repository;

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class RepositoriesTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Test
    public void cenario1_BuscaClientePorEmail() {
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail("joao@email.com");

        System.out.println("🔎 Cenário 1: Busca de Cliente por Email");
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            System.out.println("Nome: " + cliente.getNome());
            System.out.println("Email: " + cliente.getEmail());
            System.out.println("Telefone: " + cliente.getTelefone());
            System.out.println("Endereço: " + cliente.getEndereco());
            System.out.println("Resultado Esperado: Cliente encontrado com dados corretos.");
        } else {
            System.out.println("Cliente não encontrado!");
        }
        System.out.println("----------------------------\n");
    }

    @Test
    public void cenario2_ProdutosPorRestaurante() {
       List<Produto> produtos = produtoRepository.findByRestauranteIdAndDisponivelTrue(1L);


        System.out.println("🍔 Cenário 2: Produtos por Restaurante");
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto encontrado para o restaurante 1!");
        } else {
            produtos.forEach(p -> System.out.println(
                    "Produto: " + p.getNome() + " | Preço: " + p.getPreco() + " | Disponível: " + p.getDisponivel()
            ));
            System.out.println("Resultado Esperado: Lista de produtos do restaurante específico.");
        }
        System.out.println("----------------------------\n");
    }

    @Test
    public void cenario3_PedidosRecentes() {
        List<Pedido> pedidos = pedidoRepository.findTop10ByOrderByDataPedidoDesc();

        System.out.println("📅 Cenário 3: Pedidos Recentes");
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido encontrado!");
        } else {
            pedidos.forEach(p -> System.out.println(
                    "Pedido nº: " + p.getNumeroPedido() + " | Data: " + p.getDataPedido() + " | Status: " + p.getStatus()
            ));
            System.out.println("Resultado Esperado: 10 pedidos mais recentes ordenados por data.");
        }
        System.out.println("----------------------------\n");
    }

    @Test
    public void cenario4_RestaurantesPorTaxa() {
        List<Restaurante> restaurantes = restauranteRepository.findByTaxaEntregaLessThanEqual(new BigDecimal("5.00"));

        System.out.println("💰 Cenário 4: Restaurantes por Taxa");
        if (restaurantes.isEmpty()) {
            System.out.println("Nenhum restaurante encontrado com taxa de entrega <= 5.00!");
        } else {
            restaurantes.forEach(r -> System.out.println(
                    "Restaurante: " + r.getNome() + " | Taxa de entrega: " + r.getTaxaEntrega()
            ));
            System.out.println("Resultado Esperado: Restaurantes filtrados pela taxa de entrega.");
        }
        System.out.println("----------------------------\n");
    }
}
