package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc

class RestauranteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestauranteRepository restauranteRepository; // <-- adicionado

    // Gera nomes únicos para evitar conflito
    private String gerarNomeUnico(String base) {
        return base + " " + System.currentTimeMillis();
    }

    private void imprimirRestaurantesNoTerminal() {
        System.out.println("===== Restaurantes cadastrados =====");
        restauranteRepository.findAll().forEach(System.out::println);
        System.out.println("===================================");
    }

    @Test
    void deveCadastrarRestauranteComSucesso() throws Exception {
        RestauranteDTO dto = new RestauranteDTO();
        dto.setNome(gerarNomeUnico("Pizza Express"));
        dto.setCategoria("Italiana");
        dto.setEndereco("Rua das Flores, 123");
        dto.setTelefone("11999999999");
        dto.setTaxaEntrega(BigDecimal.valueOf(5.5));
        dto.setAvaliacao(BigDecimal.valueOf(4.5));
        dto.setAtivo(true);
        dto.setTempoEntrega(50);
        dto.setHorarioFuncionamento("08:00-22:00");

        String response = mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.nome").value(dto.getNome()))
                .andExpect(jsonPath("$.data.categoria").value("Italiana"))
                .andExpect(jsonPath("$.data.ativo").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Converte Integer para Long
        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        mockMvc.perform(get("/api/restaurantes/{id}", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value(dto.getNome()));

        // Imprime restaurantes no terminal
        imprimirRestaurantesNoTerminal();
    }

    @Test
    void deveAtualizarRestauranteComSucesso() throws Exception {
        RestauranteDTO dto = new RestauranteDTO();
        dto.setNome(gerarNomeUnico("Pizza Dimas"));
        dto.setCategoria("Italiana");
        dto.setEndereco("Rua das Flores, 123");
        dto.setTelefone("11999999999");
        dto.setTaxaEntrega(BigDecimal.valueOf(5.5));
        dto.setAvaliacao(BigDecimal.valueOf(4.5));
        dto.setAtivo(true);
        dto.setTempoEntrega(50);
        dto.setHorarioFuncionamento("08:00-22:00");

        String response = mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        dto.setNome("Pizza Dimas Atualizado");

        mockMvc.perform(put("/api/restaurantes/{id}", idCriado)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Pizza Dimas Atualizado"));

        imprimirRestaurantesNoTerminal();
    }

    @Test
    void deveAtivarEDesativarRestaurante() throws Exception {
        RestauranteDTO dto = new RestauranteDTO();
        dto.setNome(gerarNomeUnico("Restaurante Status"));
        dto.setCategoria("Italiana");
        dto.setEndereco("Rua Teste, 1");
        dto.setTelefone("11988888888");
        dto.setTaxaEntrega(BigDecimal.valueOf(4.0));
        dto.setAvaliacao(BigDecimal.valueOf(4.0));
        dto.setAtivo(true);
        dto.setTempoEntrega(45);
        dto.setHorarioFuncionamento("08:00-22:00");

        String response = mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        // Desativar
        mockMvc.perform(patch("/api/restaurantes/{id}/status?ativo=false", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ativo").value(false));

        // Ativar
        mockMvc.perform(patch("/api/restaurantes/{id}/status?ativo=true", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ativo").value(true));

        imprimirRestaurantesNoTerminal();
    }

    @Test
    void deveListarRestaurantes() throws Exception {
        mockMvc.perform(get("/api/restaurantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        imprimirRestaurantesNoTerminal();
    }
}
