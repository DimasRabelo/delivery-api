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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; // <-- IMPORT NECESSÁRIO
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "cliente.teste@email.com", roles = {"CLIENTE"})
@ActiveProfiles("test")
@Transactional
class PedidoControllerIntegrationTest {

    // --- DEPENDÊNCIAS INJETADAS ---

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EntityManager entityManager;

    // --- DADOS DE SETUP ---
    private Cliente clienteAtivo;
    private Restaurante restauranteAtivo;
    private Produto produtoDisponivel;

    // ===========================================================
    // SETUP DE DADOS BÁSICOS
    // ===========================================================
    @BeforeEach
    void setup() {
        // 1. Cria um cliente válido
        clienteAtivo = new Cliente();
        clienteAtivo.setNome("João da Silva");
        clienteAtivo.setEmail("joao@email.com");
        clienteAtivo.setCpf("12345678900");
        clienteAtivo.setTelefone("11999999999");
        clienteAtivo.setAtivo(true);
        clienteRepository.saveAndFlush(clienteAtivo);

        // 2. Cria um restaurante válido
        restauranteAtivo = new Restaurante();
        restauranteAtivo.setNome("Pizzaria Boa Massa");
        restauranteAtivo.setTaxaEntrega(BigDecimal.valueOf(5.00));
        restauranteAtivo.setAtivo(true);
        restauranteAtivo.setEndereco("Rua da Pizzaria (Setup), 123");
        restauranteAtivo.setTelefone("999999999");
        restauranteAtivo.setCategoria("Pizzaria");
        restauranteRepository.saveAndFlush(restauranteAtivo);

        // 3. Cria um produto válido (vinculado ao restaurante)
        produtoDisponivel = new Produto();
        produtoDisponivel.setNome("Pizza Margherita");
        produtoDisponivel.setDescricao("Deliciosa pizza com queijo e manjericão");
        produtoDisponivel.setPreco(BigDecimal.valueOf(40.00));
        produtoDisponivel.setEstoque(10);
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
        // Given
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteAtivo.getId());
        pedidoDTO.setRestauranteId(restauranteAtivo.getId());
        pedidoDTO.setEnderecoEntrega("Rua das Flores, 123 - Centro");
        pedidoDTO.setCep("12345-678");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDisponivel.getId());
        item.setQuantidade(2);
        pedidoDTO.setItens(List.of(item));

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                
                // --- CORREÇÃO 2: ADICIONADO PARA DEPURAR O ERRO 500 ---
                .andDo(print()) 
                
                .andExpect(status().isCreated()) // O log anterior mostrou falha (500) aqui
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pedido criado com sucesso"))
                .andExpect(jsonPath("$.data.clienteId").value(clienteAtivo.getId()))
                .andExpect(jsonPath("$.data.restauranteId").value(restauranteAtivo.getId()))
                .andExpect(jsonPath("$.data.itens[0].produtoId").value(produtoDisponivel.getId()))
                .andExpect(jsonPath("$.data.total").exists()); 
    }

    // ===========================================================
    // 2️⃣ ERRO: CLIENTE INATIVO
    // ===========================================================
    @Test
    @DisplayName("Deve retornar erro 400 quando cliente está inativo")
    void deveRetornarErro_QuandoClienteInativo() throws Exception {
        // Given
        Cliente clienteInativo = new Cliente();
        clienteInativo.setNome("Maria Inativa");
        clienteInativo.setEmail("maria@email.com");
        clienteInativo.setCpf("98765432100");
        clienteInativo.setTelefone("11888888888");
        clienteInativo.setAtivo(false);
        clienteRepository.saveAndFlush(clienteInativo);

        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteInativo.getId());
        pedidoDTO.setRestauranteId(restauranteAtivo.getId());
        pedidoDTO.setEnderecoEntrega("Rua das Rosas, 456");
        pedidoDTO.setCep("54321-000");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDisponivel.getId());
        item.setQuantidade(1);
        pedidoDTO.setItens(List.of(item));

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").value("Cliente inativo não pode fazer pedidos"));
    }

    // ===========================================================
    // 3️⃣ ERRO: PRODUTO DE OUTRO RESTAURANTE
    // ===========================================================
    @Test
    @DisplayName("Deve retornar erro 400 quando produto pertence a outro restaurante")
    void deveRetornarErro_QuandoProdutoDeOutroRestaurante() throws Exception {
        // Given
        // 1. Criamos um *outro* restaurante
        Restaurante outroRestaurante = new Restaurante();
        outroRestaurante.setNome("Outro Restaurante");
        outroRestaurante.setTaxaEntrega(BigDecimal.valueOf(8.0));
        outroRestaurante.setAtivo(true);
        
        // --- CORREÇÃO 1: ADICIONANDO CAMPOS OBRIGATÓRIOS ---
        outroRestaurante.setEndereco("Rua do Outro Restaurante, 456");
        outroRestaurante.setTelefone("1188888888");
        outroRestaurante.setCategoria("Italiana");
        // --------------------------------------------------
        
        restauranteRepository.saveAndFlush(outroRestaurante); // Esta linha não vai mais falhar

        // 2. Criamos um produto que pertence a esse *outro* restaurante
        Produto produtoDeOutro = new Produto();
        produtoDeOutro.setNome("Lasanha");
        produtoDeOutro.setPreco(BigDecimal.valueOf(25.00));
        produtoDeOutro.setEstoque(5);
        produtoDeOutro.setDisponivel(true);
        produtoDeOutro.setRestaurante(outroRestaurante);
        produtoRepository.saveAndFlush(produtoDeOutro);

        // 3. Criamos um DTO que tenta pedir no restaurante original
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteAtivo.getId());
        pedidoDTO.setRestauranteId(restauranteAtivo.getId());
        pedidoDTO.setEnderecoEntrega("Rua Teste, 321");
        pedidoDTO.setCep("12345-000");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDeOutro.getId());
        item.setQuantidade(1);
        pedidoDTO.setItens(List.of(item));

        // When & Then
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
        // Given
        // 1. Atualizamos o produto do setup para ter um estoque baixo
        produtoDisponivel.setEstoque(2);
        produtoRepository.saveAndFlush(produtoDisponivel);
        
        // 2. Limpa o cache do Hibernate
        entityManager.flush();
        entityManager.clear();

        // 3. Criamos um DTO que pede *mais* do que o estoque
        PedidoDTO pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(clienteAtivo.getId());
        pedidoDTO.setRestauranteId(restauranteAtivo.getId());
        pedidoDTO.setEnderecoEntrega("Rua Azul, 789");
        pedidoDTO.setCep("11111-111");
        pedidoDTO.setFormaPagamento("PIX");

        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoDisponivel.getId());
        item.setQuantidade(5); // Pedindo 5 (só tem 2)
        pedidoDTO.setItens(List.of(item));

        // When & Then
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
        // Given
        PedidoDTO pedidoDTO = new PedidoDTO(); 

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação nos dados enviados"));
    }
}