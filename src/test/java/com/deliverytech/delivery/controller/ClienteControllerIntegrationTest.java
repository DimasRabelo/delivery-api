package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.config.TestDataConfiguration;
import com.deliverytech.delivery.dto.request.ClienteDTO;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; 
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace; 

@WithMockUser(username = "admin", roles = {"ADMIN"})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDataConfiguration.class)
@DisplayName("Testes de Integração do ClienteController (Refatorado V2)")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ClienteControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("[POST /api/clientes] - REMOVIDO (Movido para AuthController)")
    void should_CreateCliente_When_ValidData() {}

    @Test
    @DisplayName("[GET /api/clientes/{id}] - Deve buscar cliente por ID")
    void should_ReturnCliente_When_IdExists() throws Exception {
        Usuario usuarioSalvo = usuarioRepository.findByEmail("joao.teste@email.com")
                .orElseThrow(() -> new IllegalStateException("Usuário de teste não encontrado"));

        mockMvc.perform(get("/api/clientes/{id}", usuarioSalvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(usuarioSalvo.getId().intValue())))
                .andExpect(jsonPath("$.data.nome", is("João Cliente")))
                .andExpect(jsonPath("$.data.email", is("joao.teste@email.com")))
                .andExpect(jsonPath("$.data.ativo", is(true)));
    }

    @Test
    @DisplayName("[GET /api/clientes/{id}] - Deve retornar 404 quando ID não existe")
    void should_ReturnNotFound_When_ClienteNotExists() throws Exception {
        mockMvc.perform(get("/api/clientes/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado")));
    }

    @Test
    @DisplayName("[GET /api/clientes] - Deve listar clientes ativos (Paginado)")
    void should_ReturnListOfClientes_When_ListarClientesAtivos() throws Exception {

        mockMvc.perform(get("/api/clientes")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome", is("João Cliente")))
                .andExpect(jsonPath("$.page.totalElements", is(1)));
    }

    // 🔹 MÉTODO CORRIGIDO — AGORA ENVIANDO EMAIL E SENHA OBRIGATÓRIOS
    @Test
    @WithUserDetails("joao.teste@email.com")
    @DisplayName("[PUT /api/clientes/{id}] - Deve atualizar o próprio perfil (CORRIGIDO)")
    void should_UpdateCliente_When_ClienteExists() throws Exception {

        Usuario usuarioSalvo = usuarioRepository.findByEmail("joao.teste@email.com")
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado"));

        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Cliente Atualizado");
        dto.setCpf(usuarioSalvo.getCliente().getCpf()); 
        dto.setTelefone("11987654321"); 

        // CORREÇÃO ESSENCIAL: Adicionar Email e Senha para satisfazer a validação do Usuario
        dto.setEmail(usuarioSalvo.getEmail()); 
        dto.setSenha("123456"); 
        // -----------------------------------------------------------------------------------

        mockMvc.perform(put("/api/clientes/{id}", usuarioSalvo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andDo(print()) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.nome", is("Cliente Atualizado")))
                .andExpect(jsonPath("$.data.telefone", is("11987654321"))); 
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("[DELETE /api/clientes/{id}] - Deve desativar o usuário (exclusão lógica)")
    void should_DeactivateCliente_When_ClienteExists() throws Exception {
        Usuario usuarioSalvo = usuarioRepository.findByEmail("joao.teste@email.com").orElseThrow();

        mockMvc.perform(delete("/api/clientes/{id}", usuarioSalvo.getId()))
                .andExpect(status().isNoContent());

        Usuario usuarioDesativado = usuarioRepository.findById(usuarioSalvo.getId()).orElseThrow();
        assertFalse(usuarioDesativado.getAtivo());
    }
}