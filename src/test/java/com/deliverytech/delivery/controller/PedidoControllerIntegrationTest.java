package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * --- ANOTAÇÕES DE TESTE DE INTEGRAÇÃO ---
 *
 * @SpringBootTest: Carrega o contexto *completo* da aplicação Spring,
 * diferente do @WebMvcTest (que só carrega a camada web).
 * @AutoConfigureMockMvc: Configura o MockMvc para simular requisições HTTP.
 * @WithMockUser: Simula um usuário autenticado (role CLIENTE) para
 * contornar o Spring Security.
 * @ActiveProfiles("test"): Usa o perfil 'test' (application-test.properties),
 * que geralmente aponta para um banco em memória (H2).
 * @Transactional: **Importante!** Em testes, esta anotação faz com que cada
 * método de teste rode dentro de uma transação que é *automaticamente revertida* (rollback) ao final.
 * Isso garante que um teste não "suje" o banco para o próximo, sendo uma alternativa
 * mais rápida ao @DirtiesContext.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "cliente.teste@email.com", roles = {"CLIENTE"})
@ActiveProfiles("test")
@Transactional
class PedidoControllerIntegrationTest {

    // --- DEPENDÊNCIAS INJETADAS ---

    @Autowired
    private MockMvc mockMvc; // Ferramenta para simular requisições HTTP (POST, GET, etc.)

    @Autowired
    private ObjectMapper objectMapper; // Converte objetos Java (DTOs) para JSON e vice-versa

    // Repositórios REAIS (não são mocks). Usamos para preparar o banco.
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EntityManager entityManager; // Usado para controle fino do cache de persistência (ver teste de estoque)

    // --- DADOS DE SETUP ---
    private Cliente clienteAtivo;
    private Restaurante restauranteAtivo;
    private Produto produtoDisponivel;

    // ===========================================================
    // SETUP DE DADOS BÁSICOS
    // ===========================================================
    /**
     * O @BeforeEach é executado antes de CADA teste.
     * Graças ao @Transactional, o banco está limpo antes de cada execução deste setup.
     */
    @BeforeEach
    void setup() {
        // 1. Cria um cliente válido
        clienteAtivo = new Cliente();
        clienteAtivo.setNome("João da Silva");
        clienteAtivo.setEmail("joao@email.com");
        clienteAtivo.setCpf("12345678900");
        clienteAtivo.setTelefone("11999999999");
        clienteAtivo.setAtivo(true);
        // saveAndFlush() força o INSERT no banco imediatamente (necessário para obter IDs)
        clienteRepository.saveAndFlush(clienteAtivo);

        // 2. Cria um restaurante válido
        restauranteAtivo = new Restaurante();
        restauranteAtivo.setNome("Pizzaria Boa Massa");
        restauranteAtivo.setTaxaEntrega(BigDecimal.valueOf(5.00));
        restauranteAtivo.setAtivo(true);
        restauranteRepository.saveAndFlush(restauranteAtivo);

        // 3. Cria um produto válido (vinculado ao restaurante)
        produtoDisponivel = new Produto();
        produtoDisponivel.setNome("Pizza Margherita");
        produtoDisponivel.setDescricao("Deliciosa pizza com queijo e manjericão");
        produtoDisponivel.setPreco(BigDecimal.valueOf(40.00));
        produtoDisponivel.setEstoque(10); // Estoque inicial
        produtoDisponivel.setDisponivel(true);
        produtoDisponivel.setRestaurante(restauranteAtivo);
        produtoRepository.saveAndFlush(produtoDisponivel);
    }

    // ===========================================================
    // 1️⃣ CRIAÇÃO DE PEDIDO COM SUCESSO (Caminho Feliz)
    // ===========================================================
    @Test
    @DisplayName("Deve criar pedido com sucesso")
    void deveCriarPedidoComSucesso() throws Exception {
        // -----------------
        // Given (Arrange) - Preparamos o DTO que será enviado no corpo da requisição
        // -----------------
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteAtivo.getId()); // ID do cliente criado no setup
        pedidoDTO.setRestauranteId(restauranteAtivo.getId()); // ID do restaurante criado no setup
        pedidoDTO.setEnderecoEntrega("Rua das Flores, 123 - Centro");
        pedidoDTO.setCep("12345-678");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDisponivel.getId()); // ID do produto criado no setup
        item.setQuantidade(2); // Quantidade válida (estoque é 10)
        pedidoDTO.setItens(List.of(item));

        // -----------------
        // When (Act) - Executamos a chamada HTTP
        // -----------------
        mockMvc.perform(post("/api/pedidos") // Simula um POST
                        .contentType(MediaType.APPLICATION_JSON) // Define o 'Content-Type'
                        .content(objectMapper.writeValueAsString(pedidoDTO))) // Converte o DTO para JSON
                
        // -----------------
        // Then (Assert) - Verificamos a resposta HTTP
        // -----------------
                .andExpect(status().isCreated()) // Esperamos um status 201 (Created)
                // Usamos jsonPath para "ler" o JSON de resposta
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido criado com sucesso"))
                .andExpect(jsonPath("$.data.clienteId").value(clienteAtivo.getId()))
                .andExpect(jsonPath("$.data.restauranteId").value(restauranteAtivo.getId()))
                .andExpect(jsonPath("$.data.itens[0].produtoId").value(produtoDisponivel.getId()))
                .andExpect(jsonPath("$.data.total").exists()); // Verifica se o total foi calculado
    }

    // ===========================================================
    // 2️⃣ ERRO: CLIENTE INATIVO
    // ===========================================================
    @Test
    @DisplayName("Deve retornar erro 400 quando cliente está inativo")
    void deveRetornarErro_QuandoClienteInativo() throws Exception {
        // -----------------
        // Given (Arrange) - Preparamos o cenário de falha
        // -----------------
        // 1. Criamos um cliente INATIVO especificamente para este teste
        Cliente clienteInativo = new Cliente();
        clienteInativo.setNome("Maria Inativa");
        clienteInativo.setEmail("maria@email.com");
        clienteInativo.setCpf("98765432100");
        clienteInativo.setTelefone("11888888888");
        clienteInativo.setAtivo(false); // Inativo
        clienteRepository.saveAndFlush(clienteInativo);

        // 2. Criamos um DTO que tenta usar o cliente inativo
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteInativo.getId()); // ID do cliente inativo
        pedidoDTO.setRestauranteId(restauranteAtivo.getId());
        pedidoDTO.setEnderecoEntrega("Rua das Rosas, 456");
        pedidoDTO.setCep("54321-000");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDisponivel.getId());
        item.setQuantidade(1);
        pedidoDTO.setItens(List.of(item));

        // -----------------
        // When (Act) & Then (Assert)
        // -----------------
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                // O ControllerAdvice deve capturar a BusinessException e retornar 400
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").value("Cliente inativo não pode fazer pedidos"));
    }

    // ===========================================================
    // 3️⃣ ERRO: PRODUTO DE OUTRO RESTAURANTE
    // ===========================================================
    @Test
    @DisplayName("Deve retornar erro 400 quando produto pertence a outro restaurante")
    void deveRetornarErro_QuandoProdutoDeOutroRestaurante() throws Exception {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Criamos um *outro* restaurante
        Restaurante outroRestaurante = new Restaurante();
        outroRestaurante.setNome("Outro Restaurante");
        outroRestaurante.setTaxaEntrega(BigDecimal.valueOf(8.0));
        outroRestaurante.setAtivo(true);
        restauranteRepository.saveAndFlush(outroRestaurante);

        // 2. Criamos um produto que pertence a esse *outro* restaurante
        Produto produtoDeOutro = new Produto();
        produtoDeOutro.setNome("Lasanha");
        produtoDeOutro.setPreco(BigDecimal.valueOf(25.00));
        produtoDeOutro.setEstoque(5);
        produtoDeOutro.setDisponivel(true);
        produtoDeOutro.setRestaurante(outroRestaurante); // Vínculo errado
        produtoRepository.saveAndFlush(produtoDeOutro);

        // 3. Criamos um DTO que tenta pedir no restaurante original (restauranteAtivo),
        //    mas inclui um item do "outroRestaurante" (produtoDeOutro).
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteAtivo.getId());
        pedidoDTO.setRestauranteId(restauranteAtivo.getId()); // Pedido é para a Pizzaria
        pedidoDTO.setEnderecoEntrega("Rua Teste, 321");
        pedidoDTO.setCep("12345-000");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDeOutro.getId()); // Item é a Lasanha (do Outro)
        item.setQuantidade(1);
        pedidoDTO.setItens(List.of(item));

        // -----------------
        // When (Act) & Then (Assert)
        // -----------------
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Produto não pertence ao restaurante selecionado"));
    }

    // ===========================================================
    // 4️⃣ ERRO: ESTOQUE INSUFICIENTE
    // ===========================================================
    @Test
    @DisplayName("Deve retornar erro 400 quando estoque for insuficiente")
    void deveRetornarErro_QuandoEstoqueInsuficiente() throws Exception {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Atualizamos o produto do setup para ter um estoque baixo
        produtoDisponivel.setEstoque(2);
        produtoRepository.saveAndFlush(produtoDisponivel);

        // 2. **Controle de Cache do Hibernate (MUITO IMPORTANTE)**
        // Como estamos no mesmo @Transactional, o Hibernate/JPA pode manter
        // a entidade 'produtoDisponivel' com estoque 10 no cache (Contexto de Persistência).
        // 'flush()' força o UPDATE no banco (stock=2).
        // 'clear()' limpa o cache. Isso força o Service (quando chamado pelo Controller)
        // a buscar o produto *diretamente do banco*, lendo o novo estoque (2).
        entityManager.flush();
        entityManager.clear();

        // 3. Criamos um DTO que pede *mais* do que o estoque (pedindo 5, mas só tem 2)
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteAtivo.getId());
        pedidoDTO.setRestauranteId(restauranteAtivo.getId());
        pedidoDTO.setEnderecoEntrega("Rua Azul, 789");
        pedidoDTO.setCep("11111-111");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDisponivel.getId());
        item.setQuantidade(5); // Pedindo 5
        pedidoDTO.setItens(List.of(item));

        // -----------------
        // When (Act) & Then (Assert)
        // -----------------
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto: Pizza Margherita"));
    }

    // ===========================================================
    // 5️⃣ ERRO DE VALIDAÇÃO (Campos Nulos/Vazios)
    // ===========================================================
    @Test
    @DisplayName("Deve retornar erro de validação quando campos obrigatórios faltarem")
    void deveRetornarErroDeValidacao_QuandoCamposObrigatoriosFaltando() throws Exception {
        // -----------------
        // Given (Arrange) - Criamos um DTO vazio
        // -----------------
        PedidoDTO pedidoDTO = new PedidoDTO(); // Viola @NotNull, @NotEmpty, etc.

        // -----------------
        // When (Act) & Then (Assert)
        // -----------------
        // Este teste valida as anotações (@Valid) no Controller
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação nos dados enviados"));
    }
}