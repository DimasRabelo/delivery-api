package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.*;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // Sobe o contexto completo do Spring Boot para testes de integração
@AutoConfigureMockMvc // Configura o MockMvc automaticamente para simular requisições HTTP
public class PedidoControllerIT {

    @Autowired
    private MockMvc mockMvc; // Objeto usado para simular requisições REST (GET, POST, PUT, DELETE, PATCH)

    @Autowired
    private ObjectMapper objectMapper; // Converte objetos Java para JSON e vice-versa

    @Autowired
    private PedidoRepository pedidoRepository; // Permite limpar ou consultar o banco durante os testes

    @BeforeEach
    void setup() {
        pedidoRepository.deleteAll(); //  Limpa os pedidos antes de cada teste (garante ambiente limpo)
    }

    // ------------------------------
    // MÉTODOS AUXILIARES
    // ------------------------------

    // Cria um modelo de pedido com cliente, restaurante, endereço e itens
    private PedidoDTO criarPedidoModelo(Long clienteId, Long restauranteId, String endereco, ItemPedidoDTO... itens) {
        PedidoDTO pedido = new PedidoDTO();
        pedido.setClienteId(clienteId);
        pedido.setRestauranteId(restauranteId);
        pedido.setEnderecoEntrega(endereco);
        pedido.setItens(List.of(itens));
        return pedido;
    }

    // Cria um item de pedido (produto + quantidade)
    private ItemPedidoDTO criarItem(Long produtoId, int quantidade) {
        ItemPedidoDTO item = new ItemPedidoDTO();
        item.setProdutoId(produtoId);
        item.setQuantidade(quantidade);
        return item;
    }

    // Cria um pedido real via API e retorna o ID gerado na resposta JSON
    private Long criarPedidoERetornarId(PedidoDTO pedido) throws Exception {
        String pedidoJson = objectMapper.writeValueAsString(pedido);
        String response = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pedidoJson))
                .andExpect(status().isCreated()) // Espera retorno HTTP 201
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asLong(); // Extrai o ID do JSON
    }

    // ------------------------------
    // Criar pedido válido com 1 item
    // ------------------------------
    @Test
    void criarPedidoValido() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 1L, "Rua A, 123", criarItem(1L, 2));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isCreated()) // Espera sucesso (201)
                .andExpect(jsonPath("$.data.clienteId").value(1))
                .andExpect(jsonPath("$.data.itens[0].produtoId").value(1));
    }

    // ------------------------------
    // Pedido com múltiplos itens
    // ------------------------------
    @Test
    void criarPedidoMultiplosItens() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(2L, 1L, "Rua B, 456",
                criarItem(1L, 2), criarItem(2L, 3));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.itens.length()").value(2)); // Espera 2 itens no pedido
    }

    // ------------------------------
    //  Pedido sem itens → deve retornar 400
    // ------------------------------
    @Test
    void criarPedidoSemItens() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 1L, "Rua C, 789");

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isBadRequest()); // Falha esperada (pedido inválido)
    }

    // ------------------------------
    //  Pedido com endereço nulo → deve retornar 400
    // ------------------------------
    @Test
    void criarPedidoEnderecoNulo() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 1L, null, criarItem(1L, 1));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isBadRequest()); // Endereço obrigatório
    }

    // ------------------------------
    //  Cliente inexistente → deve retornar 404
    // ------------------------------
    @Test
    void criarPedidoClienteInexistente() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(999L, 1L, "Rua D, 101", criarItem(1L, 1));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isNotFound()); // Cliente não encontrado
    }

    // ------------------------------
    //  Restaurante inexistente → deve retornar 404
    // ------------------------------
    @Test
    void criarPedidoRestauranteInexistente() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 999L, "Rua E, 202", criarItem(1L, 1));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isNotFound()); // Restaurante não encontrado
    }

    // ------------------------------
    // Quantidade inválida → deve retornar 400
    // ------------------------------
    @Test
    void criarPedidoQuantidadeInvalida() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 1L, "Rua F, 303", criarItem(1L, 0));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isBadRequest()); //  Quantidade mínima é 1
    }

    // ------------------------------
    //  Atualizar status do pedido (válido e inválido)
    // ------------------------------
    @Test
    void atualizarStatusPedido() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 1L, "Rua G, 404", criarItem(1L, 1));
        Long pedidoId = criarPedidoERetornarId(pedido); // Cria um pedido real

        StatusPedidoDTO statusDTO = new StatusPedidoDTO();
        statusDTO.setStatus("CONFIRMADO");

        // Atualiza status para CONFIRMADO
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMADO"));

        //  Tenta atualizar com status inválido
        statusDTO.setStatus("INVALIDO");
        mockMvc.perform(patch("/api/pedidos/" + pedidoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------
    // Cancelar pedido → deve retornar 204
    // ------------------------------
    @Test
    void cancelarPedido() throws Exception {
        PedidoDTO pedido = criarPedidoModelo(1L, 1L, "Rua H, 505", criarItem(1L, 1));
        Long pedidoId = criarPedidoERetornarId(pedido);

        mockMvc.perform(delete("/api/pedidos/" + pedidoId))
                .andExpect(status().isNoContent()); // Pedido cancelado com sucesso
    }

    // ------------------------------
    // Histórico de pedidos de um cliente
    // ------------------------------
    @Test
    void historicoCliente() throws Exception {
        mockMvc.perform(get("/api/pedidos/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray()); // Retorna lista de pedidos do cliente
    }

    // ------------------------------
    //  Histórico de pedidos de um restaurante
    // ------------------------------
    @Test
    void historicoRestaurante() throws Exception {
        mockMvc.perform(get("/api/pedidos/restaurante/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray()); // Retorna lista de pedidos do restaurante
    }
}
