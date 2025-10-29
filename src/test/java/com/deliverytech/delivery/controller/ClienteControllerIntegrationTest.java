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

    @BeforeEach
    void setup() {
        clienteRepository.deleteAll(); // garante base limpa antes de cada teste
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve criar cliente com dados válidos")
    void should_CreateCliente_When_ValidData() throws Exception {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Maria Silva");
        dto.setEmail("maria@email.com");
        dto.setCpf("98765432100");
        dto.setTelefone("11988888888");
        dto.setEndereco("Rua das Flores, 123");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.email", is("maria@email.com")))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void should_ReturnBadRequest_When_InvalidData() throws Exception {
        ClienteDTO dto = new ClienteDTO();
        dto.setNome(""); // inválido
        dto.setEmail("email-invalido");

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve buscar cliente por ID existente")
    void should_ReturnCliente_When_IdExists() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("12345678900");
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua A, 123");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        mockMvc.perform(get("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cliente.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("João Teste")))
                .andExpect(jsonPath("$.email", is("joao@email.com")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve retornar 404 quando cliente não existe")
    void should_ReturnNotFound_When_ClienteNotExists() throws Exception {
        mockMvc.perform(get("/api/clientes/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve listar clientes ativos (lista simples)")
    void should_ReturnListOfClientes_When_ListarClientesAtivos() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Ativo");
        cliente.setEmail("ativo@email.com");
        cliente.setCpf("55544433322");
        cliente.setTelefone("11988887777");
        cliente.setEndereco("Rua B, 321");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].nome", is("Cliente Ativo")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve listar clientes com paginação")
    void should_ReturnPagedClientes_When_RequestedWithPagination() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Paginado");
        cliente.setEmail("page@email.com");
        cliente.setCpf("66655544433");
        cliente.setTelefone("11977776666");
        cliente.setEndereco("Rua C, 999");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        mockMvc.perform(get("/api/clientes/page")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    // ------------------------------------------------------------------------
   @Test
@DisplayName("Deve atualizar cliente existente")
void should_UpdateCliente_When_ClienteExists() throws Exception {
    Cliente cliente = new Cliente();
    cliente.setNome("Cliente Original");
    cliente.setEmail("original@email.com");
    cliente.setCpf("39053344705"); // ✅ CPF válido
    cliente.setTelefone("11111111111");
    cliente.setEndereco("Rua D, 123");
    cliente.setAtivo(true);
    clienteRepository.save(cliente);

    ClienteDTO dto = new ClienteDTO();
    dto.setNome("Cliente Atualizado");
    dto.setEmail("original@email.com");
    dto.setCpf("39053344705"); // ✅ CPF válido
    dto.setTelefone("22222222222");
    dto.setEndereco("Rua D, 123");

    mockMvc.perform(put("/api/clientes/{id}", cliente.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome", is("Cliente Atualizado")))
            .andExpect(jsonPath("$.telefone", is("22222222222")));
}

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve desativar cliente existente (não excluir)")
    void should_DeactivateCliente_When_ClienteExists() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Desativado");
        cliente.setEmail("delete@email.com");
        cliente.setCpf("20202020200");
        cliente.setTelefone("33333333333");
        cliente.setEndereco("Rua E, 500");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        // Desativa o cliente
        mockMvc.perform(delete("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isNoContent());

        // Verifica que o cliente foi desativado (ainda existe, mas inativo)
        Cliente clienteDesativado = clienteRepository.findById(cliente.getId()).orElseThrow();
        assertFalse(clienteDesativado.isAtivo());
    }
}
