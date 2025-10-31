package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.config.TestDataConfiguration;
import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "admin", roles = {"ADMIN"})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Testes de Integração do ClienteController")
class ClienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository; 

    // O @DirtiesContext já limpa o banco, mas @BeforeEach é uma boa prática
    // para garantir que o repositório esteja vazio.
    @BeforeEach
    void setup() {
        clienteRepository.deleteAll(); 
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve criar cliente com dados válidos")
    void should_CreateCliente_When_ValidData() throws Exception {
        // Given
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Maria Silva");
        dto.setEmail("maria@email.com");
        dto.setCpf("98765432100");
        dto.setTelefone("11988888888");
        dto.setEndereco("Rua das Flores, 123");

        // When & Then
        mockMvc.perform(post("/api/clientes") 
                .contentType(MediaType.APPLICATION_JSON) 
                .content(objectMapper.writeValueAsString(dto))) 
                
                .andExpect(status().isCreated()) 
                // --- CORREÇÃO ---
                // A resposta agora está dentro do wrapper "data"
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.data.email", is("maria@email.com")))
                .andExpect(jsonPath("$.data.id", notNullValue())); 
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void should_ReturnBadRequest_When_InvalidData() throws Exception {
        // Given
        ClienteDTO dto = new ClienteDTO();
        dto.setNome(""); // inválido
        dto.setEmail("email-invalido"); // inválido

        // When & Then
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                // Este teste não mudou, pois valida o status code antes do wrapper
                .andExpect(status().isBadRequest()); 
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve buscar cliente por ID existente")
    void should_ReturnCliente_When_IdExists() throws Exception {
        // Given
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("12345678900");
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua A, 123");
        cliente.setAtivo(true);
        Cliente clienteSalvo = clienteRepository.save(cliente); // Salva e obtém o ID

        // When & Then
        mockMvc.perform(get("/api/clientes/{id}", clienteSalvo.getId())) 
                
                .andExpect(status().isOk()) 
                // --- CORREÇÃO ---
                // A resposta agora está dentro do wrapper "data"
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(clienteSalvo.getId().intValue())))
                .andExpect(jsonPath("$.data.nome", is("João Teste")))
                .andExpect(jsonPath("$.data.email", is("joao@email.com")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve retornar 404 quando cliente não existe")
    void should_ReturnNotFound_When_ClienteNotExists() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/clientes/{id}", 999L)) 
                .andExpect(status().isNotFound())
                // A sua resposta de erro (do ControllerAdvice) provavelmente não usa o wrapper
                // Se usar, este jsonPath também precisará de "$.message"
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve listar clientes (Paginado)")
    void should_ReturnListOfClientes_When_ListarClientesAtivos() throws Exception {
        // Given
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Ativo");
        cliente.setEmail("ativo@email.com");
        cliente.setCpf("55544433322");
        cliente.setTelefone("11988887777");
        cliente.setEndereco("Rua B, 321");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        // When & Then
        mockMvc.perform(get("/api/clientes") // O endpoint agora é a raiz
                .param("page", "0") // Adiciona parâmetros de paginação
                .param("size", "5"))
                
                .andExpect(status().isOk())
                // --- CORREÇÃO ---
                // A resposta agora é um objeto Page, não uma lista.
                // Verificamos o array 'content' dentro do objeto.
                .andExpect(jsonPath("$.content", hasSize(1))) 
                .andExpect(jsonPath("$.content[0].nome", is("Cliente Ativo")))
                // Verifica os metadados da paginação
                .andExpect(jsonPath("$.page.number", is(0)))
                .andExpect(jsonPath("$.page.size", is(5)))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    // ------------------------------------------------------------------------
    // O TESTE "should_ReturnPagedClientes_When_RequestedWithPagination"
    // FOI REMOVIDO POIS ESTAVA TESTANDO UM ENDPOINT (/api/clientes/page)
    // QUE NÃO EXISTE MAIS. O TESTE ACIMA JÁ COBRE A PAGINAÇÃO.
    // ------------------------------------------------------------------------

   @Test
    @DisplayName("Deve atualizar cliente existente")
    void should_UpdateCliente_When_ClienteExists() throws Exception {
        // Given
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Original");
        cliente.setEmail("original@email.com");
        cliente.setCpf("39053344705");
        cliente.setTelefone("11111111111");
        cliente.setEndereco("Rua D, 123");
        cliente.setAtivo(true);
        Cliente clienteSalvo = clienteRepository.save(cliente);

        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Cliente Atualizado");
        dto.setEmail("original@email.com");
        dto.setCpf("39053344705"); 
        dto.setTelefone("22222222222"); // Telefone mudou
        dto.setEndereco("Rua D, 123");

        // When & Then
        mockMvc.perform(put("/api/clientes/{id}", clienteSalvo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            
            .andExpect(status().isOk())
            // --- CORREÇÃO ---
            // A resposta agora está dentro do wrapper "data"
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.nome", is("Cliente Atualizado")))
            .andExpect(jsonPath("$.data.telefone", is("22222222222"))); // ATENÇÃO: Corrija se o telefone for só numérico
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve desativar cliente existente (não excluir)")
    void should_DeactivateCliente_When_ClienteExists() throws Exception {
        // Given
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Desativado");
        cliente.setEmail("delete@email.com");
        cliente.setCpf("20202020200");
        cliente.setTelefone("33333333333");
        cliente.setEndereco("Rua E, 500");
        cliente.setAtivo(true); // Começa ATIVO
        Cliente clienteSalvo = clienteRepository.save(cliente);

        // When
        mockMvc.perform(delete("/api/clientes/{id}", clienteSalvo.getId()))
                // Este teste não muda, pois espera um 204 (sem corpo)
                .andExpect(status().isNoContent()); 

        // Then
        Cliente clienteDesativado = clienteRepository.findById(clienteSalvo.getId()).orElseThrow();
        assertFalse(clienteDesativado.isAtivo());
    }
}