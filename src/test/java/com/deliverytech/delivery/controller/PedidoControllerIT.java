package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Transactional
    private void imprimirPedidosNoTerminal() {
        System.out.println("===== Pedidos cadastrados =====");
        pedidoRepository.findAll().forEach(pedido -> {
            System.out.println("Pedido ID: " + pedido.getId()
                    + ", Cliente ID: " + (pedido.getCliente() != null ? pedido.getCliente().getId() : null)
                    + ", Restaurante ID: " + (pedido.getRestaurante() != null ? pedido.getRestaurante().getId() : null)
                    + ", Endereço: " + pedido.getEnderecoEntrega());
            if (pedido.getItens() != null) {
                pedido.getItens().forEach(item -> {
                    System.out.println("   Item: Produto ID=" + item.getProduto().getId()
                            + ", Quantidade=" + item.getQuantidade());
                });
            }
        });
        System.out.println("===================================");
    }

    @Test
    void deveCadastrarPedidoComSucesso() throws Exception {
        ItemPedidoDTO item1 = new ItemPedidoDTO();
        item1.setProdutoId(1L);
        item1.setQuantidade(1);

        ItemPedidoDTO item2 = new ItemPedidoDTO();
        item2.setProdutoId(2L);
        item2.setQuantidade(2);

        PedidoDTO dto = new PedidoDTO();
        dto.setClienteId(1L);
        dto.setRestauranteId(1L);
        dto.setEnderecoEntrega("Rua A, 123 - São Paulo/SP");
        dto.setItens(Arrays.asList(item1, item2));

        String response = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Pega o id do pedido criado
        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        // Consulta o pedido criado
        mockMvc.perform(get("/api/pedidos/{id}", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clienteId").value(1))
                .andExpect(jsonPath("$.data.restauranteId").value(1));

        imprimirPedidosNoTerminal();
    }

    @Test
    void deveListarPedidos() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        imprimirPedidosNoTerminal();
    }
}
