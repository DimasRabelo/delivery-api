package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Pedido;
import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.entity.Restaurante;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.PedidoRepository;
import com.deliverytech.delivery.repository.ProdutoRepository;
import com.deliverytech.delivery.repository.RestauranteRepository;
import com.deliverytech.delivery.service.impl.PedidoServiceImpl;

// ⬇️⬇️ IMPORTS NOVOS ⬇️⬇️
import com.deliverytech.delivery.security.jwt.SecurityUtils; // Para mockar
import com.deliverytech.delivery.service.audit.AuditService;
import com.deliverytech.delivery.service.metrics.MetricsService;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.AfterEach; // Para fechar o mock estático
// ⬆️⬆️ FIM DOS IMPORTS NOVOS ⬆️⬆️

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*; // Importa MockedStatic
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.math.BigDecimal;
import java.util.ArrayList; 
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PedidoService (Unitário)")
class PedidoServiceTest {

    // --- 1. Mocks ---
    @Mock private PedidoRepository pedidoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private RestauranteRepository restauranteRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private MetricsService metricsService;
    @Mock private AuditService auditService;
    @Mock private Timer.Sample timerSample;

    // --- 2. SUT ---
    @InjectMocks
    private PedidoServiceImpl pedidoService; 

    // --- 3. Captor ---
    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;

    // ==========================================================
    // ⬇️⬇️ CORREÇÃO DA RUNTIMEEXCEPTION (SecurityUtils) ⬇️⬇️
    // ==========================================================
    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    // --- 4. Dados de Teste Padrão (Setup) ---
    private Cliente clienteAtivo;
    private Restaurante restauranteAberto;
    private Produto produto1;
    private Produto produto2;
    private PedidoDTO pedidoDTO;
    private Pedido pedidoSalvo;
    private CalculoPedidoDTO calculoDTO;
    private PedidoResponseDTO pedidoResponseDTO;

    @BeforeEach
    void setUp() {
        // --- (Setup dos dados... mantido) ---
        clienteAtivo = new Cliente();
        clienteAtivo.setId(1L);
        clienteAtivo.setNome("Cliente Teste");
        clienteAtivo.setAtivo(true); 

        restauranteAberto = new Restaurante();
        restauranteAberto.setId(10L);
        restauranteAberto.setNome("Restaurante Teste");
        restauranteAberto.setAtivo(true); 
        restauranteAberto.setTaxaEntrega(new BigDecimal("5.00"));

        produto1 = new Produto();
        produto1.setId(100L);
        produto1.setNome("Produto 1");
        produto1.setDisponivel(true); 
        produto1.setPreco(new BigDecimal("10.00"));
        produto1.setRestaurante(restauranteAberto); 
        produto1.setEstoque(10);

        produto2 = new Produto();
        produto2.setId(101L);
        produto2.setNome("Produto 2");
        produto2.setDisponivel(true);
        produto2.setPreco(new BigDecimal("20.00"));
        produto2.setRestaurante(restauranteAberto); 
        produto2.setEstoque(5);

        ItemPedidoDTO itemDTO1 = new ItemPedidoDTO();
        itemDTO1.setProdutoId(100L);
        itemDTO1.setQuantidade(2); 

        ItemPedidoDTO itemDTO2 = new ItemPedidoDTO();
        itemDTO2.setProdutoId(101L);
        itemDTO2.setQuantidade(1); 
        
        pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(1L);
        pedidoDTO.setRestauranteId(10L);
        pedidoDTO.setItens(List.of(itemDTO1, itemDTO2));
        pedidoDTO.setEnderecoEntrega("Endereço de Teste, 123");
        pedidoDTO.setCep("12345-000");
        pedidoDTO.setFormaPagamento("PIX");

        calculoDTO = new CalculoPedidoDTO();
        calculoDTO.setRestauranteId(10L);
        calculoDTO.setItens(List.of(itemDTO1, itemDTO2));
        
        pedidoSalvo = new Pedido();
        pedidoSalvo.setId(1L);
        pedidoSalvo.setCliente(clienteAtivo);
        pedidoSalvo.setRestaurante(restauranteAberto);
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        pedidoSalvo.setValorTotal(new BigDecimal("45.00"));
        pedidoSalvo.setItens(new ArrayList<>()); 

        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setId(1L);
        pedidoResponseDTO.setClienteNome("Cliente Teste");
        pedidoResponseDTO.setRestauranteNome("Restaurante Teste");
        pedidoResponseDTO.setStatus(StatusPedido.PENDENTE.name());
        pedidoResponseDTO.setTotal(new BigDecimal("45.00"));

        // --- CORREÇÃO (Timers e SecurityUtils) ---
        
        // 1. Corrige o 'UnnecessaryStubbingException'
        lenient().when(metricsService.iniciarTimerPedido()).thenReturn(timerSample);
        lenient().when(metricsService.iniciarTimerBanco()).thenReturn(timerSample);
        
        // 2. Corrige o 'RuntimeException: Usuário não autenticado'
        // Mockamos a chamada ESTÁTICA do SecurityUtils
        mockedSecurityUtils = mockStatic(SecurityUtils.class);
        lenient().when(SecurityUtils.getCurrentUserId()).thenReturn(1L); // Retorna um ID de usuário mockado
    }

    // ==========================================================
    // ⬇️⬇️ CORREÇÃO (Limpar o Mock Estático) ⬇️⬇️
    // ==========================================================
    @AfterEach
    void tearDown() {
        // Precisamos fechar o mock estático após cada teste
        mockedSecurityUtils.close();
    }
    // ==========================================================


    // ==========================================================
    // Testes: criarPedido (NÃO MUDAM)
    // ==========================================================
    @Test
    @DisplayName("Deve criar pedido com sucesso e calcular totais corretos")
    void criarPedido_DeveSalvarComTotaisCorretos_QuandoValido() {
        // (Given)
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        when(modelMapper.map(pedidoSalvo, PedidoResponseDTO.class)).thenReturn(pedidoResponseDTO);

        // (When)
        PedidoResponseDTO response = pedidoService.criarPedido(pedidoDTO);

        // (Then)
        verify(pedidoRepository).save(pedidoCaptor.capture());
        Pedido pedidoCapturado = pedidoCaptor.getValue(); 
        assertEquals(0, new BigDecimal("40.00").compareTo(pedidoCapturado.getSubtotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(pedidoCapturado.getTaxaEntrega()));
        assertEquals(0, new BigDecimal("45.00").compareTo(pedidoCapturado.getValorTotal()));
        assertEquals(StatusPedido.PENDENTE, pedidoCapturado.getStatus());
        assertEquals(clienteAtivo, pedidoCapturado.getCliente());
        assertEquals(2, pedidoCapturado.getItens().size()); 
        assertNotNull(response);
        assertEquals(0, new BigDecimal("45.00").compareTo(response.getTotal()));
        assertEquals("Cliente Teste", response.getClienteNome());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se cliente estiver inativo")
    void criarPedido_DeveLancarExcecao_QuandoClienteInativo() {
        // (Given)
        clienteAtivo.setAtivo(false); 
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        
        // (When & Then)
        // A RuntimeException foi corrigida, então o teste agora 
        // deve (corretamente) esperar a BusinessException
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO),
                "Deveria lançar BusinessException para cliente inativo");
        
        assertEquals("Cliente inativo não pode fazer pedidos", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    // ... (Resto dos testes - não precisam mudar) ...
    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se produto estiver indisponível")
    void criarPedido_DeveLancarExcecao_QuandoProdutoIndisponivel() {
        produto1.setDisponivel(false); 
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo)); 
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto)); 
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1)); 
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));
        assertEquals("Produto indisponível: Produto 1", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se produto não pertence ao restaurante")
    void criarPedido_DeveLancarExcecao_QuandoProdutoNaoPertenceAoRestaurante() {
        Restaurante outroRestaurante = new Restaurante();
        outroRestaurante.setId(99L);
        produto1.setRestaurante(outroRestaurante); 
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));
        assertEquals("Produto não pertence ao restaurante selecionado", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar EntityNotFoundException se cliente não existe")
    void criarPedido_DeveLancarExcecao_QuandoClienteNaoExiste() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> pedidoService.criarPedido(pedidoDTO));
        assertEquals("Cliente não encontrado", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Deve calcular o total do pedido (Subtotal + Taxa)")
    void calcularTotalPedido_DeveRetornarCalculoCorreto() {
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        CalculoPedidoResponseDTO response = pedidoService.calcularTotalPedido(calculoDTO);
        assertNotNull(response);
        assertEquals(0, new BigDecimal("40.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(response.getTaxaEntrega()));
        assertEquals(0, new BigDecimal("45.00").compareTo(response.getTotal()));
    }
    
    @Test
    @DisplayName("Atualizar Status: Deve permitir transição válida (PENDENTE -> CONFIRMADO)")
    void atualizarStatusPedido_DeveAtualizarStatus_QuandoTransicaoValida() {
        pedidoSalvo.setStatus(StatusPedido.PENDENTE); 
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
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
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.atualizarStatusPedido(1L, StatusPedido.ENTREGUE));
        assertEquals("Transição de status inválida: PENDENTE -> ENTREGUE", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Cancelar Pedido: Deve permitir cancelar pedido PENDENTE")
    void cancelarPedido_DeveCancelar_QuandoStatusPendente() {
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
        pedidoService.cancelarPedido(1L);
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CANCELADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Cancelar Pedido: Deve lançar exceção se pedido já SAIU_PARA_ENTREGA")
    void cancelarPedido_DeveLancarExcecao_QuandoStatusInvalido() {
        pedidoSalvo.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.cancelarPedido(1L));
        assertEquals("Pedido não pode ser cancelado no status: SAIU_PARA_ENTREGA", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarPedidoPorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> pedidoService.buscarPedidoPorId(99L));
        assertEquals("Pedido não encontrado com ID: 99", ex.getMessage());
    }
}