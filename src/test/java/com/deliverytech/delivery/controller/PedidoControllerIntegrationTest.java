package com.deliverytech.delivery.controller;

// --- Imports de Entidades, DTOs e Repos Novos ---
import com.deliverytech.delivery.config.TestDataConfiguration;
import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
// --- Fim dos Imports Novos ---

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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // (Necessário para criar usuário)
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Adicione estes imports estáticos

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
// (O @WithMockUser aqui é um 'admin' genérico para os testes de 'listar' e '404')
@WithMockUser(username = "joao.teste@email.com", roles = {"CLIENTE"})
@ActiveProfiles("test")
@Import(TestDataConfiguration.class) // (Importa o setup de dados que corrigimos)
@Transactional
@DisplayName("Testes de Integração do PedidoController (Refatorado)")
class PedidoControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    
    // --- Repositórios ---
    
    @Autowired private RestauranteRepository restauranteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private UsuarioRepository usuarioRepository; // <-- NOVO
    
    @Autowired private PasswordEncoder passwordEncoder; // <-- NOVO

    @Autowired
    private EntityManager entityManager;

    // --- Entidades de Teste ---
    private Usuario usuarioAtivo;
    private Cliente clienteAtivo;
    private Endereco enderecoAtivo;
    private Restaurante restauranteAtivo;
    private Produto produtoDisponivel;

    @BeforeEach
    void setup() {
        // Usar os dados já criados pelo TestDataConfiguration (que refatoramos)
        // (Nota: TestDataConfiguration precisa estar 100% corrigido)
        usuarioAtivo = usuarioRepository.findByEmail("joao.teste@email.com")
                .orElseThrow(() -> new RuntimeException("Usuário de teste 'joao.teste@email.com' não encontrado. Verifique TestDataConfiguration."));
        
        clienteAtivo = usuarioAtivo.getCliente();
        enderecoAtivo = usuarioAtivo.getEnderecos().get(0); // Pega o primeiro endereço cadastrado
        
        restauranteAtivo = restauranteRepository.findAll().get(0);
        produtoDisponivel = produtoRepository.findAll().get(0);
    }

    @Test
    @DisplayName("Deve criar pedido com sucesso (Refatorado)")
    void deveCriarPedidoComSucesso() throws Exception {
        
        // Mocka o SecurityUtils para retornar o ID do nosso usuário de teste
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());
            
            // --- DTO REFATORADO (Gargalos 1, 2 e 3) ---
            PedidoDTO pedidoDTO = new PedidoDTO();
            // (clienteId foi REMOVIDO)
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId()); // <-- MUDOU (String -> ID)
            pedidoDTO.setMetodoPagamento("PIX"); // <-- MUDOU (era 'formaPagamento')
            // (trocoPara é opcional)
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(2);
            item.setOpcionaisIds(List.of()); // <-- MUDOU (Gargalo 2) - envia lista vazia
            pedidoDTO.setItens(List.of(item));
            // --- FIM DO DTO ---
    
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
    @DisplayName("Deve retornar erro 400 quando cliente está inativo (Refatorado)")
    void deveRetornarErro_QuandoClienteInativo() throws Exception {
        
        // 1. Cria um usuário INATIVO no banco
        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setEmail("maria@email.com");
        usuarioInativo.setSenha(passwordEncoder.encode("123"));
        usuarioInativo.setRole(Role.CLIENTE);
        usuarioInativo.setAtivo(false); // <-- CORRIGIDO: 'ativo' é no Usuario
        
        Cliente clienteInativo = new Cliente();
        clienteInativo.setNome("Maria Inativa");
        clienteInativo.setCpf("98765432100");
        clienteInativo.setUsuario(usuarioInativo);
        usuarioInativo.setCliente(clienteInativo);
        usuarioRepository.saveAndFlush(usuarioInativo); // Salva o usuário (cascade salva o cliente)

        // 2. Mocka o login como o usuário INATIVO
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioInativo.getId());
    
            // 3. Prepara o DTO (os dados do DTO estão corretos)
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId()); // (Usa o endereço do usuário logado)
            pedidoDTO.setMetodoPagamento("PIX");
            
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(1);
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
    
            // 4. Executa
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest()) // (Ou 409 Conflict, dependendo da sua exceção)
                    .andExpect(jsonPath("$.message").value("Cliente inativo não pode fazer pedidos"));
        }
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando produto pertence a outro restaurante (Refatorado)")
    void deveRetornarErro_QuandoProdutoDeOutroRestaurante() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());

            // --- SETUP CORRIGIDO (Gargalos 1 e 2) ---
            Endereco endOutro = new Endereco();
            endOutro.setCep("11111111");
            endOutro.setRua("Rua do Outro");
            endOutro.setNumero("456");
            endOutro.setBairro("Bairro");
            endOutro.setCidade("Cidade");
            endOutro.setEstado("SP");
            
            Restaurante outroRestaurante = new Restaurante();
            outroRestaurante.setNome("Outro Restaurante");
            outroRestaurante.setTaxaEntrega(BigDecimal.valueOf(8.0));
            outroRestaurante.setAtivo(true);
            outroRestaurante.setTelefone("1188888888");
            outroRestaurante.setCategoria("Italiana");
            outroRestaurante.setEndereco(endOutro); // <-- CORRIGIDO (Objeto Endereco)
            restauranteRepository.saveAndFlush(outroRestaurante);
    
            Produto produtoDeOutro = new Produto();
            produtoDeOutro.setNome("Lasanha");
            produtoDeOutro.setPrecoBase(BigDecimal.valueOf(25.00)); // <-- CORRIGIDO (precoBase)
            produtoDeOutro.setEstoque(5);
            produtoDeOutro.setDisponivel(true);
            produtoDeOutro.setRestaurante(outroRestaurante);
            produtoRepository.saveAndFlush(produtoDeOutro);
            // --- FIM DO SETUP ---
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId()); // Tenta pedir no restaurante ATIVO
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId());
            pedidoDTO.setMetodoPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDeOutro.getId()); // Mas pede um produto do OUTRO restaurante
            item.setQuantidade(1);
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Produto não pertence ao restaurante selecionado"));
        }
    }

    @Test
    @DisplayName("Deve retornar erro 400 quando estoque for insuficiente (Refatorado)")
    void deveRetornarErro_QuandoEstoqueInsuficiente() throws Exception {
        
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioAtivo.getId());

            produtoDisponivel.setEstoque(2); // Estoque = 2
            produtoRepository.saveAndFlush(produtoDisponivel);
            entityManager.flush();
            entityManager.clear();
    
            PedidoDTO pedidoDTO = new PedidoDTO();
            pedidoDTO.setRestauranteId(restauranteAtivo.getId());
            pedidoDTO.setEnderecoEntregaId(enderecoAtivo.getId()); // <-- CORRIGIDO
            pedidoDTO.setMetodoPagamento("PIX");
    
            ItemPedidoDTO item = new ItemPedidoDTO();
            item.setProdutoId(produtoDisponivel.getId());
            item.setQuantidade(5); // Pedindo 5
            item.setOpcionaisIds(List.of());
            pedidoDTO.setItens(List.of(item));
    
            mockMvc.perform(post("/api/pedidos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pedidoDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto: Pizza Teste"));
        }
    }
}