package com.deliverytech.delivery.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários da Entidade Restaurante (Refatorado)")
class RestauranteTest {

    @Test
    @DisplayName("Deve criar restaurante com construtor padrão (Refatorado)")
    void should_CreateRestaurante_When_DefaultConstructor() {
        // Given & When
        Restaurante restaurante = new Restaurante();

        // Then
        assertNotNull(restaurante);
        assertNull(restaurante.getId());
        assertNull(restaurante.getNome());
        assertNull(restaurante.getAtivo());
        assertNull(restaurante.getTaxaEntrega());
        
        // --- CORREÇÃO (GARGALO 1) ---
        assertNull(restaurante.getEndereco()); // <-- Verifica o novo campo (objeto)
        
        // (Listas - OK)
        // (Nota: Se a sua entidade @Data inicializa as listas, 
        //  os testes de 'assertNull' para 'produtos' e 'pedidos' falharão.
        //  Eles devem ser 'assertNotNull' e 'assertTrue(isEmpty())' nesse caso)
        assertNull(restaurante.getProdutos());
        assertNull(restaurante.getPedidos());
    }

    @Test
    @DisplayName("Deve definir e obter propriedades corretamente (Refatorado)")
    void should_SetAndGetProperties_When_ValidValues() {
        // Given
        Restaurante restaurante = new Restaurante();
        BigDecimal taxa = new BigDecimal("5.50");
        BigDecimal avaliacao = new BigDecimal("4.5");
        
        // --- CORREÇÃO (GARGALO 1) ---
        // Precisamos de um objeto Endereco para o teste
        Endereco mockEndereco = new Endereco();
        mockEndereco.setId(1L);
        mockEndereco.setRua("Rua Teste, 123");
        // --- FIM DA CORREÇÃO ---

        // When
        restaurante.setId(1L);
        restaurante.setNome("Pizzaria Teste");
        restaurante.setCategoria("Pizza");
        restaurante.setTelefone("11987654321");
        restaurante.setTaxaEntrega(taxa);
        restaurante.setAtivo(true);
        restaurante.setAvaliacao(avaliacao);
        restaurante.setTempoEntrega(30);
        restaurante.setHorarioFuncionamento("18:00-23:00");
        
        // --- CORREÇÃO (GARGALO 1) ---
        restaurante.setEndereco(mockEndereco); // <-- CORRIGIDO
        // --- FIM DA CORREÇÃO ---

        // Then
        assertEquals(1L, restaurante.getId());
        assertEquals("Pizzaria Teste", restaurante.getNome());
        assertEquals("Pizza", restaurante.getCategoria());
        assertEquals("11987654321", restaurante.getTelefone());
        assertEquals(taxa, restaurante.getTaxaEntrega());
        assertTrue(restaurante.getAtivo());
        assertEquals(avaliacao, restaurante.getAvaliacao());
        assertEquals(30, restaurante.getTempoEntrega());
        assertEquals("18:00-23:00", restaurante.getHorarioFuncionamento());

        // --- CORREÇÃO (GARGALO 1) ---
        assertEquals(mockEndereco, restaurante.getEndereco()); // <-- CORRIGIDO
        // --- FIM DA CORREÇÃO ---
    }

    @Test
    @DisplayName("Deve comparar restaurantes corretamente (baseado no ID)")
    void should_CompareRestaurantes_When_SameId() {
        // (Este teste estava OK)
        Restaurante rest1 = new Restaurante();
        rest1.setId(1L);
        Restaurante rest2 = new Restaurante();
        rest2.setId(1L);
        Restaurante rest3 = new Restaurante();
        rest3.setId(2L);

        // Then
        assertEquals(rest1, rest2, "Restaurantes com mesmo ID devem ser iguais");
        assertNotEquals(rest1, rest3, "Restaurantes com IDs diferentes não devem ser iguais");
    }

    @Test
    @DisplayName("Deve gerar representação em string corretamente (Refatorado)")
    void should_GenerateToString_When_Called() {
        // Given
        Restaurante restaurante = new Restaurante();
        restaurante.setId(1L);
        restaurante.setNome("Cantina Italiana");

        // When
        String result = restaurante.toString();

        // Then
        // (Assume @ToString(exclude = {"produtos", "pedidos", "endereco"}) na Entidade)
        assertNotNull(result);
        assertTrue(result.contains("Cantina Italiana"), "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), "toString() deve conter o ID");
        assertFalse(result.contains("produtos="), "toString() não deve incluir 'produtos' (excluído)");
        assertFalse(result.contains("pedidos="), "toString() não deve incluir 'pedidos' (excluído)");
        
        // --- CORREÇÃO (GARGALO 1) ---
        assertFalse(result.contains("endereco="), "toString() não deve incluir 'endereco' (excluído)"); // <-- VERIFICAÇÃO ADICIONADA
    }
}