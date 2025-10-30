package com.deliverytech.delivery.entity; // Define o pacote onde esta classe de teste reside

// Importações necessárias do JUnit 5 para anotações e asserções
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*; // Importa estaticamente todos os métodos de asserção (assertEquals, assertTrue, etc.)

/**
 * Define um nome legível para esta suíte de testes, que aparecerá nos relatórios.
 */
@DisplayName("Testes Unitários da Entidade Cliente")
class ClienteTest {

    /**
     * Testa o construtor padrão da classe Cliente.
     * Garante que um objeto Cliente é criado com os valores iniciais esperados.
     */
    @Test
    @DisplayName("Deve criar cliente com construtor padrão")
    void should_CreateCliente_When_DefaultConstructor() {
        // --- Given (Dado) & When (Quando) ---
        // Cria uma nova instância de Cliente usando o construtor padrão (sem argumentos).
        Cliente cliente = new Cliente();

        // --- Then (Então) ---
        // Verifica se o objeto 'cliente' não é nulo (foi criado com sucesso).
        assertNotNull(cliente);
        // Verifica se o ID é nulo, pois ainda não foi persistido no banco.
        assertNull(cliente.getId());
        // Verifica se o campo 'ativo' é inicializado como 'true' (conforme definido na entidade).
        assertTrue(cliente.isAtivo());
        // Verifica se 'dataCadastro' NÃO é nula, pois é inicializada com LocalDateTime.now() na entidade.
        assertNotNull(cliente.getDataCadastro(), "Data de cadastro deve ser preenchida na criação");
    }

    /**
     * Testa os métodos getters e setters da classe Cliente.
     * Garante que podemos definir e recuperar os valores das propriedades corretamente.
     */
    @Test
    @DisplayName("Deve definir e obter propriedades corretamente")
    void should_SetAndGetProperties_When_ValidValues() {
        // --- Given (Dado) ---
        // Cria uma instância de Cliente para o teste.
        Cliente cliente = new Cliente();
        // Pega a data/hora atual para usar no teste.
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // --- When (Quando) ---
        // Define valores para todas as propriedades do objeto Cliente usando os setters.
        cliente.setId(10L);
        cliente.setNome("Teste");
        cliente.setEmail("teste@email.com");
        cliente.setTelefone("11999998888");
        cliente.setEndereco("Rua Teste, 123");
        cliente.setCpf("11122233344");
        cliente.setAtivo(false);
        cliente.setDataCadastro(now);

        // --- Then (Então) ---
        // Verifica se cada getter retorna o valor que foi definido pelo setter correspondente.
        assertEquals(10L, cliente.getId());
        assertEquals("Teste", cliente.getNome());
        assertEquals("teste@email.com", cliente.getEmail());
        assertEquals("11999998888", cliente.getTelefone());
        assertEquals("Rua Teste, 123", cliente.getEndereco());
        assertEquals("11122233344", cliente.getCpf());
        assertFalse(cliente.isAtivo()); // Verifica se o valor 'false' foi definido corretamente.
        assertEquals(now, cliente.getDataCadastro());
    }

    /**
     * Testa os métodos equals() e hashCode() da classe Cliente.
     * Garante que a comparação de objetos Cliente funcione como esperado (baseado no ID).
     * Pré-requisito: A classe Cliente DEVE ter @EqualsAndHashCode(of = "id") ou implementação manual.
     */
    @Test
    @DisplayName("Deve comparar clientes corretamente (baseado no ID)")
    void should_CompareClientes_When_SameId() {
        // --- Given (Dado) ---
        // Cria dois clientes com o mesmo ID.
        Cliente cliente1 = new Cliente();
        cliente1.setId(1L);

        Cliente cliente2 = new Cliente();
        cliente2.setId(1L);

        // Cria um terceiro cliente com ID diferente.
        Cliente cliente3 = new Cliente();
        cliente3.setId(2L);

        // --- Then (Então) ---
        // Verifica o método equals().
        // Espera-se que cliente1 seja igual a cliente2 porque têm o mesmo ID.
        assertEquals(cliente1, cliente2,
        "Clientes com mesmo ID devem ser iguais");
        // Espera-se que cliente1 NÃO seja igual a cliente3 porque têm IDs diferentes.
        assertNotEquals(cliente1, cliente3,
        "Clientes com IDs diferentes não devem ser iguais");
        // Verifica casos de borda: comparação com null e com objeto de outro tipo.
        assertNotEquals(cliente1, null,
        "Cliente não deve ser igual a null");
        assertNotEquals(cliente1, new Object(),
        "Cliente não deve ser igual a outro tipo de objeto");

        // Verifica o método hashCode().
        // Clientes iguais (mesmo ID) DEVEM ter o mesmo hashCode.
        assertEquals(cliente1.hashCode(), cliente2.hashCode(),
        "Clientes com mesmo ID devem ter mesmo hashCode");
    }

    /**
     * Testa o método toString() da classe Cliente.
     * Garante que a representação em string do objeto contenha informações relevantes.
     * Pré-requisito: A classe Cliente DEVE ter @ToString ou implementação manual.
     */
    @Test
    @DisplayName("Deve gerar representação em string corretamente")
    void should_GenerateToString_When_Called() {
        // --- Given (Dado) ---
        // Cria um cliente e define algumas propriedades.
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João");
        cliente.setEmail("joao@email.com");

        // --- When (Quando) ---
        // Chama o método toString() no objeto cliente.
        String result = cliente.toString();

        // --- Then (Então) ---
        // Verifica se a string resultante não é nula.
        assertNotNull(result);
        // Verifica se a string contém informações esperadas (nome, ID, email).
        assertTrue(result.contains("João"),
        "toString() deve conter o nome");
        assertTrue(result.contains("id=1"), // Verifica especificamente "id=1" (comum no Lombok)
        "toString() deve conter o ID");
        assertTrue(result.contains("joao@email.com"),
        "toString() deve conter o email");
        // Verifica se a string começa com o nome da classe (padrão do @ToString do Lombok).
        assertTrue(result.startsWith("Cliente("),
        "toString() deve começar com o nome da classe (se usar Lombok)");
    }

}