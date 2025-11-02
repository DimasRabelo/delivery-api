package com.deliverytech.delivery.controller;

// <<< MUDANÇA: Imports adicionados para o mock estático
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
// ---

import com.deliverytech.delivery.config.TestDataConfiguration;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "joao.teste@email.com", roles = {"CLIENTE"})
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@Transactional
class PedidoControllerIntegrationTest {

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

    private Cliente clienteAtivo;
    private Restaurante restauranteAtivo;
    private Produto produtoDisponivel;

    @BeforeEach
    void setup() {
        // Usar os dados já criados pelo TestDataConfiguration
        clienteAtivo = clienteRepository.findByEmail("joao.teste@email.com")
                .orElseThrow(() -> new RuntimeException("Cliente de teste não encontrado"));
        restauranteAtivo = restauranteRepository.findAll().get(0);
        produtoDisponivel = produtoRepository.findAll().get(0);
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso")
    void deveCriarPedidoComSucesso() throws Exception {
        
        // Bloco try-with-resources para mockar o SecurityUtils
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(clienteAtivo.getId());
            
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
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Pedido criado com sucesso"))
                    .andExpect(jsonPath("$.data.clienteId").value(clienteAtivo.getId()))
                    .andExpect(jsonPath("$.data.restauranteId").value(restauranteAtivo.getId()))
                    .andExpect(jsonPath("$.data.itens[0].produtoId").value(produtoDisponivel.getId()))
                    .andExpect(jsonPath("$.data.total").exists());
        } 
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando cliente está inativo")
    void deveRetornarErro_QuandoClienteInativo() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            Cliente clienteInativo = new Cliente();
            clienteInativo.setNome("Maria Inativa");
            clienteInativo.setEmail("maria@email.com");
            clienteInativo.setCpf("98765432100");
            clienteInativo.setTelefone("11888888888");
            clienteInativo.setAtivo(false);
            clienteRepository.saveAndFlush(clienteInativo);

            // O usuário LOGADO ainda é o clienteAtivo
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(clienteAtivo.getId());
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setClienteId(clienteInativo.getId()); // O DTO aponta para o cliente inativo
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntrega("Rua das Rosas, 456");
            pedidoDTO.setCep("54321-000"); // CEP válido (presumindo)
            pedidoDTO.setFormaPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(1);
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Cliente inativo não pode fazer pedidos"));
        }
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando produto pertence a outro restaurante")
    void deveRetornarErro_QuandoProdutoDeOutroRestaurante() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(clienteAtivo.getId());

            Restaurante outroRestaurante = new Restaurante();
            outroRestaurante.setNome("Outro Restaurante");
            outroRestaurante.setTaxaEntrega(BigDecimal.valueOf(8.0));
            outroRestaurante.setAtivo(true);
            
            // <<< CORREÇÃO 1: Preenchendo os campos obrigatórios
            outroRestaurante.setEndereco("Rua do Outro Restaurante, 456");
            outroRestaurante.setTelefone("1188888888");
            outroRestaurante.setCategoria("Italiana");
            
            restauranteRepository.saveAndFlush(outroRestaurante);
    
            Produto produtoDeOutro = new Produto();
            produtoDeOutro.setNome("Lasanha");
            produtoDeOutro.setPreco(BigDecimal.valueOf(25.00));
            produtoDeOutro.setEstoque(5);
            produtoDeOutro.setDisponivel(true);
            produtoDeOutro.setRestaurante(outroRestaurante);
            produtoRepository.saveAndFlush(produtoDeOutro);
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setClienteId(clienteAtivo.getId());
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntrega("Rua Teste, 321");
            pedidoDTO.setCep("12345-000"); // CEP válido
            pedidoDTO.setFormaPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDeOutro.getId());
            item.setQuantidade(1);
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Produto não pertence ao restaurante selecionado"));
        }
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando estoque for insuficiente")
    void deveRetornarErro_QuandoEstoqueInsuficiente() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(clienteAtivo.getId());

            produtoDisponivel.setEstoque(2);
            produtoRepository.saveAndFlush(produtoDisponivel);
            entityManager.flush();
            entityManager.clear();
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setClienteId(clienteAtivo.getId());
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntrega("Rua Azul, 789");
            
            // <<< CORREÇÃO 2: Usando um CEP válido
            pedidoDTO.setCep("12345-678"); 
            
            pedidoDTO.setFormaPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(5); // Pedindo mais do que o estoque
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto: Pizza Teste"));
        }
    }

    @Test
    @DisplayName("Deve retornar erro de validação quando campos obrigatórios faltarem")
    void deveRetornarErroDeValidacao_QuandoCamposObrigatoriosFaltando() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(clienteAtivo.getId());
            
            PedidoDTO pedidoDTO = new PedidoDTO(); // DTO Vazio
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Erro de validação nos dados enviados"));
        }
    }
}