package com.deliverytech.delivery.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.deliverytech.delivery.entity.GrupoOpcional;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;

import java.math.BigDecimal;
//import java.util.ArrayList; 
import java.util.HashSet; // Import necessário
//import java.util.List;
import java.util.Set; // Import necessário

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
        assertEquals(0, produto.getEstoque()); 
        assertNull(produto.getDisponivel()); 
        
        assertNull(produto.getPrecoBase()); 
        
        // Verifica se as coleções foram inicializadas (o que é feito na entidade)
        assertNotNull(produto.getItensPedido()); 
        assertTrue(produto.getItensPedido().isEmpty()); 
        
        assertNotNull(produto.getGruposOpcionais()); 
        assertTrue(produto.getGruposOpcionais().isEmpty()); 
    }

    @Test
    @DisplayName("Deve definir e obter propriedades corretamente (Refatorado)")
    void should_SetAndGetProperties_When_ValidValues() {
        // Given
        Produto produto = new Produto();
        BigDecimal precoBase = new BigDecimal("25.99");
        Restaurante restaurante = new Restaurante();
        restaurante.setId(10L);
        
        // CORREÇÃO ESSENCIAL: Declara a variável como Set (compatível com a entidade)
        Set<GrupoOpcional> gruposSet = new HashSet<>(); 
        
        // When
        produto.setId(100L);
        produto.setNome("X-Burger Teste");
        produto.setDescricao("Hambúrguer com queijo");
        produto.setCategoria("Lanche");
        produto.setDisponivel(true);
        produto.setEstoque(50);
        produto.setRestaurante(restaurante);
        
        produto.setPrecoBase(precoBase);
        produto.setGruposOpcionais(gruposSet); // Passa o Set

        // Then
        assertEquals(100L, produto.getId());
        assertEquals("X-Burger Teste", produto.getNome());
        // ... (outros asserts) ...
        
        assertEquals(precoBase, produto.getPrecoBase()); 
        // CORREÇÃO: Compara Set com Set. Isso resolve o erro de tipagem.
        assertEquals(gruposSet, produto.getGruposOpcionais()); 
    }

    @Test
    @DisplayName("Deve comparar produtos corretamente (baseado no ID)")
    void should_CompareProdutos_When_SameId() {
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
        produto.setPrecoBase(new BigDecimal("10.00")); 
        Restaurante restaurante = new Restaurante();
        restaurante.setId(5L);
        produto.setRestaurante(restaurante);

        // When
        String result = produto.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Batata Frita"), "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), "toString() deve conter o ID");
        
        assertTrue(result.contains("precoBase=10.00"), "toString() deve conter precoBase");
        
        assertFalse(result.contains("restaurante="), "toString() não deve incluir associações (se excluído)");
    }
}