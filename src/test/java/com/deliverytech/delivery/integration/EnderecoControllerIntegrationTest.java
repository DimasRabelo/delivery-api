package com.deliverytech.delivery.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles; // <-- 1. IMPORT NECESSÁRIO
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize; // <-- 2. IMPORT NECESSÁRIO
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de Integração para o EnderecoController.
 * Agora forçando o perfil "test" para carregar TestDataConfiguration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test") // <-- 3. CORREÇÃO: FORÇA O PERFIL DE TESTE
class EnderecoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve Retornar 200 (OK) e lista de endereços ao buscar /meus como CLIENTE")
    // 4. CORREÇÃO: Usando o usuário do TestDataConfiguration
    @WithUserDetails("joao.teste@email.com") 
    void deveRetornarStatus200EEnderecos_QuandoLogadoComoCliente() throws Exception {
        
        mockMvc.perform(get("/api/enderecos/meus")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // 5. CORREÇÃO: Verifica o tamanho da lista e o apelido do TestDataConfiguration
                .andExpect(jsonPath("$", hasSize(1))) 
                .andExpect(jsonPath("$[0].apelido").value("Casa Teste"));
    }

    @Test
    @DisplayName("Deve Retornar 403 (Forbidden) ao buscar /meus como RESTAURANTE")
    // 6. CORREÇÃO: Usando o usuário de restaurante do TestDataConfiguration
    @WithUserDetails("restaurante.dono@email.com") 
    void deveRetornarStatus403_QuandoLogadoComoRestaurante() throws Exception {
        
        mockMvc.perform(get("/api/enderecos/meus")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve Retornar 401 (Unauthorized) ao buscar /meus sem autenticação")
    void deveRetornarStatus401_QuandoNaoAutenticado() throws Exception {
        
        mockMvc.perform(get("/api/enderecos/meus")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Deve Retornar 201 (Created) ao criar novo endereço como CLIENTE")
    // 7. CORREÇÃO: Usando o usuário do TestDataConfiguration
    @WithUserDetails("joao.teste@email.com")
    void deveRetornarStatus201_AoCriarEnderecoComoCliente() throws Exception {
        
        String novoEnderecoJson = """
        {
          "rua": "Avenida Faria Lima",
          "numero": "1000",
          "bairro": "Itaim Bibi",
          "cidade": "São Paulo",
          "estado": "SP",
          "cep": "01452002",
          "apelido": "Trabalho"
        }
        """;
        
        mockMvc.perform(post("/api/enderecos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(novoEnderecoJson))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apelido").value("Trabalho"));
    }
}