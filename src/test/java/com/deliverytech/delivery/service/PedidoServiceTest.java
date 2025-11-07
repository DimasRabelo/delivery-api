package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.ItemPedidoDTO;
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.*; // Importa todas as entidades
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.audit.AuditService;
import com.deliverytech.delivery.service.impl.PedidoServiceImpl;
import com.deliverytech.delivery.service.metrics.MetricsService;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList; 
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PedidoService (Refatorado)")
class PedidoServiceTest {

    // (Mocks, SUT, Captor - OK)
    @Mock private PedidoRepository pedidoRepository;
    @Mock private RestauranteRepository restauranteRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private MetricsService metricsService;
    @Mock private AuditService auditService;
    @Mock private Timer.Sample timerSample;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private ItemOpcionalRepository itemOpcionalRepository;
    @Mock private ClienteRepository clienteRepository; 
    @InjectMocks
    private PedidoServiceImpl pedidoService; 
    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    // (Dados de Teste Padrão - OK)
    private Usuario usuarioAtivo;
    private Cliente clienteAtivo;
    private Endereco enderecoAtivo;
    private Restaurante restauranteAberto;
    private Produto produto1;
    private ItemOpcional opcional1;
    private PedidoDTO pedidoDTO;
    private CalculoPedidoDTO calculoDTO;
    private Pedido pedidoSalvo;
    private PedidoResponseDTO pedidoResponseDTO;
    
    // (NOVO: Objeto de GrupoOpcional para o mock)
    private GrupoOpcional grupoOpcionalMock;


    @BeforeEach
    void setUp() {
        // --- Setup Refatorado (Gargalos 1, 2, 4) ---
        
        // 1. Cliente e Usuário
        usuarioAtivo = new Usuario();
        usuarioAtivo.setId(1L);
        usuarioAtivo.setEmail("joao.teste@email.com");
        usuarioAtivo.setRole(Role.CLIENTE);
        usuarioAtivo.setAtivo(true);
        
        clienteAtivo = new Cliente();
        clienteAtivo.setId(1L);
        clienteAtivo.setNome("Cliente Teste");
        clienteAtivo.setUsuario(usuarioAtivo);
        usuarioAtivo.setCliente(clienteAtivo);

        // 2. Endereço
        enderecoAtivo = new Endereco();
        enderecoAtivo.setId(5L);
        enderecoAtivo.setUsuario(usuarioAtivo);
        enderecoAtivo.setRua("Rua de Teste");
        
        // 3. Restaurante
        restauranteAberto = new Restaurante();
        restauranteAberto.setId(10L);
        restauranteAberto.setTaxaEntrega(new BigDecimal("5.00"));
        
        // --- CORREÇÃO (Erro 1: Restaurante não está disponível) ---
        restauranteAberto.setAtivo(true); // <-- ESTAVA FALTANDO
        
        // 4. Produto
        produto1 = new Produto();
        produto1.setId(100L);
        produto1.setPrecoBase(new BigDecimal("10.00"));
        produto1.setRestaurante(restauranteAberto);
        produto1.setDisponivel(true); // (Adicionado para o teste 'criarPedido')
        produto1.setEstoque(10); // (Adicionado para o teste 'criarPedido')
        
        // --- CORREÇÃO (Erro 2: Opcional Inválido) ---
        // 5. Grupo Opcional
        grupoOpcionalMock = new GrupoOpcional();
        grupoOpcionalMock.setId(50L);
        grupoOpcionalMock.setProduto(produto1); // <-- Linka o grupo ao produto
        
        // 6. Opcional
        opcional1 = new ItemOpcional();
        opcional1.setId(1000L);
        opcional1.setPrecoAdicional(new BigDecimal("3.00"));
        opcional1.setGrupoOpcional(grupoOpcionalMock); // <-- Linka o opcional ao grupo
        // --- FIM DA CORREÇÃO ---

        // 7. DTOs
        ItemPedidoDTO itemDTO1 = new ItemPedidoDTO();
        itemDTO1.setProdutoId(100L);
        itemDTO1.setQuantidade(2); 
        itemDTO1.setOpcionaisIds(List.of(1000L)); 

        pedidoDTO = new PedidoDTO();
        pedidoDTO.setRestauranteId(10L);
        pedidoDTO.setItens(List.of(itemDTO1));
        pedidoDTO.setEnderecoEntregaId(5L); 
        pedidoDTO.setMetodoPagamento("PIX"); 

        calculoDTO = new CalculoPedidoDTO();
        calculoDTO.setRestauranteId(10L);
        calculoDTO.setItens(List.of(itemDTO1));
        
        pedidoSalvo = new Pedido();
        pedidoSalvo.setId(1L);
        pedidoSalvo.setItens(new ArrayList<>()); 
        
        pedidoResponseDTO = new PedidoResponseDTO();
        // 8. Mocks
        lenient().when(metricsService.iniciarTimerPedido()).thenReturn(timerSample);
         lenient().when(metricsService.iniciarTimerBanco()).thenReturn(timerSample);
            
              mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class); // use tipo explícito
              mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L); // stub static method via MockedStatic
    }
    
    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }


    // ==========================================================
    // Testes: criarPedido (REFATORADO)
    // ==========================================================
    @Test
    @DisplayName("Deve criar pedido com sucesso e calcular totais corretos (Refatorado)")
    void criarPedido_DeveSalvarComTotaisCorretos_QuandoValido() {
        // (Given)
        // (Usando doReturn() para evitar erro de inferência de tipo T)
        doReturn(Optional.of(usuarioAtivo)).when(usuarioRepository).findById(1L);
        doReturn(Optional.of(restauranteAberto)).when(restauranteRepository).findById(10L);
        doReturn(Optional.of(enderecoAtivo)).when(enderecoRepository).findById(5L);
        doReturn(Optional.of(produto1)).when(produtoRepository).findById(100L);
        doReturn(Optional.of(opcional1)).when(itemOpcionalRepository).findById(1000L);
        
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        
        when(modelMapper.map(any(Pedido.class), eq(PedidoResponseDTO.class)))
            .thenReturn(pedidoResponseDTO); 

        // (When)
        pedidoService.criarPedido(pedidoDTO);

        // (Then)
        verify(pedidoRepository).save(pedidoCaptor.capture());
        Pedido pedidoCapturado = pedidoCaptor.getValue(); 
        
        // (Asserts... OK)
        ItemPedido itemCapturado = pedidoCapturado.getItens().get(0);
        assertEquals(0, new BigDecimal("13.00").compareTo(itemCapturado.getPrecoUnitario()));
        assertEquals(0, new BigDecimal("26.00").compareTo(itemCapturado.getSubtotal()));
        assertEquals(clienteAtivo, pedidoCapturado.getCliente());
        assertEquals(enderecoAtivo, pedidoCapturado.getEnderecoEntrega());
        assertEquals(0, new BigDecimal("26.00").compareTo(pedidoCapturado.getSubtotal()));
        assertEquals(0, new BigDecimal("31.00").compareTo(pedidoCapturado.getValorTotal()));
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se cliente estiver inativo (Refatorado)")
    void criarPedido_DeveLancarExcecao_QuandoClienteInativo() {
        // (Given)
        usuarioAtivo.setAtivo(false); 
        doReturn(Optional.of(usuarioAtivo)).when(usuarioRepository).findById(1L);
        
        // (When & Then)
        assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));
        
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve calcular o total do pedido (Refatorado)")
    void calcularTotalPedido_DeveRetornarCalculoCorreto() {
        // (Given)
        doReturn(Optional.of(produto1)).when(produtoRepository).findById(100L); 
        doReturn(Optional.of(opcional1)).when(itemOpcionalRepository).findById(1000L); 
        doReturn(Optional.of(restauranteAberto)).when(restauranteRepository).findById(10L); 
        
        // (When)
        CalculoPedidoResponseDTO response = pedidoService.calcularTotalPedido(calculoDTO);
        
        // (Then)
        assertNotNull(response);
        assertEquals(0, new BigDecimal("26.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("31.00").compareTo(response.getTotal()));
    }
    
    // ==========================================================
    // O RESTO DOS MÉTODOS (atualizarStatus, cancelar, etc.)
    // ==========================================================
    
    @Test
    @DisplayName("Atualizar Status: Deve permitir transição válida (PENDENTE -> CONFIRMADO)")
    void atualizarStatusPedido_DeveAtualizarStatus_QuandoTransicaoValida() {
        pedidoSalvo.setStatus(StatusPedido.PENDENTE); 
        doReturn(Optional.of(pedidoSalvo)).when(pedidoRepository).findById(1L); 
        
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        when(modelMapper.map(pedidoSalvo, PedidoResponseDTO.class)).thenReturn(pedidoResponseDTO);
        
        pedidoService.atualizarStatusPedido(1L, StatusPedido.CONFIRMADO);
        
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CONFIRMADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Atualizar Status: Deve lançar exceção em transição inválida (PENDENTE -> ENTREGUE)")
    void atualizarStatusPedido_DeveLancarExcecao_QuandoTransicaoInvalida() {
        pedidoSalvo.setStatus(StatusPedido.PENDENTE); 
        doReturn(Optional.of(pedidoSalvo)).when(pedidoRepository).findById(1L); 
        
        assertThrows(BusinessException.class,
                () -> pedidoService.atualizarStatusPedido(1L, StatusPedido.ENTREGUE));
    }
    
    @Test
    @DisplayName("Cancelar Pedido: Deve permitir cancelar pedido PENDENTE")
    void cancelarPedido_DeveCancelar_QuandoStatusPendente() {
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        doReturn(Optional.of(pedidoSalvo)).when(pedidoRepository).findById(1L); 
        
        pedidoService.cancelarPedido(1L);
        
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CANCELADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Cancelar Pedido: Deve lançar exceção se pedido já SAIU_PARA_ENTREGA")
    void cancelarPedido_DeveLancarExcecao_QuandoStatusInvalido() {
        pedidoSalvo.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        doReturn(Optional.of(pedidoSalvo)).when(pedidoRepository).findById(1L); 
        
        assertThrows(BusinessException.class,
                () -> pedidoService.cancelarPedido(1L));
    }
    
    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarPedidoPorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        doReturn(Optional.empty()).when(pedidoRepository).findById(99L); 
        
        assertThrows(EntityNotFoundException.class,
                () -> pedidoService.buscarPedidoPorId(99L));
    }
}