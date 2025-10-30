package com.deliverytech.delivery.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
//import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários da Entidade Restaurante")
class RestauranteTest {

    @Test
    @DisplayName("Deve criar restaurante com construtor padrão")
    void should_CreateRestaurante_When_DefaultConstructor() {
        // Given & When
        Restaurante restaurante = new Restaurante();

        // Then
        assertNotNull(restaurante);
        assertNull(restaurante.getId());
        assertNull(restaurante.getNome());
        assertNull(restaurante.getAtivo()); // Boolean (objeto) começa null por padrão
        assertNull(restaurante.getTaxaEntrega());
        // Listas são inicializadas pela anotação @Data (geralmente como null se não inicializadas explicitamente)
        // Se você inicializar as listas na entidade (ex: = new ArrayList<>()), mude para assertNotNull e assertTrue(isEmpty())
        assertNull(restaurante.getProdutos());
        assertNull(restaurante.getPedidos());
    }

    @Test
    @DisplayName("Deve definir e obter propriedades corretamente")
    void should_SetAndGetProperties_When_ValidValues() {
        // Given
        Restaurante restaurante = new Restaurante();
        BigDecimal taxa = new BigDecimal("5.50");
        BigDecimal avaliacao = new BigDecimal("4.5");
        // Se inicializar as listas na entidade, use as listas inicializadas
        // List<Produto> produtos = new ArrayList<>();
        // List<Pedido> pedidos = new ArrayList<>();

        // When
        restaurante.setId(1L);
        restaurante.setNome("Pizzaria Teste");
        restaurante.setCategoria("Pizza");
        restaurante.setEndereco("Rua Teste, 123");
        restaurante.setTelefone("11987654321");
        restaurante.setTaxaEntrega(taxa);
        restaurante.setAtivo(true);
        restaurante.setAvaliacao(avaliacao);
        restaurante.setTempoEntrega(30);
        restaurante.setHorarioFuncionamento("18:00-23:00");
        // restaurante.setProdutos(produtos); // Só faz sentido se a lista for inicializada
        // restaurante.setPedidos(pedidos);

        // Then
        assertEquals(1L, restaurante.getId());
        assertEquals("Pizzaria Teste", restaurante.getNome());
        assertEquals("Pizza", restaurante.getCategoria());
        assertEquals("Rua Teste, 123", restaurante.getEndereco());
        assertEquals("11987654321", restaurante.getTelefone());
        assertEquals(taxa, restaurante.getTaxaEntrega());
        assertTrue(restaurante.getAtivo());
        assertEquals(avaliacao, restaurante.getAvaliacao());
        assertEquals(30, restaurante.getTempoEntrega());
        assertEquals("18:00-23:00", restaurante.getHorarioFuncionamento());
        // assertNotNull(restaurante.getProdutos()); // Só faz sentido se a lista for inicializada
        // assertNotNull(restaurante.getPedidos());
    }

    @Test
    @DisplayName("Deve comparar restaurantes corretamente (baseado no ID)")
    void should_CompareRestaurantes_When_SameId() {
        // Given
        Restaurante rest1 = new Restaurante();
        rest1.setId(1L);

        Restaurante rest2 = new Restaurante();
        rest2.setId(1L);

        Restaurante rest3 = new Restaurante();
        rest3.setId(2L);

        // Then
        // --> Certifique-se que sua classe Restaurante tem @EqualsAndHashCode(of = "id") do Lombok
        assertEquals(rest1, rest2, "Restaurantes com mesmo ID devem ser iguais");
        assertNotEquals(rest1, rest3, "Restaurantes com IDs diferentes não devem ser iguais");
        assertNotEquals(rest1, null);
        assertNotEquals(rest1, new Object());

        assertEquals(rest1.hashCode(), rest2.hashCode(), "Restaurantes com mesmo ID devem ter mesmo hashCode");
    }

    @Test
    @DisplayName("Deve gerar representação em string corretamente")
    void should_GenerateToString_When_Called() {
        // Given
        Restaurante restaurante = new Restaurante();
        restaurante.setId(1L);
        restaurante.setNome("Cantina Italiana");

        // When
        String result = restaurante.toString();

        // Then
        // --> Certifique-se que sua classe Restaurante tem @ToString(exclude = {"produtos", "pedidos"})
        assertNotNull(result);
        assertTrue(result.contains("Cantina Italiana"), "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), "toString() deve conter o ID");
        assertFalse(result.contains("produtos="), "toString() não deve incluir 'produtos' (excluído)");
        assertFalse(result.contains("pedidos="), "toString() não deve incluir 'pedidos' (excluído)");
    }
}