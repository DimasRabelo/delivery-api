package com.deliverytech.delivery.entity; // Define o pacote onde esta classe de teste reside

// Importações necessárias do JUnit 5 para anotações e asserções
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*; // Importa estaticamente todos os métodos de asserção

/**
 * Define um nome legível para esta suíte de testes, que aparecerá nos relatórios.
 * (REFATORADO para a arquitetura "Decisão 1")
 */
@DisplayName("Testes Unitários da Entidade Cliente (Refatorado)")
class ClienteTest {

    /**
     * Testa o construtor padrão da classe Cliente.
     * (Refatorado para a nova arquitetura)
     */
    @Test
    @DisplayName("Deve criar cliente com construtor padrão (Refatorado)")
    void should_CreateCliente_When_DefaultConstructor() {
        // --- Given (Dado) & When (Quando) ---
        Cliente cliente = new Cliente();

        // --- Then (Então) ---
        assertNotNull(cliente);
        assertNull(cliente.getId());
        
        // --- CAMPOS REMOVIDOS DO TESTE ---
        // (assertTrue(cliente.isAtivo());)
        // (assertNotNull(cliente.getDataCadastro());)
        
        // --- NOVAS VERIFICAÇÕES ---
        assertNull(cliente.getNome());
        assertNull(cliente.getCpf());
        assertNull(cliente.getTelefone());
        assertNull(cliente.getUsuario()); // O link para Usuario deve ser nulo
    }

    /**
     * Testa os métodos getters e setters da classe Cliente.
     * (Refatorado para a nova arquitetura)
     */
    @Test
    @DisplayName("Deve definir e obter propriedades corretamente (Refatorado)")
    void should_SetAndGetProperties_When_ValidValues() {
        // --- Given (Dado) ---
        Cliente cliente = new Cliente();
        Usuario mockUsuario = new Usuario(); // Cria um mock de Usuario para o link
        mockUsuario.setId(1L); // Simula um usuário com ID

        // --- When (Quando) ---
        // Define valores para as propriedades que RESTARAM no Cliente
        cliente.setId(10L);
        cliente.setNome("Teste");
        cliente.setTelefone("11999998888");
        cliente.setCpf("11122233344");
        cliente.setUsuario(mockUsuario); // Testa o link com Usuario
        
        // --- CAMPOS REMOVIDOS DO TESTE ---
        // cliente.setEmail("teste@email.com");
        // cliente.setEndereco("Rua Teste, 123");
        // cliente.setAtivo(false);
        // cliente.setDataCadastro(now);

        // --- Then (Então) ---
        // Verifica se cada getter retorna o valor que foi definido
        assertEquals(10L, cliente.getId());
        assertEquals("Teste", cliente.getNome());
        assertEquals("11999998888", cliente.getTelefone());
        assertEquals("11122233344", cliente.getCpf());
        assertEquals(mockUsuario, cliente.getUsuario()); // Verifica o link
        
        // --- ASSERTS REMOVIDOS ---
        // assertFalse(cliente.isAtivo());
        // assertEquals(now, cliente.getDataCadastro());
    }

    /**
     * Testa os métodos equals() e hashCode() da classe Cliente.
     * (Este teste continua válido, pois a comparação ainda é pelo ID)
     */
    @Test
    @DisplayName("Deve comparar clientes corretamente (baseado no ID)")
    void should_CompareClientes_When_SameId() {
        // --- Given (Dado) ---
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);

        Cliente cliente2 = new Cliente();
        cliente2.setId(1L);

        Cliente cliente3 = new Cliente();
        cliente3.setId(2L);

        // --- Then (Então) ---
        assertEquals(cliente1, cliente2, "Clientes com mesmo ID devem ser iguais");
        assertNotEquals(cliente1, cliente3, "Clientes com IDs diferentes não devem ser iguais");
        assertNotEquals(cliente1, null, "Cliente não deve ser igual a null");
        assertNotEquals(cliente1, new Object(), "Cliente não deve ser igual a outro tipo de objeto");
        assertEquals(cliente1.hashCode(), cliente2.hashCode(), "Clientes com mesmo ID devem ter mesmo hashCode");
    }

    /**
     * Testa o método toString() da classe Cliente.
     * (Refatorado para a nova arquitetura)
     */
    @Test
    @DisplayName("Deve gerar representação em string corretamente (Refatorado)")
    void should_GenerateToString_When_Called() {
        // --- Given (Dado) ---
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");

        // --- When (Quando) ---
        String result = cliente.toString();

        // --- Then (Então) ---
        assertNotNull(result);
        assertTrue(result.contains("João"), "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), "toString() deve conter o ID");
        
        // --- VERIFICAÇÃO REMOVIDA ---
        // assertTrue(result.contains("joao@email.com"), "toString() não deve conter email");
        
        assertTrue(result.startsWith("Cliente("), "toString() deve começar com o nome da classe");
    }
}