package com.deliverytech.delivery.config;

import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration; // MUDANÇA
import org.springframework.context.annotation.Profile;       // MUDANÇA
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Configuration // MUDANÇA AQUI
@Profile("test") // MUDANÇA AQUI
public class TestDataConfiguration {

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;

    public TestDataConfiguration(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            RestauranteRepository restauranteRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    @PostConstruct
    @Transactional
    public void setupTestData() {
        // Limpa dados anteriores
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        clienteRepository.deleteAll();

        // Cria cliente de teste
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setEmail("joao.teste@email.com");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        // Cria restaurante de teste
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Teste");
        restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restaurante.setAtivo(true);
        restauranteRepository.save(restaurante);

        // Cria produto de teste vinculado ao restaurante
        Produto produto = new Produto();
        produto.setNome("Pizza Teste");
        produto.setDescricao("Pizza para testes");
        produto.setPreco(BigDecimal.valueOf(29.90));
        produto.setEstoque(50);
        produto.setDisponivel(true);
        produto.setRestaurante(restaurante);
        produtoRepository.save(produto);
    }
}