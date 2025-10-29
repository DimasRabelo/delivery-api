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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    // 1. Mocks (Atores/Dublês para as dependências)
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private RestauranteRepository restauranteRepository;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ModelMapper modelMapper;

    // 2. Classe sob Teste
    @InjectMocks
    private PedidoServiceImpl pedidoService;

    // 3. Captor (Para verificar o que é passado para o 'save')
    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;

    // 4. Dados de Teste Padrão
    private Cliente clienteAtivo;
    private Restaurante restauranteAberto;
    private Produto produto1;
    private Produto produto2;
    private PedidoDTO pedidoDTO;
    private Pedido pedidoSalvo;
    private CalculoPedidoDTO calculoDTO;

    // ==============================================
    // CORREÇÃO: Adicionar variável de PedidoResponseDTO
    // ==============================================
    private PedidoResponseDTO pedidoResponseDTO;

    @BeforeEach
    void setUp() {
        // Cliente
        clienteAtivo = new Cliente();
        clienteAtivo.setId(1L);
        clienteAtivo.setNome("Cliente Teste");
        clienteAtivo.setAtivo(true);

        // Restaurante
        restauranteAberto = new Restaurante();
        restauranteAberto.setId(10L);
        restauranteAberto.setNome("Restaurante Teste");
        restauranteAberto.setAtivo(true);
        restauranteAberto.setTaxaEntrega(new BigDecimal("5.00"));

        // Produtos (ambos pertencem ao restaurante)
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

        // DTO de Itens
        ItemPedidoDTO itemDTO1 = new ItemPedidoDTO();
        itemDTO1.setProdutoId(100L);
        itemDTO1.setQuantidade(2); // 2 x 10.00 = 20.00

        ItemPedidoDTO itemDTO2 = new ItemPedidoDTO();
        itemDTO2.setProdutoId(101L);
        itemDTO2.setQuantidade(1); // 1 x 20.00 = 20.00
        // Subtotal = 40.00 | Taxa Entrega = 5.00 | Total = 45.00

        // DTO do Pedido (Entrada)
        pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(1L);
        pedidoDTO.setRestauranteId(10L);
        pedidoDTO.setItens(List.of(itemDTO1, itemDTO2));
        pedidoDTO.setEnderecoEntrega("Endereço de Teste, 123");

        // DTO de Cálculo
        calculoDTO = new CalculoPedidoDTO();
        calculoDTO.setRestauranteId(10L);
        calculoDTO.setItens(List.of(itemDTO1, itemDTO2));

        // Entidade Pedido (Resultado do 'save')
        pedidoSalvo = new Pedido();
        pedidoSalvo.setId(1L);
        pedidoSalvo.setCliente(clienteAtivo);
        pedidoSalvo.setRestaurante(restauranteAberto);
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        pedidoSalvo.setValorTotal(new BigDecimal("45.00"));
        
        // ==========================================================
        // CORREÇÃO (para falha 'expected: <2> but was: <0>')
        // List.of() cria uma lista imutável. O service não consegue
        // adicionar itens (pedido.getItens().add(...)).
        // Trocamos por uma lista mutável (ArrayList).
        // ==========================================================
        pedidoSalvo.setItens(new ArrayList<>()); 

        // ==========================================================
        // CORREÇÃO: Inicializar o DTO de resposta
        // ==========================================================
        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setId(1L);
        pedidoResponseDTO.setClienteNome("Cliente Teste");
        pedidoResponseDTO.setRestauranteNome("Restaurante Teste");
        pedidoResponseDTO.setStatus(StatusPedido.PENDENTE.name());
        pedidoResponseDTO.setTotal(new BigDecimal("45.00"));
        // Adicione outros campos se o seu PedidoResponseDTO tiver
    }

    // ==========================================================
    // Testes: criarPedido
    // ==========================================================

    @Test
    @DisplayName("Deve criar pedido com sucesso e calcular totais corretos")
    void criarPedido_DeveSalvarComTotaisCorretos_QuandoValido() {
        // Given (Simula as respostas dos repositórios)
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
        
        // Assumindo que seu service NÃO usa ModelMapper para DTO -> Entidade,
        // mas sim 'new Pedido()' e preenche manualmente.
        // Se ele USAR o ModelMapper, descomente a linha abaixo:
        // when(modelMapper.map(pedidoDTO, Pedido.class)).thenReturn(pedidoSalvo);

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);

        // ==========================================================
        // CORREÇÃO: Usar a variável 'pedidoResponseDTO' correta no mock
        // ==========================================================
        when(modelMapper.map(pedidoSalvo, PedidoResponseDTO.class)).thenReturn(pedidoResponseDTO);

        // When
        PedidoResponseDTO response = pedidoService.criarPedido(pedidoDTO);

        // Then
        // 1. Verifica se o 'save' foi chamado
        verify(pedidoRepository).save(pedidoCaptor.capture());
        Pedido pedidoCapturado = pedidoCaptor.getValue();

        // 2. Verifica os cálculos (o ponto central da Atividade 1.3)
        assertEquals(new BigDecimal("40.00"), pedidoCapturado.getSubtotal()); // (2*10) + (1*20)
        assertEquals(new BigDecimal("5.00"), pedidoCapturado.getTaxaEntrega());
        assertEquals(new BigDecimal("45.00"), pedidoCapturado.getValorTotal());

        // 3. Verifica os dados do pedido
        assertEquals(StatusPedido.PENDENTE, pedidoCapturado.getStatus());
        assertEquals(clienteAtivo, pedidoCapturado.getCliente());
        // Esta asserção agora deve passar graças à correção no setUp()
        assertEquals(2, pedidoCapturado.getItens().size()); 

        // 4. Verifica a resposta final do DTO (que agora é mockada corretamente)
        assertNotNull(response);
        assertEquals(new BigDecimal("45.00"), response.getTotal());
        assertEquals("Cliente Teste", response.getClienteNome());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se cliente estiver inativo")
    void criarPedido_DeveLancarExcecao_QuandoClienteInativo() {
        // Given
        clienteAtivo.setAtivo(false); // Cliente inativo
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));

        assertEquals("Cliente inativo não pode fazer pedidos", ex.getMessage());
        verify(pedidoRepository, never()).save(any()); // Garante que o rollback (nunca salvar) ocorreu
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se produto estiver indisponível")
    void criarPedido_DeveLancarExcecao_QuandoProdutoIndisponivel() {
        // Given (Produto 1 indisponível)
        produto1.setDisponivel(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));

        assertEquals("Produto indisponível: Produto 1", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se produto não pertence ao restaurante")
    void criarPedido_DeveLancarExcecao_QuandoProdutoNaoPertenceAoRestaurante() {
        // Given
        Restaurante outroRestaurante = new Restaurante();
        outroRestaurante.setId(99L);
        produto1.setRestaurante(outroRestaurante); // Produto pertence a outro restaurante

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));

        assertEquals("Produto não pertence ao restaurante selecionado", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar EntityNotFoundException se cliente não existe")
    void criarPedido_DeveLancarExcecao_QuandoClienteNaoExiste() {
        // Given
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> pedidoService.criarPedido(pedidoDTO));
        verify(pedidoRepository, never()).save(any());
    }

    // ==========================================================
    // Testes: calcularTotalPedido
    // ==========================================================

    @Test
    @DisplayName("Deve calcular o total do pedido (Subtotal + Taxa)")
    void calcularTotalPedido_DeveRetornarCalculoCorreto() {
        // Given
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));

        // When
        CalculoPedidoResponseDTO response = pedidoService.calcularTotalPedido(calculoDTO);

        // Then
        assertNotNull(response);
        assertEquals(new BigDecimal("40.00"), response.getSubtotal());
        assertEquals(new BigDecimal("5.00"), response.getTaxaEntrega());
        assertEquals(new BigDecimal("45.00"), response.getTotal());
    }

    // ==========================================================
    // Testes: atualizarStatusPedido
    // ==========================================================

    @Test
    @DisplayName("Atualizar Status: Deve permitir transição válida (PENDENTE -> CONFIRMADO)")
    void atualizarStatusPedido_DeveAtualizarStatus_QuandoTransicaoValida() {
        // Given
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        
        // CORREÇÃO: Usar a variável correta
        when(modelMapper.map(pedidoSalvo, PedidoResponseDTO.class)).thenReturn(pedidoResponseDTO);

        // When
        pedidoService.atualizarStatusPedido(1L, StatusPedido.CONFIRMADO);

        // Then
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CONFIRMADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Atualizar Status: Deve lançar exceção em transição inválida (PENDENTE -> ENTREGUE)")
    void atualizarStatusPedido_DeveLancarExcecao_QuandoTransicaoInvalida() {
        // Given
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.atualizarStatusPedido(1L, StatusPedido.ENTREGUE));

        assertEquals("Transição de status inválida: PENDENTE -> ENTREGUE", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    // ==========================================================
    // Testes: cancelarPedido
    // ==========================================================

    @Test
    @DisplayName("Cancelar Pedido: Deve permitir cancelar pedido PENDENTE")
    void cancelarPedido_DeveCancelar_QuandoStatusPendente() {
        // Given
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        // When
        pedidoService.cancelarPedido(1L);

        // Then
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CANCELADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Cancelar Pedido: Deve lançar exceção se pedido já SAIU_PARA_ENTREGA")
    void cancelarPedido_DeveLancarExcecao_QuandoStatusInvalido() {
        // Given
        pedidoSalvo.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        // When & Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.cancelarPedido(1L));
        
        assertEquals("Pedido não pode ser cancelado no status: SAIU_PARA_ENTREGA", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    // ==========================================================
    // Testes: buscarPedidoPorId
    // ==========================================================
    
    @Test
    @DisplayName("Buscar por ID: Deve lançar exceção quando ID não existe")
    void buscarPedidoPorId_DeveLancarExcecao_QuandoIdNaoExiste() {
        // Given
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> pedidoService.buscarPedidoPorId(99L));
        
        assertEquals("Pedido não encontrado com ID: 99", ex.getMessage());
    }
}