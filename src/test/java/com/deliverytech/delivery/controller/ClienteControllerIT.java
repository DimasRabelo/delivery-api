package com.deliverytech.delivery.controller;

// Importa as classes necessárias
import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Classe de testes de integração para o ClienteController.
 * Usa o MockMvc para simular requisições HTTP reais.
 */
@SpringBootTest
@ActiveProfiles("test") // Usa o perfil de testes (application-test.properties)
@AutoConfigureMockMvc   // Configura o MockMvc automaticamente
class ClienteControllerIT {

    @Autowired
    private MockMvc mockMvc; // Simula as requisições HTTP para a API

    @Autowired
    private ObjectMapper objectMapper; // Converte objetos Java em JSON e vice-versa

    @Autowired
    private ClienteRepository clienteRepository; // Permite manipular os dados diretamente no banco de teste

    /**
     * Executado antes de cada teste.
     * Limpa o repositório para garantir que cada teste comece com o banco limpo.
     */
    @BeforeEach
    void setup() {
        clienteRepository.deleteAll();
    }

    /**
     * Método auxiliar para imprimir dados de um cliente no console durante os testes.
     * Facilita a depuração dos resultados.
     */
    private void imprimirCliente(String mensagem, Cliente cliente) {
        System.out.println("===== " + mensagem + " =====");
        if (cliente == null) {
            System.out.println("Nenhum cliente encontrado.");
        } else {
            System.out.println("ID: " + cliente.getId());
            System.out.println("Nome: " + cliente.getNome());
            System.out.println("Email: " + cliente.getEmail());
            System.out.println("Telefone: " + cliente.getTelefone());
            System.out.println("Endereço: " + cliente.getEndereco());
            System.out.println("Ativo: " + cliente.isAtivo());
        }
        System.out.println("-----------------------------");
    }

    /**
     * Teste: deve cadastrar um novo cliente com sucesso.
     * Verifica se o retorno contém os dados esperados e se o cliente foi salvo no banco.
     */
    @Test
    void deveCadastrarCliente() throws Exception {
        imprimirCliente("ANTES de cadastrar", null);

        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Carlos Silva");
        clienteDTO.setEmail("carlos.silva@example.com");
        clienteDTO.setTelefone("11999999999");
        clienteDTO.setEndereco("Rua da Alegria, 45");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isCreated()) // Espera retorno HTTP 201
                .andExpect(jsonPath("$.nome", is("Carlos Silva")))
                .andExpect(jsonPath("$.email", is("carlos.silva@example.com")));

        Cliente cadastrado = clienteRepository.findAll().stream().findFirst().orElse(null);
        imprimirCliente("DEPOIS de cadastrar", cadastrado);
    }

    /**
     * Teste: deve buscar um cliente existente pelo ID.
     * Verifica se os dados retornados correspondem ao cliente salvo.
     */
    @Test
    void deveRetornarClientePorId() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Ana Souza");
        cliente.setEmail("ana.souza@example.com");
        cliente.setTelefone("11988888888");
        cliente.setEndereco("Rua do Sol, 100");
        cliente.setAtivo(true);
        cliente = clienteRepository.save(cliente);

        imprimirCliente("ANTES de buscar por ID", cliente);

        mockMvc.perform(get("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isOk()) // Espera retorno HTTP 200
                .andExpect(jsonPath("$.nome", is("Ana Souza")))
                .andExpect(jsonPath("$.email", is("ana.souza@example.com")));

        Cliente clienteBuscado = clienteRepository.findById(cliente.getId()).orElse(null);
        imprimirCliente("DEPOIS de buscar por ID", clienteBuscado);
    }

    /**
     * Teste: deve atualizar os dados de um cliente existente.
     * Verifica se o nome foi alterado corretamente.
     */
    @Test
    void deveAtualizarCliente() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Pedro Lima");
        cliente.setEmail("pedro.lima@example.com");
        cliente.setTelefone("11977777777");
        cliente.setEndereco("Av. Central, 200");
        cliente.setAtivo(true);
        cliente = clienteRepository.save(cliente);

        imprimirCliente("ANTES de atualizar", cliente);

        ClienteDTO updateDTO = new ClienteDTO();
        updateDTO.setNome("Pedro Lima Atualizado");
        updateDTO.setEmail("pedro.lima@example.com");
        updateDTO.setTelefone("11977777777");
        updateDTO.setEndereco("Av. Central, 200");

        mockMvc.perform(put("/api/clientes/{id}", cliente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Pedro Lima Atualizado")));

        Cliente atualizado = clienteRepository.findById(cliente.getId()).orElse(null);
        imprimirCliente("DEPOIS de atualizar", atualizado);
    }

    /**
     * Teste: deve desativar (deletar logicamente) um cliente.
     * O campo "ativo" deve passar de true para false.
     */
    @Test
    void deveDeletarCliente() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("Lucas Mendes");
        cliente.setEmail("lucas.mendes@example.com");
        cliente.setTelefone("11966666666");
        cliente.setEndereco("Rua das Flores, 50");
        cliente.setAtivo(true);
        cliente = clienteRepository.save(cliente);

        imprimirCliente("ANTES de deletar", cliente);

        mockMvc.perform(delete("/api/clientes/{id}", cliente.getId()))
                .andExpect(status().isNoContent()); // Espera retorno HTTP 204

        Cliente deletado = clienteRepository.findById(cliente.getId()).orElse(null);
        assert deletado != null;
        assert !deletado.isAtivo(); // Deve estar desativado

        imprimirCliente("DEPOIS de deletar", deletado);
    }

    /**
     * Teste: deve retornar erro ao tentar cadastrar um cliente com email duplicado.
     * Verifica se o sistema retorna um status 400 (Bad Request).
     */
    @Test
    void deveRetornarErroAoCadastrarClienteComEmailExistente() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome("João Paulo");
        cliente.setEmail("joao.paulo@example.com");
        cliente.setTelefone("11955555555");
        cliente.setEndereco("Rua do Limoeiro, 10");
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        imprimirCliente("ANTES de tentar cadastrar email duplicado", cliente);

        ClienteDTO novoCliente = new ClienteDTO();
        novoCliente.setNome("Outro João");
        novoCliente.setEmail("joao.paulo@example.com"); // Email repetido
        novoCliente.setTelefone("11955555555");
        novoCliente.setEndereco("Rua Nova, 20");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isBadRequest()); // Espera erro 400

        imprimirCliente("DEPOIS de tentar cadastrar email duplicado", cliente);
        System.out.println("Tentativa de cadastrar email duplicado resultou em BAD_REQUEST");
    }
}
