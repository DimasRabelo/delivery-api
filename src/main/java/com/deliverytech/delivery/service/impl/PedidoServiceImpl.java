package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.dto.PedidoResponseDTO;
import com.deliverytech.delivery.dto.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.CalculoPedidoResponseDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) {
        // Validar cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        if (!cliente.isAtivo()) {
            throw new BusinessException("Cliente inativo não pode fazer pedidos");
        }

        // Validar restaurante
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
        if (!restaurante.getAtivo()) {
            throw new BusinessException("Restaurante não está disponível");
        }

        // Validar produtos e calcular subtotal
        List<ItemPedido> itensPedido = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedidoDTO itemDTO : dto.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

            if (!produto.getDisponivel()) {
                throw new BusinessException("Produto indisponível: " + produto.getNome());
            }

            if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                throw new BusinessException("Produto não pertence ao restaurante selecionado");
            }

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(itemDTO.getQuantidade())));

            itensPedido.add(item);
            subtotal = subtotal.add(item.getSubtotal());
        }

        // Calcular total do pedido
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
        BigDecimal valorTotal = subtotal.add(taxaEntrega);

        // Salvar pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setRestaurante(restaurante);
        pedido.setDataPedido(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setEnderecoEntrega(dto.getEnderecoEntrega());
        pedido.setSubtotal(subtotal);
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(valorTotal);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        // Salvar itens do pedido
        for (ItemPedido item : itensPedido) {
            item.setPedido(pedidoSalvo);
        }
        pedidoSalvo.setItens(itensPedido);

        // Mapear para DTO
        PedidoResponseDTO responseDTO = modelMapper.map(pedidoSalvo, PedidoResponseDTO.class);
        responseDTO.setClienteId(cliente.getId());
        responseDTO.setClienteNome(cliente.getNome());
        responseDTO.setRestauranteId(restaurante.getId());
        responseDTO.setRestauranteNome(restaurante.getNome());
        responseDTO.setTotal(valorTotal);
        responseDTO.setItens(itensPedido.stream()
                .map(item -> {
                    ItemPedidoDTO iDTO = new ItemPedidoDTO();
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    return iDTO;
                }).collect(Collectors.toList()));

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));

        PedidoResponseDTO responseDTO = modelMapper.map(pedido, PedidoResponseDTO.class);
        responseDTO.setClienteId(pedido.getCliente().getId());
        responseDTO.setClienteNome(pedido.getCliente().getNome());
        responseDTO.setRestauranteId(pedido.getRestaurante().getId());
        responseDTO.setRestauranteNome(pedido.getRestaurante().getNome());
        responseDTO.setTotal(pedido.getValorTotal());
        responseDTO.setItens(pedido.getItens().stream()
                .map(item -> {
                    ItemPedidoDTO iDTO = new ItemPedidoDTO();
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    return iDTO;
                }).collect(Collectors.toList()));

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
        return pedidos.stream().map(pedido -> {
            PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
            dto.setTotal(pedido.getValorTotal());
            dto.setItens(pedido.getItens().stream()
                    .map(item -> {
                        ItemPedidoDTO iDTO = new ItemPedidoDTO();
                        iDTO.setProdutoId(item.getProduto().getId());
                        iDTO.setQuantidade(item.getQuantidade());
                        return iDTO;
                    }).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

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

        PedidoResponseDTO responseDTO = modelMapper.map(pedidoAtualizado, PedidoResponseDTO.class);
        responseDTO.setClienteId(pedidoAtualizado.getCliente().getId());
        responseDTO.setClienteNome(pedidoAtualizado.getCliente().getNome());
        responseDTO.setRestauranteId(pedidoAtualizado.getRestaurante().getId());
        responseDTO.setRestauranteNome(pedidoAtualizado.getRestaurante().getNome());
        responseDTO.setTotal(pedidoAtualizado.getValorTotal());
        responseDTO.setItens(pedidoAtualizado.getItens().stream()
                .map(item -> {
                    ItemPedidoDTO iDTO = new ItemPedidoDTO();
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    return iDTO;
                }).collect(Collectors.toList()));

        return responseDTO;
    }

@Override
@Transactional(readOnly = true)
public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {
    BigDecimal subtotal = BigDecimal.ZERO;

    // Somar o valor dos itens
    for (ItemPedidoDTO item : dto.getItens()) {
        Produto produto = produtoRepository.findById(item.getProdutoId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + item.getProdutoId()));
        subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(item.getQuantidade())));
    }

    // Buscar taxa de entrega do restaurante
    Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
            .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));

    BigDecimal taxaEntrega = restaurante.getTaxaEntrega();
    BigDecimal total = subtotal.add(taxaEntrega);

    // Montar DTO de resposta
    CalculoPedidoResponseDTO response = new CalculoPedidoResponseDTO();
    response.setSubtotal(subtotal);
    response.setTaxaEntrega(taxaEntrega);
    response.setTotal(total);

    return response;
}

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
                return false;
        }
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }

    // =================== LISTAR PEDIDOS COM FILTROS ===================
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

        if (status != null && inicio != null && fim != null) {
            pedidos = pedidoRepository.findByStatusAndDataPedidoBetween(status, inicio, fim, pageable);
        } else if (status != null) {
            pedidos = pedidoRepository.findByStatus(status, pageable);
        } else if (inicio != null && fim != null) {
            pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim, pageable);
        } else {
            pedidos = pedidoRepository.findAll(pageable);
        }

        return pedidos.map(pedido -> {
            PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
            dto.setTotal(pedido.getValorTotal());
            dto.setItens(pedido.getItens().stream().map(item -> {
                ItemPedidoDTO iDTO = new ItemPedidoDTO();
                iDTO.setProdutoId(item.getProduto().getId());
                iDTO.setQuantidade(item.getQuantidade());
                return iDTO;
            }).toList());
            return dto;
        });
    }

    // =================== PEDIDOS POR RESTAURANTE ===================
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {
        List<Pedido> pedidos;

        if (status != null) {
            pedidos = pedidoRepository.findByRestauranteIdAndStatus(restauranteId, status, Pageable.unpaged()).getContent();
        } else {
            pedidos = pedidoRepository.findByRestauranteId(restauranteId, Pageable.unpaged()).getContent();
        }

        return pedidos.stream().map(pedido -> {
            PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
            dto.setTotal(pedido.getValorTotal());
            dto.setItens(pedido.getItens().stream().map(item -> {
                ItemPedidoDTO iDTO = new ItemPedidoDTO();
                iDTO.setProdutoId(item.getProduto().getId());
                iDTO.setQuantidade(item.getQuantidade());
                return iDTO;
            }).toList());
            return dto;
        }).toList();
    }
}
