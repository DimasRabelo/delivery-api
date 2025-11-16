package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.config.TestDataConfiguration;
import com.deliverytech.delivery.dto.request.ItemPedidoDTO;
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.PaymentService; 
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean; 
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("all") 
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataConfiguration.class) 
@Transactional
@DisplayName("Testes de Integração do PedidoController (Refatorado)")
class PedidoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    @MockBean 
    private PaymentService paymentService; 
    
    @Autowired private RestauranteRepository restauranteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private UsuarioRepository usuarioRepository; 
    @Autowired private PasswordEncoder passwordEncoder; 
    @Autowired private PedidoRepository pedidoRepository; 
    
    @Autowired private EntityManager entityManager;

    private Usuario usuarioAtivo;
    private Endereco enderecoAtivo;
    private Restaurante restauranteAtivo;
    private Produto produtoDisponivel;

    // MÉTODO AUXILIAR PARA OBTENÇÃO DE TOKEN (Corrigido)
    private String obtainValidJwtToken(String email, String password) throws Exception {
        String loginJson = String.format("{\"email\": \"%s\", \"senha\": \"%s\"}", email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk()) 
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        
        var tokenNode = objectMapper.readTree(responseBody).get("token");
        
        if (tokenNode == null || tokenNode.asText().isEmpty()) {
            throw new IllegalStateException("Falha na extração do token. Resposta: " + responseBody);
        }
        return tokenNode.asText();
    }


    @BeforeEach
    void setup() {
        // Usar os dados já criados pelo TestDataConfiguration
        usuarioAtivo = usuarioRepository.findByEmail("joao.teste@email.com")
                .orElseThrow(() -> new RuntimeException("Usuário de teste 'joao.teste@email.com' não encontrado."));
        
        enderecoAtivo = usuarioAtivo.getEnderecos().get(0); 
        
        restauranteAtivo = restauranteRepository.findAll().get(0);
        produtoDisponivel = produtoRepository.findAll().get(0);
        
        // MOCK PADRÃO para SUCESSO
        Mockito.when(paymentService.processPayment(anyString(), anyDouble())).thenReturn(true); 
    }
    
    // =====================================================================
    // TESTE DE INTEGRAÇÃO: SUCESSO
    // =====================================================================

    @Test
    @DisplayName("Deve criar pedido com sucesso (Refatorado)")
    void deveCriarPedidoComSucesso() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());
            
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId()); 
            pedidoDTO.setMetodoPagamento("PIX"); 
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(2);
            
            item.setOpcionaisIds(List.of()); 
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .header("Authorization", "Bearer " + obtainValidJwtToken("joao.teste@email.com", "123456")) 
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        } 
    }

    // =====================================================================
    // NOVO TESTE CRÍTICO: FALHA NO PAGAMENTO
    // =====================================================================
    @Test
    @DisplayName("POST /pedidos: Deve retornar 400 Bad Request se o PaymentService simular FALHA")
    void deveRetornar400_QuandoPagamentoSimularFalha() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());

            // 2. FORÇA A FALHA DO MOCK SERVICE
            when(paymentService.processPayment(anyString(), anyDouble())).thenReturn(false); 
            
            // 3. Obtém o Token 
            String token = obtainValidJwtToken("joao.teste@email.com", "123456"); 

            // 4. Montar o DTO de requisição válido
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId());
            pedidoDTO.setMetodoPagamento("PIX");
            
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(1);
           
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
            
            // ACT & ASSERT
            
            mockMvc.perform(post("/api/pedidos")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    
                    .andDo(print())
                    // Espera a resposta HTTP 400 Bad Request
                    .andExpect(status().isBadRequest()) 
                    
                    // Verifica a mensagem da BusinessException
                    .andExpect(jsonPath("$.message").value("Transação de pagamento não autorizada. Status: Simulação de Falha."))
                    
                    // Prova de que o Mock Service foi chamado
                    .andDo(result -> verify(paymentService, times(1)).processPayment(anyString(), anyDouble()));
                    
            // A verificação do PedidoRepository foi removida para evitar o NotAMockException
        }
    }


    // =====================================================================
    // TESTES DE FALHA (EXISTENTES) - Falhas de Validação e Segurança
    // =====================================================================

   @Test
    @DisplayName("Deve retornar erro 400 quando cliente está inativo (Refatorado)")
    void deveRetornarErro_QuandoClienteInativo() throws Exception {
        
        // 1. Cria um usuário INATIVO no banco
        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setEmail("maria@email.com");
        usuarioInativo.setSenha(passwordEncoder.encode("123456")); 
        usuarioInativo.setRole(Role.CLIENTE);
        usuarioInativo.setAtivo(false); 
        
        Cliente clienteInativo = new Cliente();
        clienteInativo.setNome("Maria Inativa");
        clienteInativo.setCpf("98765432100");
        clienteInativo.setUsuario(usuarioInativo);
        usuarioInativo.setCliente(clienteInativo);
        usuarioRepository.saveAndFlush(usuarioInativo); 
        
        // 2. Obtém o token do usuário ATIVO (joao.teste@email.com)
        // Isso garante que a autenticação no filtro JWT seja bem-sucedida (Status 200)
        String tokenAtivo = obtainValidJwtToken("joao.teste@email.com", "123456"); 
        
        // 3. Mocka o SecurityUtils para usar o ID do cliente INATIVO (Maria)
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            // O ID da Maria Inativa é usado para simular que o usuário logado é ela.
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioInativo.getId()); 
    
            // 4. Prepara o DTO
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId()); 
            pedidoDTO.setMetodoPagamento("PIX");
            
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(1);
           
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
    
            // 5. Executa (Autenticação OK, mas a lógica de negócio falha)
            mockMvc.perform(post("/api/pedidos")
                            .header("Authorization", "Bearer " + tokenAtivo) // TOKEN DE JOAO ATIVO
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest()) // ESPERA O 400 DA BUSINESS EXCEPTION
                    .andExpect(jsonPath("$.message").value("Cliente inativo não pode fazer pedidos"));
        }
    }
    @Test
    @DisplayName("Deve retornar erro 400 quando produto pertence a outro restaurante (Refatorado)")
    void deveRetornarErro_QuandoProdutoDeOutroRestaurante() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());
            String token = obtainValidJwtToken("joao.teste@email.com", "123456");
            
            // --- SETUP CORRIGIDO ---
            Usuario donoRestaurante2 = usuarioRepository.findByEmail("restaurante.dono@email.com")
                    .orElseThrow();


            Endereco endOutro = new Endereco();
            endOutro.setApelido("Restaurante 2"); 
            endOutro.setCep("11111111");
            endOutro.setRua("Rua do Outro");
            endOutro.setNumero("456");
            endOutro.setBairro("Bairro");
            endOutro.setCidade("Cidade");
            endOutro.setEstado("SP");
            endOutro.setUsuario(donoRestaurante2); 
            
            Restaurante outroRestaurante = new Restaurante();
            outroRestaurante.setNome("Outro Restaurante");
            outroRestaurante.setTaxaEntrega(BigDecimal.valueOf(8.0));
            outroRestaurante.setAtivo(true);
            outroRestaurante.setTelefone("1188888888");
            outroRestaurante.setCategoria("Italiana");
            outroRestaurante.setEndereco(endOutro); 
            restauranteRepository.saveAndFlush(outroRestaurante);
    
            Produto produtoDeOutro = new Produto();
            produtoDeOutro.setNome("Lasanha");
            produtoDeOutro.setPrecoBase(BigDecimal.valueOf(25.00)); 
            produtoDeOutro.setEstoque(5);
            produtoDeOutro.setDisponivel(true);
            produtoDeOutro.setRestaurante(outroRestaurante);
            produtoRepository.saveAndFlush(produtoDeOutro);
            // --- FIM DO SETUP ---
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId()); 
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId());
            pedidoDTO.setMetodoPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDeOutro.getId()); 
            item.setQuantidade(1);
            
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
    
           mockMvc.perform(post("/api/pedidos")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Produto Lasanha não pertence ao restaurante selecionado"));
        }
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando estoque for insuficiente (Refatorado)")
    void deveRetornarErro_QuandoEstoqueInsuficiente() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());

            produtoDisponivel.setEstoque(2); 
            produtoRepository.saveAndFlush(produtoDisponivel);
            entityManager.flush();
            entityManager.clear();
            
            String token = obtainValidJwtToken("joao.teste@email.com", "123456");
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId()); 
            pedidoDTO.setMetodoPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(5); 
           
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto: Pizza Teste"));
        }
    }
}