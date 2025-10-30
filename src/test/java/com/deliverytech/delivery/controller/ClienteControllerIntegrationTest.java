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

/**
 * --- ANOTAÇÕES DE TESTE DE INTEGRAÇÃO ---
 *
 * @WithMockUser: Simula um usuário autenticado (com role ADMIN) para contornar o Spring Security.
 * @SpringBootTest: Esta é a anotação principal. Ela carrega o *contexto completo* da aplicação Spring,
 * diferente do @WebMvcTest (que só carrega a camada web) ou @DataJpaTest (só a camada de dados).
 * @AutoConfigureMockMvc: Configura automaticamente o MockMvc, que é nossa ferramenta para "chamar" os endpoints HTTP.
 * @ActiveProfiles("test"): Informa ao Spring para usar o perfil 'test', carregando o 'application-test.properties'
 * (geralmente configurado para um banco em memória H2).
 * @Import(TestDataConfiguration.class): Importa configurações de teste adicionais, se houver.
 * @DirtiesContext: Garante que o contexto do Spring (e o banco de dados) seja *resetado* após cada método de teste.
 * Isso é crucial para que um teste não interfira no próximo.
 */
@WithMockUser(username = "admin", roles = {"ADMIN"})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestDataConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Testes de Integração do ClienteController")
class ClienteControllerIntegrationTest {

    // --- DEPENDÊNCIAS INJETADAS ---

    @Autowired
    private MockMvc mockMvc; // O "Postman" do teste. Usado para fazer as requisições HTTP.

    @Autowired
    private ObjectMapper objectMapper; // Utilitário para converter objetos Java (DTOs) em JSON e vice-versa.

    @Autowired
    private ClienteRepository clienteRepository; // O repositório *real*. Usamos ele para preparar o banco (Arrange)
                                                 // e para verificar o resultado (Assert).

    @BeforeEach
    void setup() {
        // Graças ao @DirtiesContext e ao @ActiveProfiles("test"),
        // temos um banco de dados limpo (geralmente H2) para cada teste.
        // Este deleteAll() é uma garantia extra.
        clienteRepository.deleteAll(); 
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve criar cliente com dados válidos")
    void should_CreateCliente_When_ValidData() throws Exception {
        // -----------------
        // Given (Arrange) - Preparamos o DTO que será enviado no corpo da requisição
        // -----------------
        ClienteDTO dto = new ClienteDTO();
        dto.setNome("Maria Silva");
        dto.setEmail("maria@email.com");
        dto.setCpf("98765432100");
        dto.setTelefone("11988888888");
        dto.setEndereco("Rua das Flores, 123");

        // -----------------
        // When (Act) - Executamos a chamada HTTP
        // -----------------
        mockMvc.perform(post("/api/clientes") // Faz um POST para a URL
                .contentType(MediaType.APPLICATION_JSON) // Define o Header 'Content-Type'
                .content(objectMapper.writeValueAsString(dto))) // Serializa o DTO para JSON e envia no corpo
                
        // -----------------
        // Then (Assert) - Verificamos a resposta HTTP
        // -----------------
                .andExpect(status().isCreated()) // Espera um status HTTP 201 (Created)
                // Usamos 'jsonPath' para verificar o corpo (body) da resposta JSON
                .andExpect(jsonPath("$.nome", is("Maria Silva")))
                .andExpect(jsonPath("$.email", is("maria@email.com")))
                .andExpect(jsonPath("$.id", notNullValue())); // Verifica se o ID foi gerado
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve retornar erro 400 quando dados inválidos")
    void should_ReturnBadRequest_When_InvalidData() throws Exception {
        // -----------------
        // Given (Arrange) - DTO com dados inválidos (provavelmente viola @NotBlank, @Email)
        // -----------------
        ClienteDTO dto = new ClienteDTO();
        dto.setNome(""); // inválido
        dto.setEmail("email-invalido"); // inválido

        // -----------------
        // When (Act) & Then (Assert)
        // -----------------
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                // Este teste valida as anotações de @Valid no Controller/DTO
                .andExpect(status().isBadRequest()); // Espera um status HTTP 400 (Bad Request)
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve buscar cliente por ID existente")
    void should_ReturnCliente_When_IdExists() throws Exception {
        // -----------------
        // Given (Arrange) - Salvamos um cliente DIRETAMENTE no banco de dados.
        // -----------------
        Cliente cliente = new Cliente();
        cliente.setNome("João Teste");
        cliente.setEmail("joao@email.com");
        cliente.setCpf("12345678900");
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua A, 123");
        cliente.setAtivo(true);
        clienteRepository.save(cliente); // Salva e obtém o ID

        // -----------------
        // When (Act)
        // -----------------
        mockMvc.perform(get("/api/clientes/{id}", cliente.getId())) // Faz um GET usando o ID
                
        // -----------------
        // Then (Assert)
        // -----------------
                .andExpect(status().isOk()) // Espera um status HTTP 200 (OK)
                .andExpect(jsonPath("$.id", is(cliente.getId().intValue())))
                .andExpect(jsonPath("$.nome", is("João Teste")))
                .andExpect(jsonPath("$.email", is("joao@email.com")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve retornar 404 quando cliente não existe")
    void should_ReturnNotFound_When_ClienteNotExists() throws Exception {
        // -----------------
        // Given (Arrange) - Nenhum cliente existe (banco limpo)
        // -----------------

        // -----------------
        // When (Act) & Then (Assert)
        // -----------------
        mockMvc.perform(get("/api/clientes/{id}", 999L)) // Tenta buscar um ID que não existe
                .andExpect(status().isNotFound()) // Espera um status HTTP 404 (Not Found)
                // Verifica a mensagem de erro (do @ControllerAdvice / ExceptionHandler)
                .andExpect(jsonPath("$.message", containsString("Cliente não encontrado")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve listar clientes ativos (lista simples)")
    void should_ReturnListOfClientes_When_ListarClientesAtivos() throws Exception {
        // -----------------
        // Given (Arrange) - Salva um cliente no banco
        // -----------------
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Ativo");
        cliente.setEmail("ativo@email.com");
        cliente.setCpf("55544433322");
        // ... (outros dados)
        cliente.setTelefone("11988887777");
        cliente.setEndereco("Rua B, 321");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        // -----------------
        // When (Act)
        // -----------------
        mockMvc.perform(get("/api/clientes"))
                
        // -----------------
        // Then (Assert)
        // -----------------
                .andExpect(status().isOk())
                // '$' é a raiz do JSON. Esperamos que seja uma lista (array)
                .andExpect(jsonPath("$", hasSize(greaterThan(0)))) // A lista tem 1 ou mais itens
                // '$[0]' é o primeiro item da lista. Verificamos seu nome.
                .andExpect(jsonPath("$[0].nome", is("Cliente Ativo")));
    }

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve listar clientes com paginação")
    void should_ReturnPagedClientes_When_RequestedWithPagination() throws Exception {
        // -----------------
        // Given (Arrange)
        // -----------------
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Paginado");
        cliente.setEmail("page@email.com");
        cliente.setCpf("66655544433");
        // ... (outros dados)
        cliente.setTelefone("11977776666");
        cliente.setEndereco("Rua C, 999");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        // -----------------
        // When (Act)
        // -----------------
        mockMvc.perform(get("/api/clientes/page")
                .param("page", "0") // Adiciona parâmetros de query (?page=0&size=10)
                .param("size", "10"))
                
        // -----------------
        // Then (Assert) - Verificamos a estrutura da Página (Page)
        // -----------------
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0)))) // 'content' é a lista de itens
                .andExpect(jsonPath("$.number", is(0))) // 'number' é a página atual
                .andExpect(jsonPath("$.size", is(10))); // 'size' é o tamanho da página
    }

    // ------------------------------------------------------------------------
   @Test
@DisplayName("Deve atualizar cliente existente")
void should_UpdateCliente_When_ClienteExists() throws Exception {
    // -----------------
    // Given (Arrange)
    // -----------------
    // 1. Cliente original salvo no banco
    Cliente cliente = new Cliente();
    cliente.setNome("Cliente Original");
    cliente.setEmail("original@email.com");
    cliente.setCpf("39053344705");
    cliente.setTelefone("11111111111");
    cliente.setEndereco("Rua D, 123");
    cliente.setAtivo(true);
    clienteRepository.save(cliente);

    // 2. DTO com os dados atualizados
    ClienteDTO dto = new ClienteDTO();
    dto.setNome("Cliente Atualizado");
    dto.setEmail("original@email.com");
    dto.setCpf("39053344705"); 
    dto.setTelefone("22222222222"); // Telefone mudou
    dto.setEndereco("Rua D, 123");

    // -----------------
    // When (Act)
    // -----------------
    mockMvc.perform(put("/api/clientes/{id}", cliente.getId()) // Faz um PUT
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            
    // -----------------
    // Then (Assert) - Verifica se a resposta contém os dados atualizados
    // -----------------
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome", is("Cliente Atualizado")))
            .andExpect(jsonPath("$.telefone", is("22222222222")));
}

    // ------------------------------------------------------------------------
    @Test
    @DisplayName("Deve desativar cliente existente (não excluir)")
    void should_DeactivateCliente_When_ClienteExists() throws Exception {
        // -----------------
        // Given (Arrange)
        // -----------------
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Desativado");
        cliente.setEmail("delete@email.com");
        cliente.setCpf("20202020200");
        cliente.setTelefone("33333333333");
        cliente.setEndereco("Rua E, 500");
        cliente.setAtivo(true); // Começa ATIVO
        clienteRepository.save(cliente);

        // -----------------
        // When (Act)
        // -----------------
        // Executa a chamada DELETE
        mockMvc.perform(delete("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isNoContent()); // Espera um status HTTP 204 (No Content)

        // -----------------
        // Then (Assert) - Verificação de estado no BANCO DE DADOS
        // -----------------
        // Como o endpoint retorna 204 (sem corpo), precisamos verificar
        // o estado diretamente no banco para confirmar a desativação (soft delete).
        Cliente clienteDesativado = clienteRepository.findById(cliente.getId()).orElseThrow();
        
        // Verificamos se o status 'ativo' foi alterado para 'false'
        assertFalse(clienteDesativado.isAtivo());
    }
}