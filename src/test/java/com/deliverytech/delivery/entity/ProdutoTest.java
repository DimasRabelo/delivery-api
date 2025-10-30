package com.deliverytech.delivery.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
//import java.util.ArrayList; 


import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes Unitários da Entidade Produto")
class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto com construtor padrão")
    void should_CreateProduto_When_DefaultConstructor() {
        // Given & When
        Produto produto = new Produto();

        // Then
        assertNotNull(produto);
        assertNull(produto.getId());
        assertNull(produto.getNome());
        assertNull(produto.getPreco());
        assertEquals(0, produto.getEstoque()); // int primitivo começa com 0
        assertNull(produto.getDisponivel());  // Boolean (objeto) começa null
        assertNotNull(produto.getItensPedido()); // Lista inicializada na entidade
        assertTrue(produto.getItensPedido().isEmpty()); // Deve estar vazia
    }

    @Test
    @DisplayName("Deve definir e obter propriedades corretamente")
    void should_SetAndGetProperties_When_ValidValues() {
        // Given
        Produto produto = new Produto();
        BigDecimal preco = new BigDecimal("25.99");
        Restaurante restaurante = new Restaurante(); // Restaurante mock/simples para o teste
        restaurante.setId(10L);
        // Se inicializar a lista na entidade, use a lista inicializada
        // List<ItemPedido> itens = new ArrayList<>();

        // When
        produto.setId(100L);
        produto.setNome("X-Burger Teste");
        produto.setDescricao("Hambúrguer com queijo");
        produto.setPreco(preco);
        produto.setCategoria("Lanche");
        produto.setDisponivel(true);
        produto.setEstoque(50);
        produto.setRestaurante(restaurante);
        // produto.setItensPedido(itens); // Só faz sentido se a lista não for final ou inicializada

        // Then
        assertEquals(100L, produto.getId());
        assertEquals("X-Burger Teste", produto.getNome());
        assertEquals("Hambúrguer com queijo", produto.getDescricao());
        assertEquals(preco, produto.getPreco());
        assertEquals("Lanche", produto.getCategoria());
        assertTrue(produto.getDisponivel());
        assertEquals(50, produto.getEstoque());
        assertEquals(restaurante, produto.getRestaurante());
        assertNotNull(produto.getItensPedido()); // Deve estar inicializada
    }

    @Test
    @DisplayName("Deve comparar produtos corretamente (baseado no ID)")
    void should_CompareProdutos_When_SameId() {
        // Given
        Produto prod1 = new Produto();
        prod1.setId(1L);

        Produto prod2 = new Produto();
        prod2.setId(1L);

        Produto prod3 = new Produto();
        prod3.setId(2L);

        // Then
        // --> Certifique-se que sua classe Produto tem @EqualsAndHashCode(of = "id") do Lombok
        assertEquals(prod1, prod2, "Produtos com mesmo ID devem ser iguais");
        assertNotEquals(prod1, prod3, "Produtos com IDs diferentes não devem ser iguais");
        assertNotEquals(prod1, null);
        assertNotEquals(prod1, new Object());

        assertEquals(prod1.hashCode(), prod2.hashCode(), "Produtos com mesmo ID devem ter mesmo hashCode");
    }

    @Test
    @DisplayName("Deve gerar representação em string corretamente")
    void should_GenerateToString_When_Called() {
        // Given
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setNome("Batata Frita");
        Restaurante restaurante = new Restaurante();
        restaurante.setId(5L); // Definindo um ID para o restaurante associado
        produto.setRestaurante(restaurante); // Associando o restaurante

        // When
        String result = produto.toString();

        // Then
        // --> Certifique-se que sua classe Produto tem @ToString (e talvez @ToString.Exclude em associações complexas)
        assertNotNull(result);
        assertTrue(result.contains("Batata Frita"), "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), "toString() deve conter o ID");
        // Verifica se a representação do restaurante associado está presente (pode variar com a implementação do toString de Restaurante)
        // Se Restaurante tiver @ToString, pode aparecer algo como Restaurante(id=5, ...)
        // Se Restaurante não tiver @ToString, pode aparecer algo como com.deliverytech.delivery.entity.Restaurante@<hashcode>
        assertTrue(result.contains("restaurante="), "toString() deve mencionar a associação com restaurante");
        // Verifica se a lista itensPedido foi excluída ou está presente (se não foi excluída)
        // assertTrue(result.contains("itensPedido=[]"), "toString() deve mostrar lista vazia se não excluída"); // Se não usar @ToString.Exclude
        // assertFalse(result.contains("itensPedido="), "toString() não deve incluir 'itensPedido' se excluído"); // Se usar @ToString.Exclude
    }
}