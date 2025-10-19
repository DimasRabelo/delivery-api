package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void testarCadastroEImprimirCliente() throws Exception {
        // Criar o cliente
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setNome("Carlos Silva");
        clienteDTO.setEmail("carlos.silva@example.com");
        clienteDTO.setTelefone("11999999999");
        clienteDTO.setEndereco("Rua da Alegria, 45");

        // Enviar requisição POST para cadastrar
        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteDTO)))
                .andExpect(status().isCreated());

        // Buscar cliente no banco
        Cliente cliente = clienteRepository.findByEmail("carlos.silva@example.com")
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Imprimir os dados no terminal
        System.out.println("➡️ Cliente cadastrado com sucesso:");
        System.out.println("ID: " + cliente.getId());
        System.out.println("Nome: " + cliente.getNome());
        System.out.println("Email: " + cliente.getEmail());
        System.out.println("Telefone: " + cliente.getTelefone());
        System.out.println("Endereço: " + cliente.getEndereco());
        System.out.println("Ativo: " + cliente.isAtivo());
    }
}
