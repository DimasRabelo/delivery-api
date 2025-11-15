package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.ItemPedidoDTO;
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.request.StatusPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.*;
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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes completos e revisados do PedidoService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PedidoService (Refatorado e Ajustado)")
class PedidoServiceTest {

    // --- Repositórios Mockados ---
    @Mock private PedidoRepository pedidoRepository;
    @Mock private RestauranteRepository restauranteRepository;
    @Mock private ProdutoRepository produtoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private ItemOpcionalRepository itemOpcionalRepository;
    @Mock private GrupoOpcionalRepository grupoOpcionalRepository;
    @Mock private ClienteRepository clienteRepository;

    // --- Serviços Auxiliares ---
    @Mock private MetricsService metricsService;
    @Mock private AuditService auditService;
    @Mock private ModelMapper modelMapper;
    @Mock private Timer.Sample timerSample;

    // --- Classe testada ---
    @InjectMocks
    private PedidoServiceImpl pedidoService;

    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    // --- Entidades e DTOs para teste ---
    private Usuario usuarioAtivo;
    private Cliente clienteAtivo;
    private Endereco enderecoAtivo;
    private Restaurante restauranteAberto;
    private Produto produto1;
    private ItemOpcional opcional1;
    private List<ItemOpcional> opcionalList;

    private PedidoDTO pedidoDTO;
    private CalculoPedidoDTO calculoDTO;
    private Pedido pedidoSalvo;
    private PedidoResponseDTO pedidoResponseDTO;

    @BeforeEach
    void setUp() {

        // --- Usuário ---
        usuarioAtivo = new Usuario();
        usuarioAtivo.setId(1L);
        usuarioAtivo.setEmail("teste@teste.com");
        usuarioAtivo.setRole(Role.CLIENTE);
        usuarioAtivo.setAtivo(true);

        clienteAtivo = new Cliente();
        clienteAtivo.setId(1L);
        clienteAtivo.setUsuario(usuarioAtivo);
        usuarioAtivo.setCliente(clienteAtivo);

        // --- Endereço ---
        enderecoAtivo = new Endereco();
        enderecoAtivo.setId(5L);
        enderecoAtivo.setUsuario(usuarioAtivo);

        // --- Restaurante ---
        restauranteAberto = new Restaurante();
        restauranteAberto.setId(10L);
        restauranteAberto.setAtivo(true);
        restauranteAberto.setTaxaEntrega(new BigDecimal("5.00"));

        // --- Produto ---
        produto1 = new Produto();
        produto1.setId(100L);
        produto1.setPrecoBase(new BigDecimal("10.00"));
        produto1.setDisponivel(true);
        produto1.setEstoque(10);
        produto1.setRestaurante(restauranteAberto);

        // --- Opcional ---
        GrupoOpcional grupo = new GrupoOpcional();
        grupo.setId(50L);
        grupo.setProduto(produto1);

        opcional1 = new ItemOpcional();
        opcional1.setId(1000L);
        opcional1.setPrecoAdicional(new BigDecimal("3.00"));
        opcional1.setGrupoOpcional(grupo);

        opcionalList = List.of(opcional1);

        // --- PedidoDTO ---
        ItemPedidoDTO itemDTO = new ItemPedidoDTO();
        itemDTO.setProdutoId(100L);
        itemDTO.setQuantidade(2);
        itemDTO.setOpcionaisIds(List.of(1000L));

        pedidoDTO = new PedidoDTO();
        pedidoDTO.setRestauranteId(10L);
        pedidoDTO.setItens(List.of(itemDTO));
        pedidoDTO.setEnderecoEntregaId(5L);
        pedidoDTO.setMetodoPagamento("PIX");

        // --- DTO cálculo ---
        calculoDTO = new CalculoPedidoDTO();
        calculoDTO.setRestauranteId(10L);
        calculoDTO.setItens(List.of(itemDTO));

        // --- Pedido salvo ---
        pedidoSalvo = new Pedido();
        pedidoSalvo.setId(1L);
        pedidoSalvo.setCliente(clienteAtivo);
        pedidoSalvo.setRestaurante(restauranteAberto);
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        pedidoSalvo.setItens(new ArrayList<>());

        pedidoResponseDTO = new PedidoResponseDTO();

        // --- Mocks padrões ---
        mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(1L);

        lenient().when(itemOpcionalRepository.findAllById(any())).thenReturn(opcionalList);
        lenient().when(grupoOpcionalRepository.findByProdutoId(100L)).thenReturn(new ArrayList<>());
        lenient().when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        lenient().when(metricsService.iniciarTimerPedido()).thenReturn(timerSample);
        lenient().when(metricsService.iniciarTimerBanco()).thenReturn(timerSample);

        // ⚠️ SOLUÇÃO DEFINITIVA — evita TODOS os UnnecessaryStubbingException
        lenient().when(modelMapper.map(any(), eq(PedidoResponseDTO.class)))
                .thenReturn(pedidoResponseDTO);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    // =====================================================================
    // TESTE: CRIAR PEDIDO
    // =====================================================================
    @Test
    @DisplayName("Criar pedido com sucesso e calcular totais corretamente")
    void criarPedido_DeveCriarComTotaisCorretos() {

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(enderecoRepository.findById(5L)).thenReturn(Optional.of(enderecoAtivo));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));

        // ⚠️ NÃO PRECISA MAIS MOCKAR modelMapper AQUI

        pedidoService.criarPedido(pedidoDTO);

        verify(pedidoRepository).save(pedidoCaptor.capture());
        Pedido pedido = pedidoCaptor.getValue();

        ItemPedido item = pedido.getItens().get(0);

        assertEquals(0, item.getPrecoUnitario().compareTo(new BigDecimal("13.00")));
        assertEquals(0, item.getSubtotal().compareTo(new BigDecimal("26.00")));
        assertEquals(0, pedido.getSubtotal().compareTo(new BigDecimal("26.00")));
        assertEquals(0, pedido.getValorTotal().compareTo(new BigDecimal("31.00")));
    }

    @Test
    @DisplayName("Criar pedido deve falhar se cliente estiver inativo")
    void criarPedido_DeveFalhar_ClienteInativo() {

        usuarioAtivo.setAtivo(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAtivo));

        assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));

        verify(pedidoRepository, never()).save(any());
    }

    // =====================================================================
    // TESTE: CÁLCULO DO PEDIDO
    // =====================================================================
    @Test
    @DisplayName("Deve calcular subtotal e total corretamente")
    void calcularTotalPedido_DeveCalcularCorretamente() {

        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));

        CalculoPedidoResponseDTO r = pedidoService.calcularTotalPedido(calculoDTO);

        assertEquals(0, r.getSubtotal().compareTo(new BigDecimal("26.00")));
        assertEquals(0, r.getTotal().compareTo(new BigDecimal("31.00")));
    }

    // =====================================================================
    // TESTE: ATUALIZAR STATUS
    // =====================================================================
    @Test
    @DisplayName("Atualizar status válido: PENDENTE -> CONFIRMADO")
    void atualizarStatus_Valido() {

        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        // ⚠️ NÃO MOCKAR modelMapper AQUI

        StatusPedidoDTO dto = new StatusPedidoDTO();
        dto.setStatus(StatusPedido.CONFIRMADO.name());

        pedidoService.atualizarStatusPedido(1L, dto);

        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CONFIRMADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Atualizar status inválido: PENDENTE -> ENTREGUE deve falhar")
    void atualizarStatus_Invalido() {

        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        StatusPedidoDTO dto = new StatusPedidoDTO();
        dto.setStatus(StatusPedido.ENTREGUE.name());

        assertThrows(BusinessException.class,
                () -> pedidoService.atualizarStatusPedido(1L, dto));
    }

    // =====================================================================
    // TESTE: CANCELAR PEDIDO
    // =====================================================================
    @Test
    @DisplayName("Cancelar pedido PENDENTE deve funcionar")
    void cancelarPedido_Valido() {

        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        pedidoService.cancelarPedido(1L);

        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CANCELADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Cancelar pedido deve falhar se já saiu para entrega")
    void cancelarPedido_Invalido() {

        pedidoSalvo.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        assertThrows(BusinessException.class,
                () -> pedidoService.cancelarPedido(1L));
    }

    // =====================================================================
    // TESTE: BUSCAR POR ID
    // =====================================================================
    @Test
    @DisplayName("Buscar por ID inexistente deve lançar exceção")
    void buscarPorId_Inexistente() {

        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> pedidoService.buscarPedidoPorId(99L));
    }
}
