package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProdutoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Test
    void deveCadastrarEListarProdutosPorCategoria() throws Exception {

        // 1️⃣ Criar um restaurante válido (necessário para vincular o produto)
        Restaurante restaurante = new Restaurante();
        restaurante.setNome("Restaurante Bom Sabor");
        restaurante.setAtivo(true);
        restaurante.setEndereco("Rua das Flores, 123");
        restaurante.setTelefone("11988887777");
        restaurante.setCategoria("Lanches");
        restaurante.setTempoEntrega(25);
        restaurante.setTaxaEntrega(BigDecimal.valueOf(5.0));
        restaurante.setAvaliacao(BigDecimal.valueOf(4.8));
        restaurante.setHorarioFuncionamento("09:00 - 22:00");
        restaurante = restauranteRepository.save(restaurante);

        Long restauranteId = restaurante.getId();

        // 2️⃣ Criar primeiro produto
        ProdutoDTO produto1 = new ProdutoDTO();
        produto1.setNome("X-Bacon");
        produto1.setDescricao("Hamburguer artesanal com bacon crocante");
        produto1.setPreco(BigDecimal.valueOf(29.90));
        produto1.setCategoria("Lanche");
        produto1.setDisponivel(true);
        produto1.setRestauranteId(restauranteId);

        // 3️⃣ Criar segundo produto
        ProdutoDTO produto2 = new ProdutoDTO();
        produto2.setNome("X-Salada");
        produto2.setDescricao("Hamburguer com queijo, alface e tomate");
        produto2.setPreco(BigDecimal.valueOf(24.50));
        produto2.setCategoria("Lanche");
        produto2.setDisponivel(true);
        produto2.setRestauranteId(restauranteId);

        // 4️⃣ Enviar produtos via POST
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto1)))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produto2)))
                .andDo(print())
                .andExpect(status().isCreated());

        // 5️⃣ Buscar produtos pela categoria (endpoint real do seu controller)
        mockMvc.perform(get("/api/produtos/categoria/Lanche")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // vai mostrar os produtos no terminal
                .andExpect(status().isOk());
    }
}
