package com.deliverytech.delivery.config;

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

/**
 * 💡 Classe de Configuração para popular o banco de dados de teste.
 * * A anotação @Configuration informa ao Spring para processar esta classe
 * em busca de beans ou configurações.
 * * A anotação @Profile("test") é a parte mais importante:
 * Ela garante que esta classe SÓ será carregada e executada quando o
 * perfil "test" estiver ativo (ex: no seu 'application-test.properties'
 * e nas anotações @ActiveProfiles("test") dos seus testes de integração).
 * * Isso evita que esses dados de teste sejam carregados em produção.
 */
@Configuration
@Profile("test")
public class TestDataConfiguration {

    // Injeção dos repositórios necessários para manipular os dados
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final RestauranteRepository restauranteRepository;

    /**
     * Injeção de dependência via construtor. O Spring fornecerá as
     * implementações reais dos repositórios quando o perfil "test" estiver ativo.
     */
    public TestDataConfiguration(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            RestauranteRepository restauranteRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.restauranteRepository = restauranteRepository;
    }

    /**
     * 💡 Método de setup inicial.
     * * @PostConstruct: Esta anotação garante que o Spring executará este método
     * uma única vez, logo após a inicialização desta classe e a injeção de
     * todas as dependências (repositórios).
     * * @Transactional: Garante que todo o método execute dentro de uma única
     * transação de banco de dados. Se algo falhar (ex: salvar o produto),
     * todas as operações (incluindo o cliente e restaurante) serão
     * revertidas (rollback), mantendo o banco consistente.
     */
    @PostConstruct
    @Transactional
    public void setupTestData() {
        // 1. Limpa os dados...
        produtoRepository.deleteAll();
        restauranteRepository.deleteAll();
        clienteRepository.deleteAll();

        // 2. Cria e salva um Cliente...
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setEmail("joao.teste@email.com");
        cliente.setCpf("12345678901");
        cliente.setTelefone("11999999999");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        // 3. Cria e salva um Restaurante de teste padrão
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Teste");
        restaurante.setTaxaEntrega(BigDecimal.valueOf(10.00));
        restaurante.setAtivo(true);
        
        // ----- PREENCHA OS CAMPOS AQUI -----
        restaurante.setEndereco("Rua Fictícia de Teste, 123");
        restaurante.setTelefone("999999999");
        restaurante.setCategoria("Pizzaria");
        
        // ----- SALVE O OBJETO COMPLETO APENAS UMA VEZ -----
        restauranteRepository.save(restaurante); // <--- SÓ UMA CHAMADA AO SAVE

        // 4. Cria e salva um Produto de teste
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