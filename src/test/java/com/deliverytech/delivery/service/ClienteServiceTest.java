package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Anotação para integrar o JUnit 5 com o Mockito.
 * Isso inicializa os mocks (@Mock) e os injeta no System Under Test (@InjectMocks).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService (com DTOs e ModelMapper)")
class ClienteServiceTest {

    // --- DEPENDÊNCIAS MOCADAS ---

    @Mock // Mockamos o repositório para simular o comportamento do banco de dados.
    private ClienteRepository clienteRepository;

    @Mock // Mockamos o ModelMapper, pois ele é uma dependência externa da classe de serviço.
    private ModelMapper modelMapper;

    // --- SYSTEM UNDER TEST (SUT) ---

    @InjectMocks // Esta é a classe que estamos testando. O Mockito injetará os @Mocks nela.
    private ClienteServiceImpl clienteService;

    // --- OBJETOS DE APOIO PARA OS TESTES ---
    private ClienteDTO clienteDTO;
    private Cliente cliente;
    private ClienteResponseDTO clienteResponseDTO;
    private Long clienteId = 1L;
    private String clienteEmail = "joao@email.com";
    private String clienteCpf = "12345678901";

    /**
     * O método @BeforeEach é executado antes de CADA teste (@Test).
     * Isso garante que os testes sejam independentes e não compartilhem estado.
     */
    @BeforeEach
    void setUp() {
        // 1. DTO (Entrada da API) - O que o usuário envia.
        clienteDTO = new ClienteDTO();
        clienteDTO.setNome("João Silva");
        clienteDTO.setEmail(clienteEmail);
        clienteDTO.setTelefone("11999999999");
        clienteDTO.setEndereco("Rua A, 123");
        clienteDTO.setCpf(clienteCpf);

        // 2. Entidade (Banco de Dados) - O que é salvo no banco.
        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("João Silva");
        cliente.setEmail(clienteEmail);
        cliente.setCpf(clienteCpf);
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua A, 123");
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());

        // 3. ResponseDTO (Saída da API) - O que a API retorna.
        clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setId(clienteId);
        clienteResponseDTO.setNome("João Silva");
        clienteResponseDTO.setEmail(clienteEmail);
        clienteResponseDTO.setCpf(clienteCpf);
        clienteResponseDTO.setAtivo(true);
        clienteResponseDTO.setDataCadastro(cliente.getDataCadastro());
    }

    // ==========================================================
    // Testes do cadastrarCliente
    // ==========================================================

    @Test
    @DisplayName("Deve cadastrar cliente com sucesso")
    void cadastrarCliente_DeveRetornarDTO_QuandoSucesso() {
        // -----------------
        // Given (Arrange) - Configuração do cenário
        // -----------------
        // 1. Simula a verificação de duplicidade de email (retorna false, ou seja, NÃO existe)
        when(clienteRepository.existsByEmail(clienteEmail)).thenReturn(false);
        // 2. Simula a verificação de duplicidade de CPF (retorna false, ou seja, NÃO existe)
        when(clienteRepository.existsByCpf(clienteCpf)).thenReturn(false);
        // 3. Simula a conversão do DTO de entrada para a Entidade Cliente
        when(modelMapper.map(clienteDTO, Cliente.class)).thenReturn(cliente);
        // 4. Simula a ação de salvar no banco, retornando a entidade (agora com ID, data, etc.)
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        // 5. Simula a conversão final da Entidade para o DTO de resposta
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // -----------------
        // When (Act) - Execução da ação
        // -----------------
        // Executamos o método do serviço que queremos testar
        ClienteResponseDTO resultado = clienteService.cadastrarCliente(clienteDTO);

        // -----------------
        // Then (Assert) - Verificação dos resultados
        // -----------------
        // 1. Afirmamos que o resultado não é nulo
        assertNotNull(resultado);
        // 2. Afirmamos que os dados no DTO de resposta são os esperados
        assertEquals(clienteId, resultado.getId());
        assertEquals(clienteEmail, resultado.getEmail());
        
        // 3. Verificamos se os métodos mockados foram realmente chamados
        verify(clienteRepository).existsByEmail(clienteEmail); // Verificou o email
        verify(clienteRepository).existsByCpf(clienteCpf);     // Verificou o CPF
        verify(clienteRepository).save(any(Cliente.class));    // Salvou no banco
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando email já existe")
    void cadastrarCliente_DeveLancarExcecao_QuandoEmailJaExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Simulamos que o email JÁ EXISTE no banco
        when(clienteRepository.existsByEmail(clienteEmail)).thenReturn(true);

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        // Verificamos se a exceção correta (BusinessException) é lançada
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.cadastrarCliente(clienteDTO) // Tentativa de cadastro
        );

        // Verificamos se a mensagem da exceção é a esperada
        assertEquals("Email já cadastrado: " + clienteEmail, exception.getMessage());
        
        // Verificação de segurança: Garantimos que o método 'save' NUNCA foi chamado
        verify(clienteRepository, never()).save(any());
        // Também verificamos que a checagem de CPF não precisou ocorrer
        verify(clienteRepository, never()).existsByCpf(anyString());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando CPF já existe")
    void cadastrarCliente_DeveLancarExcecao_QuandoCpfJaExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Simulamos que o email está OK (não existe)
        when(clienteRepository.existsByEmail(clienteEmail)).thenReturn(false);
        // Simulamos que o CPF JÁ EXISTE
        when(clienteRepository.existsByCpf(clienteCpf)).thenReturn(true);

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        // Verificamos se a exceção correta é lançada
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.cadastrarCliente(clienteDTO)
        );

        // Verificamos a mensagem de erro
        assertEquals("CPF já cadastrado: " + clienteCpf, exception.getMessage());

        // Verificamos que a checagem de email ocorreu
        verify(clienteRepository).existsByEmail(clienteEmail);
        // Garantimos que o 'save' NUNCA foi chamado
        verify(clienteRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do buscarClientePorId
    // ==========================================================

    @Test
    @DisplayName("Buscar por ID: Deve retornar DTO quando ID existe")
    void buscarClientePorId_DeveRetornarDTO_QuandoIdExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Simulamos que o repositório encontrou o cliente e retorna um Optional com ele
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        // 2. Simulamos o mapeamento da Entidade para o DTO de resposta
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // -----------------
        // When (Act)
        // -----------------
        ClienteResponseDTO resultado = clienteService.buscarClientePorId(clienteId);

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        verify(clienteRepository).findById(clienteId); // Verifica se o 'findById' foi chamado
    }

    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarClientePorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Simulamos que o repositório NÃO encontrou o cliente (retorna um Optional vazio)
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.buscarClientePorId(clienteId)
        );

        assertEquals("Cliente não encontrado com ID: " + clienteId, exception.getMessage());
        // Garantimos que o modelMapper não foi chamado, pois a exceção ocorreu antes
        verify(modelMapper, never()).map(any(), any());
    }

    // ==========================================================
    // Testes do buscarClientePorEmail
    // ==========================================================
    
    @Test
    @DisplayName("Buscar por Email: Deve retornar DTO quando email existe")
    void buscarClientePorEmail_DeveRetornarDTO_QuandoEmailExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        when(clienteRepository.findByEmail(clienteEmail)).thenReturn(Optional.of(cliente));
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // -----------------
        // When (Act)
        // -----------------
        ClienteResponseDTO resultado = clienteService.buscarClientePorEmail(clienteEmail);

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(resultado);
        assertEquals(clienteEmail, resultado.getEmail());
        verify(clienteRepository).findByEmail(clienteEmail);
    }

    @Test
    @DisplayName("Buscar por Email: Deve lançar exceção quando email não existe")
    void buscarClientePorEmail_DeveLancarExcecao_QuandoEmailNaoExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        when(clienteRepository.findByEmail(clienteEmail)).thenReturn(Optional.empty());

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.buscarClientePorEmail(clienteEmail)
        );

        assertEquals("Cliente não encontrado com email: " + clienteEmail, exception.getMessage());
    }

    // ==========================================================
    // Testes do atualizarCliente
    // ==========================================================

    @Test
    @DisplayName("Atualizar: Deve atualizar cliente com sucesso")
    void atualizarCliente_DeveRetornarDTO_QuandoSucesso() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Criamos um DTO com dados *novos* para a atualização
        ClienteDTO dtoAtualizado = new ClienteDTO();
        dtoAtualizado.setNome("João Silva Atualizado");
        dtoAtualizado.setEmail("novoemail@email.com");
        dtoAtualizado.setCpf("00011122233");
        dtoAtualizado.setTelefone("22888888888");
        dtoAtualizado.setEndereco("Rua B, 456");

        // 2. Simulamos a busca do cliente original (que será atualizado)
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        // 3. Simulamos que o *novo* email NÃO existe (ou pertence ao próprio usuário, o service deve tratar isso)
        when(clienteRepository.existsByEmail(dtoAtualizado.getEmail())).thenReturn(false);
        // 4. Simulamos que o *novo* CPF NÃO existe
        when(clienteRepository.existsByCpf(dtoAtualizado.getCpf())).thenReturn(false);
        // 5. Simulamos o save (retornando a própria entidade 'cliente', que o service deve ter modificado)
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        // 6. Simulamos o mapeamento final para a resposta
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // -----------------
        // When (Act)
        // -----------------
        ClienteResponseDTO resultado = clienteService.atualizarCliente(clienteId, dtoAtualizado);

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(resultado);
        // 1. Verificamos as chamadas aos mocks
        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository).existsByEmail(dtoAtualizado.getEmail());
        verify(clienteRepository).existsByCpf(dtoAtualizado.getCpf());
        verify(clienteRepository).save(any(Cliente.class));
        
        // 2. Verificação crucial:
        // Checamos se a *entidade* (cliente) teve seus dados alterados ANTES de ser "salva".
        // Isso testa a lógica interna do service (a cópia de propriedades do DTO para a entidade).
        assertEquals("João Silva Atualizado", cliente.getNome());
        assertEquals("novoemail@email.com", cliente.getEmail());
        assertEquals("00011122233", cliente.getCpf());
    }

    @Test
    @DisplayName("Atualizar: Deve lançar exceção quando email já existe")
    void atualizarCliente_DeveLancarExcecao_QuandoEmailJaExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. DTO com um email duplicado
        ClienteDTO dtoAtualizado = new ClienteDTO();
        dtoAtualizado.setEmail("email.duplicado@email.com");
        dtoAtualizado.setCpf(clienteCpf); // CPF igual ao original (não deve ser checado)

        // 2. Simulamos a busca do cliente original
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        // 3. Simulamos que o email duplicado JÁ EXISTE
        when(clienteRepository.existsByEmail("email.duplicado@email.com")).thenReturn(true);
        // (Nota: O service real deve checar se o email pertence ao PRÓPRIO usuário.
        // Este teste assume a lógica mais simples: se existe, barra.)

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.atualizarCliente(clienteId, dtoAtualizado)
        );

        assertEquals("Email já cadastrado: email.duplicado@email.com", exception.getMessage());
        // 4. Garantimos que não tentou salvar
        verify(clienteRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do ativarDesativarCliente
    // ==========================================================

    @Test
    @DisplayName("Deve desativar um cliente ativo")
    void ativarDesativarCliente_DeveDesativarClienteAtivo() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Garantia inicial: O cliente no setup (@BeforeEach) começa ativo
        assertTrue(cliente.isAtivo());
        
        // 2. Simulamos a busca do cliente
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        // 3. Simulamos o 'save' (que receberá o cliente modificado)
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        
        /* * NOTA: Se o seu método 'ativarDesativarCliente' for 'void' (não retornar nada),
         * a linha abaixo é desnecessária e pode ser removida.
         * Se ele retornar um ClienteResponseDTO, ela está correta.
         */
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);
        
        // -----------------
        // When (Act)
        // -----------------
        // Executamos o método (assumindo que ele é void)
        clienteService.ativarDesativarCliente(clienteId);

        // -----------------
        // Then (Assert)
        // -----------------
        // 1. Verificação de mudança de estado:
        // Afirmamos que o estado do objeto 'cliente' foi alterado para false.
        assertFalse(cliente.isAtivo());
        
        // 2. Verificamos que o repositório foi chamado para salvar a entidade *modificada*
        verify(clienteRepository).save(cliente);
    }

    // ==========================================================
    // Testes do listarClientesAtivos
    // ==========================================================

    @Test
    @DisplayName("Deve listar clientes ativos")
    void listarClientesAtivos_DeveRetornarListaDeDTOs() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Criamos a lista de entidades que o repositório deve retornar
        List<Cliente> listaDeClientes = List.of(cliente);
        // 2. Simulamos a chamada ao repositório
        when(clienteRepository.findByAtivoTrue()).thenReturn(listaDeClientes);
        // 3. Simulamos o mapeamento (que o service fará em loop para cada item da lista)
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // -----------------
        // When (Act)
        // -----------------
        List<ClienteResponseDTO> resultado = clienteService.listarClientesAtivos();

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        // Verificamos se o item na lista de resposta é o esperado
        assertEquals(clienteEmail, resultado.get(0).getEmail());
        
        // Verificamos que o método correto do repositório foi chamado
        verify(clienteRepository).findByAtivoTrue();
    }
}