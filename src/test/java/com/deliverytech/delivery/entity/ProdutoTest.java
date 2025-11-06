package com.deliverytech.delivery.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList; // IMPORT ADICIONADO (para testar o setter)
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários da Entidade Produto (Refatorado)")
class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto com construtor padrão (Refatorado)")
    void should_CreateProduto_When_DefaultConstructor() {
        // Given & When
        Produto produto = new Produto();

        // Then
        assertNotNull(produto);
        assertNull(produto.getId());
        assertNull(produto.getNome());
        assertEquals(0, produto.getEstoque()); // int primitivo
        assertNull(produto.getDisponivel());  // Boolean (objeto)
        
        // --- CORREÇÃO (GARGALO 2) ---
        assertNull(produto.getPrecoBase()); // <-- CORRIGIDO (era getPreco)
        
        // Verifica se as listas foram inicializadas (boa prática na entidade)
        assertNotNull(produto.getItensPedido());
        assertTrue(produto.getItensPedido().isEmpty());
        assertNotNull(produto.getGruposOpcionais()); // <-- NOVO TESTE
        assertTrue(produto.getGruposOpcionais().isEmpty()); // <-- NOVO TESTE
    }

    @Test
    @DisplayName("Deve definir e obter propriedades corretamente (Refatorado)")
    void should_SetAndGetProperties_When_ValidValues() {
        // Given
        Produto produto = new Produto();
        BigDecimal precoBase = new BigDecimal("25.99");
        Restaurante restaurante = new Restaurante();
        restaurante.setId(10L);
        List<GrupoOpcional> grupos = new ArrayList<>(); // Lista de opcionais

        // When
        produto.setId(100L);
        produto.setNome("X-Burger Teste");
        produto.setDescricao("Hambúrguer com queijo");
        produto.setCategoria("Lanche");
        produto.setDisponivel(true);
        produto.setEstoque(50);
        produto.setRestaurante(restaurante);
        
        // --- CORREÇÃO (GARGALO 2) ---
        produto.setPrecoBase(precoBase); // <-- CORRIGIDO (era setPreco)
        produto.setGruposOpcionais(grupos); // <-- NOVO TESTE

        // Then
        assertEquals(100L, produto.getId());
        assertEquals("X-Burger Teste", produto.getNome());
        assertEquals("Hambúrguer com queijo", produto.getDescricao());
        assertEquals("Lanche", produto.getCategoria());
        assertTrue(produto.getDisponivel());
        assertEquals(50, produto.getEstoque());
        assertEquals(restaurante, produto.getRestaurante());
        assertNotNull(produto.getItensPedido());
        
        // --- CORREÇÃO (GARGALO 2) ---
        assertEquals(precoBase, produto.getPrecoBase()); // <-- CORRIGIDO (era getPreco)
        assertEquals(grupos, produto.getGruposOpcionais()); // <-- NOVO TESTE
    }

    @Test
    @DisplayName("Deve comparar produtos corretamente (baseado no ID)")
    void should_CompareProdutos_When_SameId() {
        // (Este teste estava OK, pois @EqualsAndHashCode(of = "id") não mudou)
        // Given
        Produto prod1 = new Produto();
        prod1.setId(1L);
        Produto prod2 = new Produto();
        prod2.setId(1L);
        Produto prod3 = new Produto();
        prod3.setId(2L);

        // Then
        assertEquals(prod1, prod2, "Produtos com mesmo ID devem ser iguais");
        assertNotEquals(prod1, prod3, "Produtos com IDs diferentes não devem ser iguais");
    }

    @Test
    @DisplayName("Deve gerar representação em string corretamente (Refatorado)")
    void should_GenerateToString_When_Called() {
        // Given
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Batata Frita");
        produto.setPrecoBase(new BigDecimal("10.00")); // <-- CORRIGIDO
        Restaurante restaurante = new Restaurante();
        restaurante.setId(5L);
        produto.setRestaurante(restaurante);

        // When
        String result = produto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Batata Frita"), "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), "toString() deve conter o ID");
        
        // Verifica se o novo campo (precoBase) está lá, e o antigo (preco) não
        assertTrue(result.contains("precoBase=10.00"), "toString() deve conter precoBase");
        assertFalse(result.contains("preco="), "toString() não deve conter o campo 'preco' antigo");
        
        // (Assumindo que 'restaurante', 'itensPedido' e 'gruposOpcionais' 
        //  estão com @ToString.Exclude na entidade Produto)
        assertFalse(result.contains("restaurante="), "toString() não deve incluir associações (se excluído)");
    }
}