package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Usuario; // IMPORT ADICIONADO
import com.deliverytech.delivery.enums.Role; // IMPORT ADICIONADO
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository; // IMPORT ADICIONADO
import com.deliverytech.delivery.security.jwt.SecurityUtils; // IMPORT ADICIONADO
import com.deliverytech.delivery.service.impl.ClienteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic; // IMPORT ADICIONADO
import org.mockito.Mockito; // IMPORT ADICIONADO
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ClienteService (Refatorado)")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    // --- MUDANÇA: REPOSITÓRIO NECESSÁRIO ---
    @Mock
    private UsuarioRepository usuarioRepository; // <-- ADICIONADO

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    // --- OBJETOS DE APOIO (REFATORADOS) ---
    private ClienteDTO clienteDTO;
    private Cliente cliente;
    private Usuario mockUsuario; // Usuário associado
    private ClienteResponseDTO clienteResponseDTO;
    private Long clienteId = 1L; // (ID do Cliente e do Usuário)
    private String clienteEmail = "joao@email.com";
    private String clienteCpf = "12345678901";
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        
        // 1. DTO (Refatorado) - O que o usuário envia para ATUALIZAR
        clienteDTO = new ClienteDTO();
        clienteDTO.setNome("João Silva (Atualizado)");
        clienteDTO.setTelefone("11988888888");
        clienteDTO.setCpf("11122233344");
        // (email e endereco (String) REMOVIDOS)

        // 2. Entidade Usuario (Mock)
        mockUsuario = new Usuario();
        mockUsuario.setId(clienteId);
        mockUsuario.setEmail(clienteEmail);
        mockUsuario.setRole(Role.CLIENTE);
        mockUsuario.setAtivo(true);
        mockUsuario.setDataCriacao(dataCadastro);
        
        // 3. Entidade Cliente (Banco de Dados)
        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("João Silva (Original)");
        cliente.setCpf(clienteCpf);
        cliente.setTelefone("11999999999");
        cliente.setUsuario(mockUsuario); // <-- Linkado ao Usuário
        // (email, ativo, dataCadastro, endereco (String) REMOVIDOS)
        
        // Link bidirecional (para o 'getCliente()' funcionar)
        mockUsuario.setCliente(cliente);

        // 4. ResponseDTO (Refatorado) - O que a API retorna
        clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setId(clienteId);
        clienteResponseDTO.setNome("João Silva (Original)");
        clienteResponseDTO.setEmail(clienteEmail); // (Vem do Usuário)
        clienteResponseDTO.setCpf(clienteCpf);
        clienteResponseDTO.setAtivo(true); // (Vem do Usuário)
        clienteResponseDTO.setDataCadastro(dataCadastro); // (Vem do Usuário)
        // (endereco (String) REMOVIDO)
    }

    // ==========================================================
    // Testes do cadastrarCliente (REMOVIDOS)
    // O cadastro agora é feito pelo AuthService.registrarCliente()
    // ==========================================================

    @Test
    @DisplayName("Cadastrar: Deve lançar exceção (Método Obsoleto)")
    void cadastrarCliente_DeveLancarExcecao_QuandoChamado() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteService.cadastrarCliente(clienteDTO)
        );
        assertEquals("Método obsoleto. Use o endpoint de registro do AuthService.", exception.getMessage());
    }
    
    // ==========================================================
    // Testes do buscarClientePorId (OK)
    // ==========================================================

    @Test
    @DisplayName("Buscar por ID: Deve retornar DTO quando ID existe")
    void buscarClientePorId_DeveRetornarDTO_QuandoIdExiste() {
        // Given
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        // (Simula o mapToClienteResponse, que agora busca dados do Usuario)
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        ClienteResponseDTO resultado = clienteService.buscarClientePorId(clienteId);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteId, resultado.getId());
        assertEquals(clienteEmail, resultado.getEmail()); // Verifica se o email (do Usuario) foi mapeado
        assertEquals(true, resultado.isAtivo()); // Verifica se o 'ativo' (do Usuario) foi mapeado
        verify(clienteRepository).findById(clienteId);
    }
    
    // ==========================================================
    // Testes do buscarClientePorEmail (Refatorado)
    // ==========================================================
    
    @Test
    @DisplayName("Buscar por Email: Deve retornar DTO quando email existe (Refatorado)")
    void buscarClientePorEmail_DeveRetornarDTO_QuandoEmailExiste() {
        // Given
        // (CORRIGIDO: Usa o novo método do repositório)
        when(clienteRepository.findByUsuarioEmail(clienteEmail)).thenReturn(Optional.of(cliente));
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        ClienteResponseDTO resultado = clienteService.buscarClientePorEmail(clienteEmail);

        // Then
        assertNotNull(resultado);
        assertEquals(clienteEmail, resultado.getEmail());
        verify(clienteRepository).findByUsuarioEmail(clienteEmail);
    }
    
    // ==========================================================
    // Testes do atualizarCliente (Refatorado)
    // ==========================================================

    @Test
    @DisplayName("Atualizar: Deve atualizar perfil do cliente com sucesso")
    void atualizarCliente_DeveRetornarDTO_QuandoSucesso() {
        // Given
        // 1. Simula o usuário logado (o 'clienteId' agora é pego do SecurityUtils)
        // (Usamos try-with-resources para mockar o método estático)
        try (MockedStatic<SecurityUtils> mockedSecurity = Mockito.mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUserId).thenReturn(clienteId);

            // 2. Simula a busca do cliente original
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            
            // 3. Simula a verificação de duplicidade do NOVO CPF
            when(clienteRepository.existsByCpf(clienteDTO.getCpf())).thenReturn(false);
            
            // 4. Simula o save
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
            
            // 5. Simula o mapeamento final
            when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);
            // (A lógica de 'existsByEmail' foi removida)

            // When
            ClienteResponseDTO resultado = clienteService.atualizarCliente(clienteId, clienteDTO);

            // Then
            assertNotNull(resultado);
            verify(clienteRepository).findById(clienteId);
            verify(clienteRepository).existsByCpf(clienteDTO.getCpf());
            verify(clienteRepository).save(any(Cliente.class));
            
            // Verifica se a entidade 'cliente' foi atualizada ANTES de salvar
            assertEquals("João Silva (Atualizado)", cliente.getNome());
            assertEquals("11988888888", cliente.getTelefone());
            assertEquals("11122233344", cliente.getCpf());
            // (Verifica se o email NÃO foi tocado)
            assertEquals(clienteEmail, cliente.getUsuario().getEmail());
        }
    }
    
    // ==========================================================
    // Testes do ativarDesativarCliente (Refatorado)
    // ==========================================================

    @Test
    @DisplayName("Deve desativar um cliente ativo (Refatorado)")
    void ativarDesativarCliente_DeveDesativarClienteAtivo() {
        // Given
        assertTrue(cliente.getUsuario().getAtivo()); // Garante que o USUARIO começa ativo
        
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        // (Não precisamos mockar o save, pois ele é 'void')
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);
        
        // When
        clienteService.ativarDesativarCliente(clienteId);

        // Then
        // (CORRIGIDO: Verifica se 'setAtivo(false)' foi chamado no USUARIO)
        assertFalse(cliente.getUsuario().getAtivo());
        
        // Verifica que o USUARIO (não o cliente) foi salvo
        verify(usuarioRepository).save(mockUsuario);
        verify(clienteRepository, never()).save(any(Cliente.class)); // O Cliente em si não foi salvo
    }

    // ==========================================================
    // Testes do listarClientesAtivos (Refatorado)
    // ==========================================================

    @Test
    @DisplayName("Deve listar clientes ativos (Refatorado)")
    void listarClientesAtivos_DeveRetornarListaDeDTOs() {
        // Given
        List<Cliente> listaDeClientes = List.of(cliente);
        // (CORRIGIDO: Usa o novo método do repositório)
        when(clienteRepository.findByUsuarioAtivoTrue()).thenReturn(listaDeClientes);
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // When
        List<ClienteResponseDTO> resultado = clienteService.listarClientesAtivos();

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        assertEquals(clienteEmail, resultado.get(0).getEmail());
        
        verify(clienteRepository).findByUsuarioAtivoTrue();
    }
}