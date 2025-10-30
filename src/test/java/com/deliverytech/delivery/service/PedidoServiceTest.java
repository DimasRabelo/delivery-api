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

    // --- 1. Mocks (Atores/Dublês para as dependências) ---
    // Simula o comportamento das classes que o PedidoService depende.
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

    // --- 2. Classe sob Teste (SUT - System Under Test) ---
    @InjectMocks
    private PedidoServiceImpl pedidoService; // A classe real que estamos testando

    // --- 3. Captor ---
    /**
     * O ArgumentCaptor é uma ferramenta poderosa do Mockito.
     * Ele nos permite "capturar" o argumento que foi passado para um método mockado (como o 'save').
     * Usamos isso para verificar o *estado* do objeto 'Pedido' no momento exato em que ele foi salvo.
     */
    @Captor
    private ArgumentCaptor<Pedido> pedidoCaptor;

    // --- 4. Dados de Teste Padrão (Setup) ---
    // Objetos reutilizáveis que representam o "cenário feliz"
    private Cliente clienteAtivo;
    private Restaurante restauranteAberto;
    private Produto produto1;
    private Produto produto2;
    private PedidoDTO pedidoDTO; // DTO de entrada
    private Pedido pedidoSalvo; // Entidade que o 'save' retorna
    private CalculoPedidoDTO calculoDTO;
    private PedidoResponseDTO pedidoResponseDTO; // DTO de saída

    @BeforeEach
    void setUp() {
        // --- Cliente ---
        clienteAtivo = new Cliente();
        clienteAtivo.setId(1L);
        clienteAtivo.setNome("Cliente Teste");
        clienteAtivo.setAtivo(true); // Importante para as regras de negócio

        // --- Restaurante ---
        restauranteAberto = new Restaurante();
        restauranteAberto.setId(10L);
        restauranteAberto.setNome("Restaurante Teste");
        restauranteAberto.setAtivo(true); // Importante
        restauranteAberto.setTaxaEntrega(new BigDecimal("5.00"));

        // --- Produtos (ambos pertencem ao restaurante) ---
        produto1 = new Produto();
        produto1.setId(100L);
        produto1.setNome("Produto 1");
        produto1.setDisponivel(true); // Importante
        produto1.setPreco(new BigDecimal("10.00"));
        produto1.setRestaurante(restauranteAberto); // Importante
        produto1.setEstoque(10);

        produto2 = new Produto();
        produto2.setId(101L);
        produto2.setNome("Produto 2");
        produto2.setDisponivel(true);
        produto2.setPreco(new BigDecimal("20.00"));
        produto2.setRestaurante(restauranteAberto); // Importante
        produto2.setEstoque(5);

        // --- DTO de Itens (Payload da requisição) ---
        ItemPedidoDTO itemDTO1 = new ItemPedidoDTO();
        itemDTO1.setProdutoId(100L);
        itemDTO1.setQuantidade(2); // 2 x 10.00 = 20.00

        ItemPedidoDTO itemDTO2 = new ItemPedidoDTO();
        itemDTO2.setProdutoId(101L);
        itemDTO2.setQuantidade(1); // 1 x 20.00 = 20.00
        // Subtotal = 40.00 | Taxa Entrega = 5.00 | Total = 45.00

        // --- DTO do Pedido (Entrada) ---
        pedidoDTO = new PedidoDTO();
        pedidoDTO.setClienteId(1L);
        pedidoDTO.setRestauranteId(10L);
        pedidoDTO.setItens(List.of(itemDTO1, itemDTO2));
        pedidoDTO.setEnderecoEntrega("Endereço de Teste, 123");

        // --- DTO de Cálculo (para o método de calcular) ---
        calculoDTO = new CalculoPedidoDTO();
        calculoDTO.setRestauranteId(10L);
        calculoDTO.setItens(List.of(itemDTO1, itemDTO2));

        // --- Entidade Pedido (Resultado do 'save') ---
        // Esta é a entidade que simulamos ser retornada pelo banco
        pedidoSalvo = new Pedido();
        pedidoSalvo.setId(1L);
        pedidoSalvo.setCliente(clienteAtivo);
        pedidoSalvo.setRestaurante(restauranteAberto);
        pedidoSalvo.setStatus(StatusPedido.PENDENTE);
        pedidoSalvo.setValorTotal(new BigDecimal("45.00")); // Valor já calculado
        
        // ==========================================================
        // CORREÇÃO (para falha 'expected: <2> but was: <0>')
        // List.of() cria uma lista imutável. O service não consegue
        // adicionar itens (pedido.getItens().add(...)).
        // Trocamos por uma lista mutável (ArrayList).
        // (Ótima correção! Isso é um bug comum em testes.)
        // ==========================================================
        pedidoSalvo.setItens(new ArrayList<>()); 

        // --- DTO de Resposta (Saída) ---
        // Este é o objeto que o ModelMapper simula converter no final
        pedidoResponseDTO = new PedidoResponseDTO();
        pedidoResponseDTO.setId(1L);
        pedidoResponseDTO.setClienteNome("Cliente Teste");
        pedidoResponseDTO.setRestauranteNome("Restaurante Teste");
        pedidoResponseDTO.setStatus(StatusPedido.PENDENTE.name());
        pedidoResponseDTO.setTotal(new BigDecimal("45.00"));
    }

    // ==========================================================
    // Testes: criarPedido
    // ==========================================================

    @Test
    @DisplayName("Deve criar pedido com sucesso e calcular totais corretos")
    void criarPedido_DeveSalvarComTotaisCorretos_QuandoValido() {
        // -----------------
        // Given (Arrange) - Configuração do cenário
        // -----------------
        // 1. Simulamos as buscas no banco (validações iniciais)
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        // 2. Simulamos a busca de cada produto do DTO
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));
        when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2));
        
        // 3. Simulamos a ação de salvar. 'any(Pedido.class)' pois o objeto é criado
        //    dentro do método, então não temos a instância exata dele.
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);

        // 4. Simulamos a conversão final da Entidade (pedidoSalvo) para o DTO de Resposta
        when(modelMapper.map(pedidoSalvo, PedidoResponseDTO.class)).thenReturn(pedidoResponseDTO);

        // -----------------
        // When (Act) - Execução da ação
        // -----------------
        PedidoResponseDTO response = pedidoService.criarPedido(pedidoDTO);

        // -----------------
        // Then (Assert) - Verificação dos resultados
        // -----------------
        // 1. Verificamos se o 'save' foi chamado e capturamos o objeto Pedido
        verify(pedidoRepository).save(pedidoCaptor.capture());
        Pedido pedidoCapturado = pedidoCaptor.getValue(); // Pegamos o objeto que foi "salvo"

        // 2. Verificamos os cálculos (o ponto central do teste)
        // Usamos 'compareTo' para BigDecimals, que é mais seguro que 'equals'
        assertEquals(0, new BigDecimal("40.00").compareTo(pedidoCapturado.getSubtotal())); // (2*10) + (1*20)
        assertEquals(0, new BigDecimal("5.00").compareTo(pedidoCapturado.getTaxaEntrega()));
        assertEquals(0, new BigDecimal("45.00").compareTo(pedidoCapturado.getValorTotal()));

        // 3. Verificamos os dados do pedido capturado
        assertEquals(StatusPedido.PENDENTE, pedidoCapturado.getStatus());
        assertEquals(clienteAtivo, pedidoCapturado.getCliente());
        assertEquals(2, pedidoCapturado.getItens().size()); // Verifica se os itens foram adicionados

        // 4. Verificamos a resposta final do DTO
        assertNotNull(response);
        assertEquals(0, new BigDecimal("45.00").compareTo(response.getTotal()));
        assertEquals("Cliente Teste", response.getClienteNome());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se cliente estiver inativo")
    void criarPedido_DeveLancarExcecao_QuandoClienteInativo() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Preparamos o cenário de falha
        clienteAtivo.setAtivo(false); // Cliente inativo
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        // Não precisamos mockar os outros repositórios, pois o service deve falhar antes

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO),
                "Deveria lançar BusinessException para cliente inativo");

        // Verificamos a mensagem de erro
        assertEquals("Cliente inativo não pode fazer pedidos", ex.getMessage());
        
        // Verificação de segurança: Garantimos que NENHUM pedido foi salvo
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se produto estiver indisponível")
    void criarPedido_DeveLancarExcecao_QuandoProdutoIndisponivel() {
        // -----------------
        // Given (Arrange)
        // -----------------
        produto1.setDisponivel(false); // Produto 1 indisponível
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo)); // Cliente OK
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto)); // Restaurante OK
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1)); // Produto 1 falha

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));

        assertEquals("Produto indisponível: Produto 1", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Criar Pedido: Deve lançar BusinessException se produto não pertence ao restaurante")
    void criarPedido_DeveLancarExcecao_QuandoProdutoNaoPertenceAoRestaurante() {
        // -----------------
        // Given (Arrange)
        // -----------------
        Restaurante outroRestaurante = new Restaurante();
        outroRestaurante.setId(99L);
        produto1.setRestaurante(outroRestaurante); // Produto 1 pertence a outro restaurante

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteAtivo));
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto));
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1));

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        BusinessException ex = assertThrows(BusinessException.class,
                () -> pedidoService.criarPedido(pedidoDTO));

        assertEquals("Produto não pertence ao restaurante selecionado", ex.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Criar Pedido: Deve lançar EntityNotFoundException se cliente não existe")
    void criarPedido_DeveLancarExcecao_QuandoClienteNaoExiste() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Simulamos o "não encontrar" o cliente
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        // Verificamos o tipo da exceção
        assertThrows(EntityNotFoundException.class,
                () -> pedidoService.criarPedido(pedidoDTO));
        
        // Garantimos que o 'save' não foi chamado
        verify(pedidoRepository, never()).save(any());
    }

    // ==========================================================
    // Testes: calcularTotalPedido
    // ==========================================================

    @Test
    @DisplayName("Deve calcular o total do pedido (Subtotal + Taxa)")
    void calcularTotalPedido_DeveRetornarCalculoCorreto() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Simulamos as buscas necessárias para o cálculo
        when(produtoRepository.findById(100L)).thenReturn(Optional.of(produto1)); // 2 x 10.00
        when(produtoRepository.findById(101L)).thenReturn(Optional.of(produto2)); // 1 x 20.00
        when(restauranteRepository.findById(10L)).thenReturn(Optional.of(restauranteAberto)); // Taxa 5.00

        // -----------------
        // When (Act)
        // -----------------
        CalculoPedidoResponseDTO response = pedidoService.calcularTotalPedido(calculoDTO);

        // -----------------
        // Then (Assert)
        // -----------------
        assertNotNull(response);
        assertEquals(0, new BigDecimal("40.00").compareTo(response.getSubtotal()));
        assertEquals(0, new BigDecimal("5.00").compareTo(response.getTaxaEntrega()));
        assertEquals(0, new BigDecimal("45.00").compareTo(response.getTotal()));
    }

    // ==========================================================
    // Testes: atualizarStatusPedido
    // ==========================================================

    @Test
    @DisplayName("Atualizar Status: Deve permitir transição válida (PENDENTE -> CONFIRMADO)")
    void atualizarStatusPedido_DeveAtualizarStatus_QuandoTransicaoValida() {
        // -----------------
        // Given (Arrange)
        // -----------------
        pedidoSalvo.setStatus(StatusPedido.PENDENTE); // Estado inicial
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);
        
        // Simulamos o mapeamento de resposta (mesmo que o método possa ser void,
        // ele pode retornar o DTO atualizado)
        when(modelMapper.map(pedidoSalvo, PedidoResponseDTO.class)).thenReturn(pedidoResponseDTO);

        // -----------------
        // When (Act)
        // -----------------
        // Tentamos a transição PENDENTE -> CONFIRMADO
        pedidoService.atualizarStatusPedido(1L, StatusPedido.CONFIRMADO);

        // -----------------
        // Then (Assert)
        // -----------------
        // Capturamos o pedido salvo e verificamos se o status foi alterado
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CONFIRMADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Atualizar Status: Deve lançar exceção em transição inválida (PENDENTE -> ENTREGUE)")
    void atualizarStatusPedido_DeveLancarExcecao_QuandoTransicaoInvalida() {
        // -----------------
        // Given (Arrange)
        // -----------------
        pedidoSalvo.setStatus(StatusPedido.PENDENTE); // Estado inicial
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));
        // Não mockamos o 'save' pois ele não deve ser chamado

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        // Tentamos a transição PENDENTE -> ENTREGUE (inválida)
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
        // -----------------
        // Given (Arrange)
        // -----------------
        pedidoSalvo.setStatus(StatusPedido.PENDENTE); // Estado inicial
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        // -----------------
        // When (Act)
        // -----------------
        pedidoService.cancelarPedido(1L);

        // -----------------
        // Then (Assert)
        // -----------------
        // Verificamos se o pedido foi salvo com o novo status CANCELADO
        verify(pedidoRepository).save(pedidoCaptor.capture());
        assertEquals(StatusPedido.CANCELADO, pedidoCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Cancelar Pedido: Deve lançar exceção se pedido já SAIU_PARA_ENTREGA")
    void cancelarPedido_DeveLancarExcecao_QuandoStatusInvalido() {
        // -----------------
        // Given (Arrange)
        // -----------------
        // Estado em que o cancelamento não é permitido
        pedidoSalvo.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoSalvo));

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
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
        // -----------------
        // Given (Arrange)
        // -----------------
        // Simulamos o retorno de um Optional vazio
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        // -----------------
        // When & Then (Act & Assert)
        // -----------------
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> pedidoService.buscarPedidoPorId(99L));
        
        assertEquals("Pedido não encontrado com ID: 99", ex.getMessage());
    }
}