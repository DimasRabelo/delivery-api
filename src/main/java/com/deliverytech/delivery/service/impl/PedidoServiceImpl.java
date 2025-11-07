package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.request.ItemPedidoDTO;
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;

// Imports de Entidades e Enums
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.enums.StatusPedido;

// Imports de Exceções
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;

// Imports de Repositórios
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;

// Imports de Serviços e Segurança
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.metrics.MetricsService;
import com.deliverytech.delivery.service.audit.AuditService;

// Imports do Spring e Java
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.Timer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set; 
import java.util.UUID;
import java.util.function.Function; 
import java.util.stream.Collectors;
import java.util.ArrayList; 
import java.util.HashSet; 


@Service("pedidoService")
public class PedidoServiceImpl implements PedidoService {

    // === INJEÇÃO DE DEPENDÊNCIAS ===
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private RestauranteRepository restauranteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private MetricsService metricsService;
    @Autowired private AuditService auditService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ItemOpcionalRepository itemOpcionalRepository;
    @Autowired private GrupoOpcionalRepository grupoOpcionalRepository; 


    // (Método criarPedido... OK)
    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) {
        Timer.Sample sample = metricsService.iniciarTimerPedido();
        metricsService.incrementarPedidosProcessados();
        Long usuarioId = SecurityUtils.getCurrentUserId();
        String usuarioIdLog = (usuarioId != null) ? usuarioId.toString() : "ANONIMO";
        try {
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_INICIO", "PedidoDTO", dto);
            if (usuarioId == null) {
                throw new BusinessException("Acesso negado. Usuário não autenticado.");
            }
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                throw new BusinessException("Este usuário não possui um perfil de cliente.");
            }
            if (!usuario.getAtivo()) {
                throw new BusinessException("Cliente inativo não pode fazer pedidos");
            }
            Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                    .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
            if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
                throw new BusinessException("Restaurante não está disponível");
            }
            Endereco endereco = enderecoRepository.findById(dto.getEnderecoEntregaId())
                    .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
            if (!endereco.getUsuario().getId().equals(usuarioId)) {
                throw new BusinessException("Endereço de entrega inválido. Pertence a outro usuário.");
            }
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setRestaurante(restaurante);
            pedido.setDataPedido(LocalDateTime.now());
            pedido.setStatus(StatusPedido.PENDENTE);
            pedido.setObservacoes(dto.getObservacoes());
            pedido.setEnderecoEntrega(endereco);
            pedido.setMetodoPagamento(dto.getMetodoPagamento());
            pedido.setTrocoPara(dto.getTrocoPara());
            pedido.setSubtotal(BigDecimal.ZERO);
            pedido.setTaxaEntrega(BigDecimal.ZERO);
            pedido.setValorTotal(BigDecimal.ZERO);
            BigDecimal subtotal = BigDecimal.ZERO;
            for (ItemPedidoDTO itemDTO : dto.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));
                if (produto.getDisponivel() == null || !produto.getDisponivel()) {
                    throw new BusinessException("Produto indisponível: " + produto.getNome());
                }
                if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                    throw new BusinessException("Produto " + produto.getNome() + " não pertence ao restaurante selecionado");
                }
                if (produto.getEstoque() < itemDTO.getQuantidade()) {
                    throw new BusinessException("Estoque insuficiente para o produto: " + produto.getNome());
                }
                BigDecimal precoUnitarioCalculado = produto.getPrecoBase();
                ItemPedido item = new ItemPedido();
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                item.setPedido(pedido);
                List<Long> opcionaisIdsEnviados = (itemDTO.getOpcionaisIds() != null) 
                    ? itemDTO.getOpcionaisIds() 
                    : new ArrayList<>(); 
                if (!opcionaisIdsEnviados.isEmpty()) {
                    Set<Long> uniqueIds = new HashSet<>(opcionaisIdsEnviados);
                    Map<Long, ItemOpcional> mapaOpcionais = itemOpcionalRepository.findAllById(uniqueIds).stream()
                            .collect(Collectors.toMap(ItemOpcional::getId, Function.identity()));
                    List<ItemOpcional> opcionaisSelecionados = new ArrayList<>();
                    for (Long id : opcionaisIdsEnviados) {
                        ItemOpcional opcional = mapaOpcionais.get(id);
                        if (opcional == null) {
                            throw new EntityNotFoundException("Opcional não encontrado: " + id);
                        }
                         if (opcional.getGrupoOpcional() == null ||
                             opcional.getGrupoOpcional().getProduto() == null ||
                             !opcional.getGrupoOpcional().getProduto().getId().equals(produto.getId())) {
                             throw new BusinessException("Opcional inválido: '" + opcional.getNome() +
                                     "' não pertence ao produto '" + produto.getNome() + "'");
                         }
                        opcionaisSelecionados.add(opcional);
                    }
                    Map<GrupoOpcional, Long> contagemPorGrupo = opcionaisSelecionados.stream()
                            .collect(Collectors.groupingBy(ItemOpcional::getGrupoOpcional, Collectors.counting()));
                    List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());
                    for (GrupoOpcional grupo : gruposDoProduto) {
                        long contagem = contagemPorGrupo.getOrDefault(grupo, 0L);
                        if (contagem < grupo.getMinSelecao()) {
                            throw new BusinessException(String.format(
                                "Seleção obrigatória para '%s'. Mínimo: %d, Enviado: %d",
                                grupo.getNome(), grupo.getMinSelecao(), contagem
                            ));
                        }
                        if (contagem > grupo.getMaxSelecao()) {
                            throw new BusinessException(String.format(
                                "Seleção máxima excedida para '%s'. Máximo: %d, Enviado: %d",
                                grupo.getNome(), grupo.getMaxSelecao(), contagem
                            ));
                        }
                    }
                    for (ItemOpcional opcional : opcionaisSelecionados) {
                        precoUnitarioCalculado = precoUnitarioCalculado.add(opcional.getPrecoAdicional());
                        ItemPedidoOpcional linkOpcional = new ItemPedidoOpcional(item, opcional);
                        item.getOpcionaisSelecionados().add(linkOpcional);
                    }
                } else {
                     List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());
                     for (GrupoOpcional grupo : gruposDoProduto) {
                        if (grupo.getMinSelecao() > 0) { 
                             throw new BusinessException(String.format(
                                "Seleção obrigatória para '%s'. Mínimo: %d, Enviado: 0",
                                grupo.getNome(), grupo.getMinSelecao()
                            ));
                        }
                     }
                }
                item.setPrecoUnitario(precoUnitarioCalculado);
                item.calcularSubtotal();
                pedido.getItens().add(item);
                subtotal = subtotal.add(item.getSubtotal());
                produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
                produtoRepository.save(produto);
            } 
            BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
            BigDecimal valorTotal = subtotal.add(taxaEntrega);
            pedido.setSubtotal(subtotal);
            pedido.setTaxaEntrega(taxaEntrega);
            pedido.setValorTotal(valorTotal);
            pedido.setNumeroPedido(UUID.randomUUID().toString().substring(0, 18));
            Pedido pedidoSalvo = pedidoRepository.save(pedido);
            metricsService.incrementarPedidosComSucesso();
            BigDecimal totalFinal = pedidoSalvo.getValorTotal() != null ? pedidoSalvo.getValorTotal() : BigDecimal.ZERO;
            metricsService.adicionarReceita(totalFinal.doubleValue());
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_SUCESSO", "Pedido", pedidoSalvo);
            return mapToPedidoResponseDTO(pedidoSalvo);
        } catch (Exception e) {
            metricsService.incrementarPedidosComErro();
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_FALHA", e.getClass().getSimpleName(), e.getMessage());
            throw e; 
        } finally { 
            metricsService.finalizarTimerPedido(sample);
        }
    }
    
    
    // (Método calcularTotalPedido... OK)
    @Override
    @Transactional(readOnly = true)
    public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedidoDTO item : dto.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + item.getProdutoId()));
            BigDecimal precoItem = produto.getPrecoBase();
            List<Long> opcionaisIdsEnviados = (item.getOpcionaisIds() != null) 
                    ? item.getOpcionaisIds() 
                    : new ArrayList<>();
            if (!opcionaisIdsEnviados.isEmpty()) {
                Set<Long> uniqueIds = new HashSet<>(opcionaisIdsEnviados);
                Map<Long, ItemOpcional> mapaOpcionais = itemOpcionalRepository.findAllById(uniqueIds).stream()
                        .collect(Collectors.toMap(ItemOpcional::getId, Function.identity()));
                List<ItemOpcional> opcionaisSelecionados = new ArrayList<>();
                for (Long id : opcionaisIdsEnviados) {
                    ItemOpcional opcional = mapaOpcionais.get(id);
                    if (opcional == null) {
                        throw new EntityNotFoundException("Opcional não encontrado: " + id);
                    }
                    if (opcional.getGrupoOpcional() == null || 
                        opcional.getGrupoOpcional().getProduto() == null ||
                        !opcional.getGrupoOpcional().getProduto().getId().equals(produto.getId())) {
                        throw new BusinessException("Opcional inválido: '" + opcional.getNome() + "'");
                    }
                    opcionaisSelecionados.add(opcional);
                }
                Map<GrupoOpcional, Long> contagemPorGrupo = opcionaisSelecionados.stream()
                        .collect(Collectors.groupingBy(ItemOpcional::getGrupoOpcional, Collectors.counting()));
                List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());
                for (GrupoOpcional grupo : gruposDoProduto) {
                    long contagem = contagemPorGrupo.getOrDefault(grupo, 0L);
                    if (contagem < grupo.getMinSelecao()) {
                        throw new BusinessException(String.format(
                            "Cálculo falhou: Seleção obrigatória para '%s'. Mínimo: %d, Enviado: %d",
                            grupo.getNome(), grupo.getMinSelecao(), contagem
                        ));
                    }
                    if (contagem > grupo.getMaxSelecao()) {
                         throw new BusinessException(String.format(
                            "Cálculo falhou: Seleção máxima excedida para '%s'. Máximo: %d, Enviado: %d",
                            grupo.getNome(), grupo.getMaxSelecao(), contagem
                        ));
                    }
                }
                for (ItemOpcional opcional : opcionaisSelecionados) {
                    precoItem = precoItem.add(opcional.getPrecoAdicional());
                }
            } else {
                 List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());
                 for (GrupoOpcional grupo : gruposDoProduto) {
                    if (grupo.getMinSelecao() > 0) {
                         throw new BusinessException(String.format(
                            "Cálculo falhou: Seleção obrigatória para '%s'. Mínimo: %d, Enviado: 0",
                            grupo.getNome(), grupo.getMinSelecao()
                        ));
                    }
                 }
            }
            subtotal = subtotal.add(precoItem.multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(taxaEntrega);
        CalculoPedidoResponseDTO response = new CalculoPedidoResponseDTO();
        response.setSubtotal(subtotal);
        response.setTaxaEntrega(taxaEntrega);
        response.setTotal(total);
        return response;
    }


    // (Método buscarPedidoPorId... OK)
    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id) {
        Timer.Sample sample = metricsService.iniciarTimerBanco();
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
            return mapToPedidoResponseDTO(pedido);
        } finally {
            metricsService.finalizarTimerBanco(sample);
        }
    }

    // (Método buscarPedidosPorCliente... OK)
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    // (Método atualizarStatusPedido... OK)
    @Override
    @Transactional
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        if (!isTransicaoValida(pedido.getStatus(), novoStatus)) {
            throw new BusinessException("Transição de status inválida: " +
                    pedido.getStatus() + " -> " + novoStatus);
        }
        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA) {
            if (pedido.getEntregador() == null) {
                Usuario entregador = encontrarEntregadorDisponivel();
                pedido.setEntregador(entregador);
            }
        }
        pedido.setStatus(novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        return mapToPedidoResponseDTO(pedidoAtualizado);
    }

    // (Método cancelarPedido... OK)
    @Override
    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        if (!podeSerCancelado(pedido.getStatus())) {
            throw new BusinessException("Pedido não pode ser cancelado no status: " + pedido.getStatus());
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    // (Método listarPedidos... OK)
    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Page<Pedido> pedidos;
        LocalDateTime inicio = null, fim = null;
        if (dataInicio != null && dataFim != null) {
            inicio = dataInicio.atStartOfDay();
            fim = dataFim.plusDays(1).atStartOfDay();
        }
        if (status != null && inicio != null) {
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

    // (Método listarMeusPedidos... OK)
    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarMeusPedidos(Pageable pageable) {
        Long usuarioIdLogado = SecurityUtils.getCurrentUserId();
        if (usuarioIdLogado == null) {
            throw new BusinessException("Acesso negado. Usuário não autenticado.");
        }
        Page<Pedido> paginaPedidos = pedidoRepository.findByClienteId(usuarioIdLogado, pageable);
        return paginaPedidos.map(this::mapToPedidoResponseDTO);
    }

    
    // (Método buscarPedidosPorRestaurante... OK)
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {
        List<Pedido> pedidos = pedidoRepository.findPedidosByRestauranteIdAndStatusComItens(restauranteId, status);
        if (!pedidos.isEmpty()) {
            List<ItemPedido> todosOsItens = pedidos.stream()
                                                  .flatMap(pedido -> pedido.getItens().stream())
                                                  .collect(Collectors.toList());
            if (!todosOsItens.isEmpty()) {
                pedidoRepository.fetchOpcionaisParaItens(todosOsItens);
            }
        }
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }


    // (Método canAccess... OK)
    @Override
    @Transactional(readOnly = true)
    public boolean canAccess(Long pedidoId) {
        try {
            Long usuarioLogadoId = SecurityUtils.getCurrentUserId();
            if (usuarioLogadoId == null) {
                return false; 
            }
            
            Long clienteId = null;
            Long restauranteLogadoId = null;
            Long entregadorId = null;

            if (SecurityUtils.isCliente()) {
                clienteId = usuarioLogadoId;
            } else if (SecurityUtils.isRestaurante()) {
                restauranteLogadoId = SecurityUtils.getCurrentRestauranteId();
            } else if (SecurityUtils.isEntregador()) {
                entregadorId = usuarioLogadoId;
            }
            
            return pedidoRepository.isPedidoOwnedBy(
                pedidoId, 
                clienteId, 
                restauranteLogadoId,
                entregadorId
            );
            
        } catch (Exception e) {
            return false;
        }
    }

    // --- Métodos Privados (Helper) ---

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
            case ENTREGUE:
                return false; 
            case CANCELADO:
                return false; 
            default:
                return false;
        }
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }

    // ==========================================================
    // --- MÉTODO mapToPedidoResponseDTO (A CORREÇÃO) ---
    // ==========================================================
    private PedidoResponseDTO mapToPedidoResponseDTO(Pedido pedido) {
        PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);
        
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome()); 
        }
        if (pedido.getRestaurante() != null) {
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
        }
        if (pedido.getEnderecoEntrega() != null) {
            Endereco end = pedido.getEnderecoEntrega();
            String enderecoFormatado = String.format("%s, %s - %s, %s/%s",
                    end.getRua(), end.getNumero(), end.getBairro(), end.getCidade(), end.getEstado());
            dto.setEnderecoEntrega(enderecoFormatado); 
        }
        
        // --- CORREÇÃO (Lógica que faltava) ---
        if (pedido.getEntregador() != null) {
            Usuario entregador = pedido.getEntregador();
            dto.setEntregadorId(entregador.getId());
            
            // O 'Usuario' do entregador não tem 'nome'. Usamos o email como fallback.
            // Se você criar uma entidade 'Entregador' (como 'Cliente') com um 'nome',
            // essa lógica deve ser atualizada.
            dto.setEntregadorNome(entregador.getEmail()); 
        }
        // --- FIM DA CORREÇÃO ---
        
        dto.setTotal(pedido.getValorTotal());
        
        dto.setItens(pedido.getItens().stream()
                .map(item -> {
                    ItemPedidoDTO iDTO = new ItemPedidoDTO(); 
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    
                    if (item.getOpcionaisSelecionados() != null) {
                         iDTO.setOpcionaisIds(item.getOpcionaisSelecionados().stream()
                            .map(opcionalLink -> opcionalLink.getItemOpcional().getId())
                            .collect(Collectors.toList()));
                    }
                    return iDTO;
                }).collect(Collectors.toList()));
        return dto;
    }
    // ==========================================================
    // FIM DA CORREÇÃO
    // ==========================================================
    
    private Usuario encontrarEntregadorDisponivel() {
        
        List<Usuario> entregadoresDisponiveis = usuarioRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ENTREGADOR && u.getAtivo())
                .filter(u -> {
                    boolean estaEmEntrega = pedidoRepository.existsByEntregadorAndStatus(
                            u, StatusPedido.SAIU_PARA_ENTREGA
                    );
                    return !estaEmEntrega; 
                })
                .collect(Collectors.toList());

        if (entregadoresDisponiveis.isEmpty()) {
            throw new BusinessException("Nenhum entregador disponível no momento.");
        }

        return entregadoresDisponiveis.get(0); 
    }
}