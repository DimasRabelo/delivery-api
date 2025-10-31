package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.service.PedidoService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.deliverytech.delivery.security.jwt.SecurityUtils; 

/**
 * Implementação dos serviços de lógica de negócio para Pedidos.
 * Gerencia a criação, consulta, atualização de status e cancelamento de pedidos.
 */
@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Cria um novo pedido no sistema.
     * Este método realiza uma série de validações:
     * 1. Verifica se o Cliente existe e está ativo.
     * 2. Verifica se o Restaurante existe e está ativo.
     * 3. Itera sobre cada item, verificando se o Produto existe, está disponível,
     * pertence ao restaurante correto e se há estoque suficiente.
     * 4. Decrementa o estoque dos produtos.
     * 5. Calcula o subtotal, taxa de entrega e o valor total.
     * 6. Salva o Pedido com status 'PENDENTE'.
     *
     * @param dto O PedidoDTO contendo os dados do cliente, restaurante e itens.
     * @return O PedidoResponseDTO com os dados do pedido criado.
     * @throws EntityNotFoundException Se cliente, restaurante ou um produto não for encontrado.
     * @throws BusinessException Se cliente/restaurante estiver inativo, produto indisponível,
     * produto de outro restaurante, ou estoque insuficiente.
     */
    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) {
        // 1️⃣ Validar cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        if (!cliente.isAtivo()) {
            throw new BusinessException("Cliente inativo não pode fazer pedidos");
        }

        // 2️⃣ Validar restaurante
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        if (!restaurante.getAtivo()) {
            throw new BusinessException("Restaurante não está disponível");
        }

        // 3️⃣ Criar o Pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());

        BigDecimal subtotal = BigDecimal.ZERO;

        // 4️⃣ Validar produtos e montar itens
        for (ItemPedidoDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

            if (!produto.getDisponivel()) {
                throw new BusinessException("Produto indisponível: " + produto.getNome());
            }

            if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                throw new BusinessException("Produto não pertence ao restaurante selecionado");
            }
            if (produto.getEstoque() < itemDTO.getQuantidade()) {
                throw new BusinessException("Estoque insuficiente para o produto: " + produto.getNome());
            }

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));
            item.setPedido(pedido);
            pedido.getItens().add(item);

            subtotal = subtotal.add(item.getSubtotal());

            // Atualiza estoque do produto
            produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);
        }

        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal valorTotal = subtotal.add(taxaEntrega);

        pedido.setSubtotal(subtotal);
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(valorTotal);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // 5️⃣ Mapear e retornar a resposta
        return mapToPedidoResponseDTO(pedidoSalvo);
    }

    /**
     * Busca um pedido específico pelo seu ID.
     *
     * @param id O ID do pedido a ser buscado.
     * @return O PedidoResponseDTO com os dados do pedido.
     * @throws EntityNotFoundException Se o pedido não for encontrado.
     */
    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));

        return mapToPedidoResponseDTO(pedido);
    }

    /**
     * Busca o histórico de pedidos de um cliente específico (sem paginação).
     * Utilizado pelo endpoint GET /api/pedidos/cliente/{clienteId}.
     *
     * @param clienteId O ID do cliente cujos pedidos serão buscados.
     * @return Uma Lista (List) de PedidoResponseDTO.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o status de um pedido.
     * A transição de status é validada pelo método privado {@link #isTransicaoValida}.
     *
     * @param id O ID do pedido a ser atualizado.
     * @param novoStatus O novo StatusPedido (ex: CONFIRMADO, PREPARANDO).
     * @return O PedidoResponseDTO com o status atualizado.
     * @throws EntityNotFoundException Se o pedido não for encontrado.
     * @throws BusinessException Se a transição de status for inválida.
     */
    @Override
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        if (!isTransicaoValida(pedido.getStatus(), novoStatus)) {
            throw new BusinessException("Transição de status inválida: " +
                    pedido.getStatus() + " -> " + novoStatus);
        }

        pedido.setStatus(novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        return mapToPedidoResponseDTO(pedidoAtualizado);
    }

    /**
     * Calcula o valor total de um pedido (subtotal + taxa de entrega) sem
     * persistir a entidade no banco de dados.
     *
     * @param dto DTO com os itens e o ID do restaurante.
     * @return Um CalculoPedidoResponseDTO contendo subtotal, taxaEntrega e total.
     * @throws EntityNotFoundException Se um produto ou o restaurante não for encontrado.
     */
    @Override
    public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedidoDTO item : dto.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Produto não encontrado: " + item.getProdutoId()));
            subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal total = subtotal.add(taxaEntrega);

        CalculoPedidoResponseDTO response = new CalculoPedidoResponseDTO();
        response.setSubtotal(subtotal);
        response.setTaxaEntrega(taxaEntrega);
        response.setTotal(total);

        return response;
    }

    /**
     * Cancela um pedido, alterando seu status para CANCELADO.
     * Apenas pedidos em status PENDENTE ou CONFIRMADO podem ser cancelados,
     * conforme validado pelo método {@link #podeSerCancelado}.
     *
     * @param id O ID do pedido a ser cancelado.
     * @throws EntityNotFoundException Se o pedido não for encontrado.
     * @throws BusinessException Se o pedido não puder mais ser cancelado.
     */
    @Override
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        if (!podeSerCancelado(pedido.getStatus())) {
            throw new BusinessException("Pedido não pode ser cancelado no status: " + pedido.getStatus());
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    /**
     * Lista **todos** os pedidos do sistema (de forma paginada) para o **Admin**.
     * Permite filtros dinâmicos por status, data de início e data de fim.
     * Utilizado pelo endpoint GET /api/pedidos.
     *
     * @param status (Opcional) Filtra pedidos pelo status.
     * @param dataInicio (Opcional) Data inicial do período.
     * @param dataFim (Opcional) Data final do período.
     * @param pageable Objeto contendo as informações de paginação.
     * @return Uma Página (Page) de PedidoResponseDTO.
     */
     @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Page<Pedido> pedidos;
        LocalDateTime inicio = null;
        LocalDateTime fim = null;

        if (dataInicio != null && dataFim != null) {
            inicio = dataInicio.atStartOfDay();
            fim = dataFim.plusDays(1).atStartOfDay();
        }

        // Lógica de filtragem dinâmica
        if (status != null && inicio != null) { // Nota: o 'fim' está incluído na var 'inicio'
            pedidos = pedidoRepository.findByStatusAndDataPedidoBetween(status, inicio, fim, pageable);
        } else if (status != null) {
            pedidos = pedidoRepository.findByStatus(status, pageable);
        } else if (inicio != null) {
            pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim, pageable);
        } else {
            pedidos = pedidoRepository.findAll(pageable);
        }

        return pedidos.map(this::mapToPedidoResponseDTO);
    }

    /**
     * Lista os pedidos (de forma paginada) do **cliente autenticado**.
     * Utilizado pelo endpoint GET /api/pedidos/meus.
     *
     * @param pageable Objeto contendo as informações de paginação.
     * @return Uma Página (Page) de PedidoResponseDTO.
     * @throws BusinessException Se o usuário não estiver autenticado.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarMeusPedidos(Pageable pageable) {

        // 1. Pega o ID do CLIENTE logado.
        Long clienteIdLogado = SecurityUtils.getCurrentUserId(); 

        // 2. Valida se o ID não é nulo
        if (clienteIdLogado == null) {
            throw new BusinessException("Acesso negado. Usuário não autenticado.");
        }

        // 3. Busca os pedidos paginados no repositório usando o ID do cliente.
        Page<Pedido> paginaPedidos = pedidoRepository.findByClienteId(clienteIdLogado, pageable);

        // 4. Mapeia a Page<Pedido> para Page<PedidoResponseDTO>
        return paginaPedidos.map(this::mapToPedidoResponseDTO);
    }

    /**
     * Busca o histórico de pedidos de um restaurante específico (sem paginação).
     * Permite filtro opcional por status.
     * Utilizado pelo endpoint GET /api/pedidos/restaurante/{restauranteId}.
     *
     * @param restauranteId O ID do restaurante.
     * @param status (Opcional) Filtra pedidos pelo status.
     * @return Uma Lista (List) de PedidoResponseDTO.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {
        List<Pedido> pedidos;

        if (status != null) {
            pedidos = pedidoRepository.findByRestauranteIdAndStatus(restauranteId, status, Pageable.unpaged()).getContent();
        } else {
            // .getContent() é usado para extrair a Lista da Página "não paginada"
            pedidos = pedidoRepository.findByRestauranteId(restauranteId, Pageable.unpaged()).getContent();
        }

        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    // ===========================
    // MÉTODOS AUXILIARES (PRIVADOS)
    // ===========================

    /**
     * Método auxiliar privado que define a "máquina de estados" do pedido.
     * Valida se a mudança de um status para outro é permitida.
     *
     * @param statusAtual O status atual do pedido.
     * @param novoStatus O status para o qual se deseja mudar.
     * @return true se a transição for válida, false caso contrário.
     */
    private boolean isTransicaoValida(StatusPedido statusAtual, StatusPedido novoStatus) {
        switch (statusAtual) {
            case PENDENTE:
                return novoStatus == StatusPedido.CONFIRMADO || novoStatus == StatusPedido.CANCELADO;
            case CONFIRMADO:
                return novoStatus == StatusPedido.PREPARANDO || novoStatus == StatusPedido.CANCELADO;
            case PREPARANDO:
                return novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA:
                return novoStatus == StatusPedido.ENTREGUE;
            default:
                // Nenhum status pode ser alterado após ENTREGUE ou CANCELADO
                return false;
        }
    }

    /**
     * Método auxiliar privado que verifica se um pedido ainda pode ser cancelado.
     *
     * @param status O status atual do pedido.
     * @return true se o status for PENDENTE ou CONFIRMADO.
     */
    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }
    
    /**
     * Método auxiliar privado para centralizar o mapeamento de Pedido -> PedidoResponseDTO.
     * Evita repetição de código nos métodos de consulta.
     * * @param pedido A entidade Pedido a ser mapeada.
     * @return O DTO PedidoResponseDTO preenchido.
     */
    private PedidoResponseDTO mapToPedidoResponseDTO(Pedido pedido) {
        PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);
        
        // Mapeia campos complexos que o modelMapper pode se perder
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
        }
        
        if (pedido.getRestaurante() != null) {
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
        }
        
        dto.setTotal(pedido.getValorTotal());

        // Mapeia a lista de itens
        dto.setItens(pedido.getItens().stream()
                .map(item -> {
                    ItemPedidoDTO iDTO = new ItemPedidoDTO();
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    // Você pode adicionar mais campos aqui se necessário (ex: nome do produto, subtotal do item)
                    return iDTO;
                }).collect(Collectors.toList()));
                
        return dto;
    }
}