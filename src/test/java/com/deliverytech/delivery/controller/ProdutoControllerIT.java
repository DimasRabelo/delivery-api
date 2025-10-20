package com.deliverytech.delivery.controller;

// Importações necessárias
import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.repository.ProdutoRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

// Executa o contexto completo da aplicação Spring Boot nos testes
@SpringBootTest
// Configura o MockMvc para simular requisições HTTP
@AutoConfigureMockMvc
// Define o uso do perfil "test" (usa application-test.yml)
@ActiveProfiles("test")
// Garante que o banco de dados será revertido após cada teste
@Transactional
public class ProdutoControllerIT {

    // MockMvc simula requisições HTTP (GET, POST, PUT, PATCH, DELETE)
    @Autowired
    private MockMvc mockMvc;

    // Converte objetos Java ↔ JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Acesso ao repositório de restaurantes (para criar dados antes dos testes)
    @Autowired
    private RestauranteRepository restauranteRepository;

    // Acesso ao repositório de produtos
    @Autowired
    private ProdutoRepository produtoRepository;

    // TESTE 1 — Criar produto com sucesso
    @Test
    void deveCadastrarProduto() throws Exception {
        // Cria um restaurante válido (produto precisa estar vinculado a um)
        Restaurante restaurante = criarRestaurante("Burger Test", "Lanches");
        // Cria o DTO do produto para enviar na requisição
        ProdutoDTO produtoDTO = criarProdutoDTO("X-Test", "Delicioso hambúrguer teste", 19.90, "Lanche", restaurante.getId());

        // Envia requisição POST para criar produto
        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoDTO)))
                .andDo(print()) // Mostra a resposta no terminal
                .andExpect(status().isCreated()) // Espera retorno 201 (Created)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nome").value("X-Test"));
    }

    // TESTE 2 — Buscar produto por ID
    @Test
    void deveBuscarProdutoPorId() throws Exception {
        Restaurante restaurante = criarRestaurante("Pizza Test", "Pizza");
        Produto produto = salvarProduto("Pizza Teste", "Mussarela com tomate", 30.0, "Pizza", true, restaurante.getId());

        // Faz GET /api/produtos/{id}
        mockMvc.perform(get("/api/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nome").value("Pizza Teste"));
    }

    // TESTE 3 — Atualizar produto existente
    @Test
    void deveAtualizarProduto() throws Exception {
        Restaurante restaurante = criarRestaurante("Atualiza Burger", "Lanches");
        Produto produto = salvarProduto("X-Burger", "Original", 22.0, "Lanche", true, restaurante.getId());

        // Cria DTO com os novos dados
        ProdutoDTO dtoAtualizado = criarProdutoDTO("X-Burger Duplo", "Agora com dois hambúrgueres!", 29.90, "Lanche", restaurante.getId());

        // Faz PUT /api/produtos/{id}
        mockMvc.perform(put("/api/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoAtualizado)))
                .andDo(print())
                .andExpect(status().isOk()) // Espera 200 (OK)
                .andExpect(jsonPath("$.data.nome").value("X-Burger Duplo"))
                .andExpect(jsonPath("$.data.descricao").value("Agora com dois hambúrgueres!"))
                .andExpect(jsonPath("$.data.preco").value(29.90));
    }

    // TESTE 4 — Remover produto
    @Test
    void deveRemoverProduto() throws Exception {
        Restaurante restaurante = criarRestaurante("Delete Test", "Massas");
        Produto produto = salvarProduto("Lasanha Teste", "Bolonhesa", 25.0, "Massa", true, restaurante.getId());

        // DELETE /api/produtos/{id}
        mockMvc.perform(delete("/api/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNoContent()); // Espera 204 (sem conteúdo)

        // Verifica no repositório se foi realmente removido
        assertTrue(produtoRepository.findById(produto.getId()).isEmpty());
    }

    // TESTE 5 — Alterar disponibilidade do produto (PATCH)
    @Test
    void deveAlterarDisponibilidadeProduto() throws Exception {
        Restaurante restaurante = criarRestaurante("Disponibilidade Test", "Bebidas");
        Produto produto = salvarProduto("Suco Teste", "Natural", 10.0, "Bebida", true, restaurante.getId());

        // PATCH /api/produtos/{id}/disponibilidade
        mockMvc.perform(patch("/api/produtos/{id}/disponibilidade", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.disponivel").value(false)); // Espera que o campo mude para false

        // Confirma no banco se realmente foi atualizado
        Produto atualizado = produtoRepository.findById(produto.getId()).orElseThrow();
        assertEquals(false, atualizado.getDisponivel());
    }

    // TESTE 6 — Buscar produtos por categoria
    @Test
    void deveBuscarProdutosPorCategoria() throws Exception {
        Restaurante restaurante = criarRestaurante("Categoria Test", "Lanches");
        salvarProduto("X-Bacon", "Bacon", 20.0, "Lanche", true, restaurante.getId());
        salvarProduto("X-Salada", "Salada", 18.0, "Lanche", true, restaurante.getId());

        // GET /api/produtos/categoria/Lanche
        mockMvc.perform(get("/api/produtos/categoria/Lanche")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // Espera que a lista tenha pelo menos 2 produtos
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(2))));
    }

    // TESTE 7 — Buscar produtos pelo nome
    @Test
    void deveBuscarProdutosPorNome() throws Exception {
        Restaurante restaurante = criarRestaurante("Nome Test", "Massas");
        salvarProduto("Lasanha Bolonhesa", "Tradicional", 28.0, "Massa", true, restaurante.getId());

        // GET /api/produtos/buscar?nome=Lasanha
        mockMvc.perform(get("/api/produtos/buscar")
                        .param("nome", "Lasanha")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nome", containsString("Lasanha")));
    }

    // -------------------- MÉTODOS AUXILIARES --------------------

    // Cria e salva um restaurante no banco
    private Restaurante criarRestaurante(String nome, String categoria) {
        Restaurante r = new Restaurante();
        r.setNome(nome);
        r.setAtivo(true);
        r.setEndereco("Rua Teste, 123");
        r.setTelefone("11999999999");
        r.setCategoria(categoria);
        r.setTempoEntrega(30);
        r.setTaxaEntrega(BigDecimal.valueOf(5.0));
        r.setAvaliacao(BigDecimal.valueOf(4.5));
        r.setHorarioFuncionamento("10:00 - 22:00");
        return restauranteRepository.save(r);
    }

    // Cria um DTO de produto (sem salvar no banco)
    private ProdutoDTO criarProdutoDTO(String nome, String descricao, double preco, String categoria, Long restauranteId) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setNome(nome);
        dto.setDescricao(descricao);
        dto.setPreco(BigDecimal.valueOf(preco));
        dto.setCategoria(categoria);
        dto.setDisponivel(true);
        dto.setRestauranteId(restauranteId);
        return dto;
    }

    // Cria e salva um produto diretamente no banco
    private Produto salvarProduto(String nome, String descricao, double preco, String categoria, boolean disponivel, Long restauranteId) {
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(BigDecimal.valueOf(preco));
        produto.setCategoria(categoria);
        produto.setDisponivel(disponivel);
        produto.setRestaurante(new Restaurante() {{ setId(restauranteId); }});
        return produtoRepository.save(produto);
    }
}
