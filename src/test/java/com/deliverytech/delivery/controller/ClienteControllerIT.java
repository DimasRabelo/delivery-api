package com.deliverytech.delivery.controller;

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

//import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ClienteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setup() {
        clienteRepository.deleteAll(); // Limpar dados antes de cada teste
    }

    // Novo método: imprime apenas um cliente
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Carlos Silva")))
                .andExpect(jsonPath("$.email", is("carlos.silva@example.com")));

        Cliente cadastrado = clienteRepository.findAll().stream().findFirst().orElse(null);
        imprimirCliente("DEPOIS de cadastrar", cadastrado);
    }

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Ana Souza")))
                .andExpect(jsonPath("$.email", is("ana.souza@example.com")));

        Cliente clienteBuscado = clienteRepository.findById(cliente.getId()).orElse(null);
        imprimirCliente("DEPOIS de buscar por ID", clienteBuscado);
    }

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
                .andExpect(status().isNoContent());

        Cliente deletado = clienteRepository.findById(cliente.getId()).orElse(null);
        assert deletado != null;
        assert !deletado.isAtivo(); // ativo deve ser false

        imprimirCliente("DEPOIS de deletar", deletado);
    }

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
        novoCliente.setEmail("joao.paulo@example.com"); // mesmo email
        novoCliente.setTelefone("11955555555");
        novoCliente.setEndereco("Rua Nova, 20");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoCliente)))
                .andExpect(status().isBadRequest());

        imprimirCliente("DEPOIS de tentar cadastrar email duplicado", cliente);
        System.out.println("Tentativa de cadastrar email duplicado resultou em BAD_REQUEST");
    }
}
