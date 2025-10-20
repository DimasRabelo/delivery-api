package com.deliverytech.delivery.controller;

// Importa as classes necessárias para os testes
import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Indica que será usado o contexto completo do Spring Boot nos testes
@SpringBootTest
// Define o perfil "test", ou seja, vai usar configurações do application-test.yml
@ActiveProfiles("test")
// Ativa o uso do MockMvc para simular requisições HTTP
@AutoConfigureMockMvc
class RestauranteControllerIT {

    // Injeta o MockMvc (permite simular requisições HTTP sem subir servidor)
    @Autowired
    private MockMvc mockMvc;

    // Converte objetos Java ↔ JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Acesso direto ao banco de dados (para fins de debug ou validação)
    @Autowired
    private RestauranteRepository restauranteRepository;

    // Gera nomes únicos (evita conflito em testes que criam restaurantes com mesmo nome)
    private String gerarNomeUnico(String base) {
        return base + " " + System.currentTimeMillis();
    }

    // Exibe no terminal todos os restaurantes cadastrados — útil para debug
    private void imprimirRestaurantesNoTerminal() {
        System.out.println("===== Restaurantes cadastrados =====");
        restauranteRepository.findAll().forEach(System.out::println);
        System.out.println("===================================");
    }

    // TESTE 1 — Cadastrar restaurante com sucesso
    @Test
    void deveCadastrarRestauranteComSucesso() throws Exception {
        // Cria o DTO com os dados do restaurante
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

        // Envia uma requisição POST para /api/restaurantes com o JSON do DTO
        String response = mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                // Espera o status HTTP 201 (Created)
                .andExpect(status().isCreated())
                // Valida se o retorno contém os mesmos dados enviados
                .andExpect(jsonPath("$.data.nome").value(dto.getNome()))
                .andExpect(jsonPath("$.data.categoria").value("Italiana"))
                .andExpect(jsonPath("$.data.ativo").value(true))
                // Captura a resposta JSON como String
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extrai o ID criado da resposta JSON
        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        // Faz uma requisição GET para confirmar que o restaurante foi salvo
        mockMvc.perform(get("/api/restaurantes/{id}", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value(dto.getNome()));

        // Mostra no terminal os restaurantes cadastrados
        imprimirRestaurantesNoTerminal();
    }

    // TESTE 2 — Atualizar restaurante com sucesso
    @Test
    void deveAtualizarRestauranteComSucesso() throws Exception {
        // Cria um restaurante inicial
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

        // Faz o cadastro inicial via POST
        String response = mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Lê o ID criado
        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        // Atualiza o nome
        dto.setNome("Pizza Dimas Atualizado");

        // Faz o PUT (atualização)
        mockMvc.perform(put("/api/restaurantes/{id}", idCriado)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Pizza Dimas Atualizado"));

        imprimirRestaurantesNoTerminal();
    }

    // TESTE 3 — Ativar e desativar restaurante
    @Test
    void deveAtivarEDesativarRestaurante() throws Exception {
        // Cria um restaurante ativo
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

        // Cadastra via POST
        String response = mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extrai o ID criado
        Integer idCriadoInt = JsonPath.read(response, "$.data.id");
        Long idCriado = idCriadoInt.longValue();

        // Faz PATCH para desativar o restaurante
        mockMvc.perform(patch("/api/restaurantes/{id}/status?ativo=false", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ativo").value(false));

        // Faz PATCH para reativar o restaurante
        mockMvc.perform(patch("/api/restaurantes/{id}/status?ativo=true", idCriado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ativo").value(true));

        imprimirRestaurantesNoTerminal();
    }

    // TESTE 4 — Listar restaurantes
    @Test
    void deveListarRestaurantes() throws Exception {
        // Faz um GET geral e verifica se a resposta contém um array
        mockMvc.perform(get("/api/restaurantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        imprimirRestaurantesNoTerminal();
    }
}
