package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.impl.EnderecoServiceImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes Unitários para a classe EnderecoServiceImpl.
 * Usa Mockito para isolar a lógica de serviço das dependências.
 */
@ExtendWith(MockitoExtension.class) // Habilita a injeção de mocks do Mockito
class EnderecoServiceImplTest {

    // Dependências que serão "mockadas" (simuladas)
    @Mock
    private EnderecoRepository enderecoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ModelMapper modelMapper;

    // A classe que estamos testando. Mockito injetará os mocks acima nela.
    @InjectMocks
    private EnderecoServiceImpl enderecoService;

    // Variável para controlar o mock de métodos estáticos do SecurityUtils
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    /**
     * Configura os mocks antes de CADA teste.
     */
    @BeforeEach
    void setUp() {
        // Inicializa o mock estático do SecurityUtils
        // Isso "intercepta" todas as chamadas a SecurityUtils
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
    }

    /**
     * Limpa os mocks depois de CADA teste para não afetar outros testes.
     */
    @AfterEach
    void tearDown() {
        // Fecha o mock estático
        mockedSecurityUtils.close();
    }

    // ========================================================================
    // TESTES PARA: buscarPorUsuarioLogado()
    // ========================================================================

    @Test
    @DisplayName("Deve retornar lista de endereços do usuário logado")
    void buscarPorUsuarioLogado_DeveRetornarListaDeEnderecos() {
        // 1. ARRANGE (Organização)
        Long usuarioLogadoId = 2L; // ID do "João Cliente"
        Endereco enderecoMock = new Endereco(); // Um endereço de teste
        List<Endereco> listaDeEnderecosMock = Collections.singletonList(enderecoMock);

        // Simula o SecurityUtils: "Quando SecurityUtils.getCurrentUserId() for chamado, retorne 2L"
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioLogadoId);

        // Simula o Repositório: "Quando enderecoRepository.findByUsuarioId(2L) for chamado, retorne a lista mock"
        when(enderecoRepository.findByUsuarioIdAndAtivoIsTrue(usuarioLogadoId)).thenReturn(listaDeEnderecosMock);

        // 2. ACT (Ação)
        // Chama o método que queremos testar
        List<Endereco> resultado = enderecoService.buscarPorUsuarioLogado();

        // 3. ASSERT (Verificação)
        assertNotNull(resultado); // A lista não deve ser nula
        assertEquals(1, resultado.size()); // Deve conter 1 item
        assertEquals(enderecoMock, resultado.get(0)); // O item deve ser o nosso mock
        
        // Verifica se os mocks foram chamados
        mockedSecurityUtils.verify(SecurityUtils::getCurrentUserId); // Verifica se pegamos o ID do usuário
        verify(enderecoRepository, times(1)).findByUsuarioIdAndAtivoIsTrue(usuarioLogadoId); // Verifica se o repo foi chamado
    }
    
    // (Poderíamos adicionar um teste para quando SecurityUtils lança exceção, mas vamos focar no caminho feliz)

    // ========================================================================
    // TESTES PARA: salvarNovoEndereco()
    // ========================================================================

    @Test
    @DisplayName("Deve salvar e retornar novo endereço com sucesso")
    void salvarNovoEndereco_DeveSalvarComSucesso() {
        // 1. ARRANGE (Organização)
        Long usuarioLogadoId = 2L;
        EnderecoDTO dtoEntrada = new EnderecoDTO(); // DTO que vem do Postman
        dtoEntrada.setRua("Rua Nova");
        
        Usuario usuarioMock = new Usuario(); // O usuário dono do endereço
        usuarioMock.setId(usuarioLogadoId);
        
        Endereco enderecoNaoSalvo = new Endereco(); // O endereço antes de salvar (mapeado pelo ModelMapper)
        enderecoNaoSalvo.setRua("Rua Nova");
        
        Endereco enderecoSalvo = new Endereco(); // O endereço depois de salvar (com ID)
        enderecoSalvo.setId(5L);
        enderecoSalvo.setRua("Rua Nova");
        enderecoSalvo.setUsuario(usuarioMock);

        // Simula o SecurityUtils
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioLogadoId);

        // Simula o UsuarioRepository
        when(usuarioRepository.findById(usuarioLogadoId)).thenReturn(Optional.of(usuarioMock));

        // Simula o ModelMapper
        when(modelMapper.map(dtoEntrada, Endereco.class)).thenReturn(enderecoNaoSalvo);

        // Simula o EnderecoRepository (ArgumentCaptor captura o objeto enviado para o 'save')
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoSalvo);
        
        // 2. ACT (Ação)
        Endereco resultado = enderecoService.salvarNovoEndereco(dtoEntrada);

        // 3. ASSERT (Verificação)
        assertNotNull(resultado);
        assertEquals(5L, resultado.getId()); // Verifica se o ID foi setado
        assertEquals(usuarioMock, resultado.getUsuario()); // Verifica se o usuário foi associado
        
        // Verifica se o ModelMapper foi chamado
        verify(modelMapper, times(1)).map(dtoEntrada, Endereco.class); 
        // Verifica se o repositório 'save' foi chamado
        verify(enderecoRepository, times(1)).save(enderecoNaoSalvo); 
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException se o usuário não for encontrado")
    void salvarNovoEndereco_DeveLancarExcecao_QuandoUsuarioNaoEncontrado() {
        // 1. ARRANGE
        Long usuarioLogadoId = 99L; // Um ID que não existe
        EnderecoDTO dtoEntrada = new EnderecoDTO();
        
        // Simula o SecurityUtils
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(usuarioLogadoId);

        // Simula o UsuarioRepository retornando "vazio"
        when(usuarioRepository.findById(usuarioLogadoId)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT
        // Verifica se a AÇÃO (chamar o service) lança a exceção esperada
        EntityNotFoundException excecao = assertThrows(EntityNotFoundException.class, () -> {
            enderecoService.salvarNovoEndereco(dtoEntrada);
        });

        // Verifica a mensagem da exceção
        assertEquals("Usuário com ID 99 não encontrado.", excecao.getMessage());
        
        // Garante que o 'save' nunca foi chamado
        verify(enderecoRepository, never()).save(any(Endereco.class)); 
    }
}