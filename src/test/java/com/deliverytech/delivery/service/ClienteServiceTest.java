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

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService (com DTOs e ModelMapper)")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ModelMapper modelMapper; // Essencial mockar esta dependência

    @InjectMocks
    private ClienteServiceImpl clienteService;

    // --- OBJETOS DE APOIO PARA OS TESTES ---
    private ClienteDTO clienteDTO;
    private Cliente cliente;
    private ClienteResponseDTO clienteResponseDTO;
    private Long clienteId = 1L;
    private String clienteEmail = "joao@email.com";
    private String clienteCpf = "12345678901";

    @BeforeEach
    void setUp() {
        // 1. DTO (Entrada da API)
        clienteDTO = new ClienteDTO();
        clienteDTO.setNome("João Silva");
        clienteDTO.setEmail(clienteEmail);
        clienteDTO.setTelefone("11999999999");
        clienteDTO.setEndereco("Rua A, 123");
        clienteDTO.setCpf(clienteCpf);

        // 2. Entidade (Banco de Dados)
        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("João Silva");
        cliente.setEmail(clienteEmail);
        cliente.setCpf(clienteCpf);
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua A, 123");
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());

        // 3. ResponseDTO (Saída da API)
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
        // Given
        when(clienteRepository.existsByEmail(clienteEmail)).thenReturn(false);
        when(clienteRepository.existsByCpf(clienteCpf)).thenReturn(false);
        when(modelMapper.map(clienteDTO, Cliente.class)).thenReturn(cliente);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        ClienteResponseDTO resultado = clienteService.cadastrarCliente(clienteDTO);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        assertEquals(clienteEmail, resultado.getEmail());
        assertEquals(clienteCpf, resultado.getCpf());
        verify(clienteRepository).existsByEmail(clienteEmail);
        verify(clienteRepository).existsByCpf(clienteCpf);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando email já existe")
    void cadastrarCliente_DeveLancarExcecao_QuandoEmailJaExiste() {
        // Given
        when(clienteRepository.existsByEmail(clienteEmail)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.cadastrarCliente(clienteDTO)
        );

        assertEquals("Email já cadastrado: " + clienteEmail, exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção quando CPF já existe")
    void cadastrarCliente_DeveLancarExcecao_QuandoCpfJaExiste() {
        // Given
        when(clienteRepository.existsByEmail(clienteEmail)).thenReturn(false); // Email OK
        when(clienteRepository.existsByCpf(clienteCpf)).thenReturn(true);   // CPF Duplicado

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.cadastrarCliente(clienteDTO)
        );

        assertEquals("CPF já cadastrado: " + clienteCpf, exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do buscarClientePorId
    // ==========================================================

    @Test
    @DisplayName("Buscar por ID: Deve retornar DTO quando ID existe")
    void buscarClientePorId_DeveRetornarDTO_QuandoIdExiste() {
        // Given
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        ClienteResponseDTO resultado = clienteService.buscarClientePorId(clienteId);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarClientePorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        // Given
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.buscarClientePorId(clienteId)
        );

        assertEquals("Cliente não encontrado com ID: " + clienteId, exception.getMessage());
    }

    // ==========================================================
    // Testes do buscarClientePorEmail
    // ==========================================================
    
    @Test
    @DisplayName("Buscar por Email: Deve retornar DTO quando email existe")
    void buscarClientePorEmail_DeveRetornarDTO_QuandoEmailExiste() {
        // Given
        when(clienteRepository.findByEmail(clienteEmail)).thenReturn(Optional.of(cliente));
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        ClienteResponseDTO resultado = clienteService.buscarClientePorEmail(clienteEmail);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteEmail, resultado.getEmail());
        verify(clienteRepository).findByEmail(clienteEmail);
    }

    @Test
    @DisplayName("Buscar por Email: Deve lançar exceção quando email não existe")
    void buscarClientePorEmail_DeveLancarExcecao_QuandoEmailNaoExiste() {
        // Given
        when(clienteRepository.findByEmail(clienteEmail)).thenReturn(Optional.empty());

        // When & Then
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
        // Given
        ClienteDTO dtoAtualizado = new ClienteDTO();
        dtoAtualizado.setNome("João Silva Atualizado");
        dtoAtualizado.setEmail("novoemail@email.com");
        dtoAtualizado.setCpf("00011122233");
        dtoAtualizado.setTelefone("22888888888");
        dtoAtualizado.setEndereco("Rua B, 456");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail(dtoAtualizado.getEmail())).thenReturn(false);
        when(clienteRepository.existsByCpf(dtoAtualizado.getCpf())).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente); // Retorna a entidade atualizada
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO); // Simula mapeamento

        // When
        ClienteResponseDTO resultado = clienteService.atualizarCliente(clienteId, dtoAtualizado);

        // Then
        assertNotNull(resultado);
        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository).existsByEmail(dtoAtualizado.getEmail());
        verify(clienteRepository).existsByCpf(dtoAtualizado.getCpf());
        verify(clienteRepository).save(any(Cliente.class));
        
        // Verifica se os dados na entidade 'cliente' foram atualizados antes de salvar
        assertEquals("João Silva Atualizado", cliente.getNome());
        assertEquals("novoemail@email.com", cliente.getEmail());
        assertEquals("00011122233", cliente.getCpf());
    }

    @Test
    @DisplayName("Atualizar: Deve lançar exceção quando email já existe")
    void atualizarCliente_DeveLancarExcecao_QuandoEmailJaExiste() {
        // Given
        ClienteDTO dtoAtualizado = new ClienteDTO();
        dtoAtualizado.setEmail("email.duplicado@email.com");
        dtoAtualizado.setCpf(clienteCpf); // CPF igual ao original

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail("email.duplicado@email.com")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.atualizarCliente(clienteId, dtoAtualizado)
        );

        assertEquals("Email já cadastrado: email.duplicado@email.com", exception.getMessage());
        verify(clienteRepository, never()).save(any());
    }

    // ==========================================================
    // Testes do ativarDesativarCliente
    // ==========================================================

    @Test
    @DisplayName("Deve desativar um cliente ativo")
    void ativarDesativarCliente_DeveDesativarClienteAtivo() {
        // Given
        assertTrue(cliente.isAtivo()); // Garante que está ativo
        
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);
        
        // When
        clienteService.ativarDesativarCliente(clienteId);

        // Then
        assertFalse(cliente.isAtivo()); // Verifica se o estado mudou na entidade
        verify(clienteRepository).save(cliente);
    }

    // ==========================================================
    // Testes do listarClientesAtivos
    // ==========================================================

    @Test
    @DisplayName("Deve listar clientes ativos")
    void listarClientesAtivos_DeveRetornarListaDeDTOs() {
        // Given
        List<Cliente> listaDeClientes = List.of(cliente);
        when(clienteRepository.findByAtivoTrue()).thenReturn(listaDeClientes);
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        List<ClienteResponseDTO> resultado = clienteService.listarClientesAtivos();

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(clienteEmail, resultado.get(0).getEmail());
        verify(clienteRepository).findByAtivoTrue();
    }
}