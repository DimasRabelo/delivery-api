package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.request.ItemPedidoDTO;
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.request.StatusPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.ItemPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.service.audit.AuditService;
import com.deliverytech.delivery.service.metrics.MetricsService;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
// IMPORT NECESSÁRIO PARA A LISTA DE STATUS
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors; 

@Service("pedidoService")
public class PedidoServiceImpl implements PedidoService {

    // === INJEÇÃO DE DEPENDÊNCIAS ===
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private RestauranteRepository restauranteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private MetricsService metricsService;
    @Autowired private AuditService auditService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ItemOpcionalRepository itemOpcionalRepository;
    @Autowired private GrupoOpcionalRepository grupoOpcionalRepository;


    /**
     * Processa e salva um novo pedido no sistema.
     */
    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) {
        Timer.Sample sample = metricsService.iniciarTimerPedido();
        metricsService.incrementarPedidosProcessados();

        Long usuarioId = SecurityUtils.getCurrentUserId();
        String usuarioIdLog = (usuarioId != null) ? usuarioId.toString() : "ANONIMO";

        try {
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_INICIO", "PedidoDTO", dto);

            // 1. Validação do Usuário e Cliente
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

            // 2. Validação do Restaurante
            Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                    .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
            if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
                throw new BusinessException("Restaurante não está disponível");
            }

            // 3. Validação do Endereço
            Endereco endereco = enderecoRepository.findById(dto.getEnderecoEntregaId())
                    .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
            if (!endereco.getUsuario().getId().equals(usuarioId)) {
                throw new BusinessException("Endereço de entrega inválido. Pertence a outro usuário.");
            }

            // 4. Inicialização do Pedido
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setRestaurante(restaurante);
            // CORREÇÃO DA DATA: Usando dataPedido
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

            // 5. Processamento dos Itens
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
                item.setObservacoes(itemDTO.getObservacoes()); 

                List<Long> opcionaisIdsEnviados = (itemDTO.getOpcionaisIds() != null) ? itemDTO.getOpcionaisIds() : new ArrayList<>();

                // 5a. Processamento de Opcionais
                if (!opcionaisIdsEnviados.isEmpty()) {
                    Set<Long> uniqueIds = new HashSet<>(opcionaisIdsEnviados);
                    Map<Long, ItemOpcional> mapaOpcionais = itemOpcionalRepository.findAllById(uniqueIds).stream()
                            .collect(Collectors.toMap(ItemOpcional::getId, Function.identity()));

                    List<ItemOpcional> opcionaisSelecionados = new ArrayList<>();
                    for (Long id : opcionaisIdsEnviados) {
                        ItemOpcional opcional = mapaOpcionais.get(id);
                        if (opcional == null) throw new EntityNotFoundException("Opcional não encontrado: " + id);
                        
                        if (opcional.getGrupoOpcional() == null ||
                            opcional.getGrupoOpcional().getProduto() == null ||
                            !opcional.getGrupoOpcional().getProduto().getId().equals(produto.getId())) {
                            throw new BusinessException("Opcional inválido para este produto.");
                        }
                        opcionaisSelecionados.add(opcional);
                    }

                    // Validação de Grupos
                    Map<GrupoOpcional, Long> contagemPorGrupo = opcionaisSelecionados.stream()
                            .collect(Collectors.groupingBy(ItemOpcional::getGrupoOpcional, Collectors.counting()));
                    
                    List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());

                    for (GrupoOpcional grupo : gruposDoProduto) {
                        long contagem = contagemPorGrupo.getOrDefault(grupo, 0L);
                        if (contagem < grupo.getMinSelecao()) {
                            throw new BusinessException("Mínimo não atingido para: " + grupo.getNome());
                        }
                        if (contagem > grupo.getMaxSelecao()) {
                            throw new BusinessException("Máximo excedido para: " + grupo.getNome());
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
                             throw new BusinessException("Seleção obrigatória faltando para: " + grupo.getNome());
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

            // 6. Cálculo Final
            BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
            BigDecimal valorTotal = subtotal.add(taxaEntrega);

            pedido.setSubtotal(subtotal);
            pedido.setTaxaEntrega(taxaEntrega);
            pedido.setValorTotal(valorTotal);
            pedido.setNumeroPedido(UUID.randomUUID().toString().substring(0, 18));

            Pedido pedidoSalvo = pedidoRepository.save(pedido);

            // 7. Métricas
            metricsService.incrementarPedidosComSucesso();
            metricsService.adicionarReceita(valorTotal.doubleValue());
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

@Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPendentesEntregador() {
        // 1. Pega o ID do usuário logado (assumindo que é o entregador)
        Long entregadorId = SecurityUtils.getCurrentUserId();
        
        if (entregadorId == null) {
            throw new BusinessException("Usuário não autenticado.");
        }

        // 2. Busca no banco apenas os pedidos que estão "SAIU_PARA_ENTREGA" para este ID
        List<Pedido> pedidos = pedidoRepository.findByEntregadorIdAndStatus(
            entregadorId, 
            StatusPedido.SAIU_PARA_ENTREGA
        );

        // 3. Converte para DTO
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    // --- O MÉTODO (CONTADOR) ---
    @Override
    @Transactional(readOnly = true)
    public Long contarPedidosAtivosDoCliente() {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        
        if (usuarioId == null) {
            return 0L;
        }

        // Define quais status NÃO contam como "ativos"
        List<StatusPedido> statusIgnorados = Arrays.asList(
            StatusPedido.ENTREGUE, 
            StatusPedido.CANCELADO
        );

        // Passa a lista para o repositório
        return pedidoRepository.contarPedidosAtivosPorCliente(usuarioId, statusIgnorados);
    }

    /**
     * Calcula o total (Preview).
     */
    @Override
    @Transactional(readOnly = true)
    public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (ItemPedidoDTO item : dto.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
            
            BigDecimal precoItem = produto.getPrecoBase();
            List<Long> opcionaisIds = (item.getOpcionaisIds() != null) ? item.getOpcionaisIds() : new ArrayList<>();

            if (!opcionaisIds.isEmpty()) {
                 Set<Long> uniqueIds = new HashSet<>(opcionaisIds);
                 Map<Long, ItemOpcional> mapaOpcionais = itemOpcionalRepository.findAllById(uniqueIds).stream()
                        .collect(Collectors.toMap(ItemOpcional::getId, Function.identity()));

                 for(Long id : opcionaisIds) {
                     ItemOpcional op = mapaOpcionais.get(id);
                     if(op != null) precoItem = precoItem.add(op.getPrecoAdicional());
                 }
            }
            subtotal = subtotal.add(precoItem.multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
        
        BigDecimal taxa = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
        
        CalculoPedidoResponseDTO response = new CalculoPedidoResponseDTO();
        response.setSubtotal(subtotal);
        response.setTaxaEntrega(taxa);
        response.setTotal(subtotal.add(taxa));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id) {
        Timer.Sample sample = metricsService.iniciarTimerBanco();
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado: " + id));
            return mapToPedidoResponseDTO(pedido);
        } finally {
            metricsService.finalizarTimerBanco(sample);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
        return pedidos.stream().map(this::mapToPedidoResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        StatusPedido novoStatusEnum;
        try {
            novoStatusEnum = StatusPedido.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + dto.getStatus());
        }

        if (!isTransicaoValida(pedido.getStatus(), novoStatusEnum)) {
            throw new BusinessException("Transição inválida: " + pedido.getStatus() + " -> " + novoStatusEnum);
        }

        if (novoStatusEnum == StatusPedido.SAIU_PARA_ENTREGA) {
            if (dto.getEntregadorId() == null) {
                throw new BusinessException("Obrigatório selecionar entregador.");
            }
            Usuario entregador = usuarioRepository.findById(dto.getEntregadorId())
                    .orElseThrow(() -> new EntityNotFoundException("Entregador não encontrado"));

            if (entregador.getRole() != Role.ENTREGADOR) {
                throw new BusinessException("Usuário não é um entregador.");
            }
            pedido.setEntregador(entregador);
        }

        pedido.setStatus(novoStatusEnum);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        return mapToPedidoResponseDTO(pedidoAtualizado);
    }

    @Override
    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        
        if (!podeSerCancelado(pedido.getStatus())) {
            throw new BusinessException("Não pode cancelar no status: " + pedido.getStatus());
        }
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

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

    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarMeusPedidos(Pageable pageable) {
        Long usuarioIdLogado = SecurityUtils.getCurrentUserId();
        if (usuarioIdLogado == null) throw new BusinessException("Usuário não autenticado.");
        return pedidoRepository.findByClienteId(usuarioIdLogado, pageable).map(this::mapToPedidoResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {
        List<Pedido> pedidos = pedidoRepository.findPedidosByRestauranteIdAndStatusComItens(restauranteId, status);
        
        if (!pedidos.isEmpty()) {
            List<ItemPedido> todosOsItens = pedidos.stream()
                                                  .flatMap(p -> p.getItens().stream())
                                                  .collect(Collectors.toList());
            if (!todosOsItens.isEmpty()) {
                pedidoRepository.fetchOpcionaisParaItens(todosOsItens);
            }
        }
        return pedidos.stream().map(this::mapToPedidoResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccess(Long pedidoId) {
        try {
            Long usuarioId = SecurityUtils.getCurrentUserId();
            if (usuarioId == null) return false;
            
            Long clienteId = null;
            Long restauranteId = null;
            Long entregadorId = null;

            if (SecurityUtils.isCliente()) clienteId = usuarioId;
            else if (SecurityUtils.isRestaurante()) restauranteId = SecurityUtils.getCurrentRestauranteId();
            else if (SecurityUtils.isEntregador()) entregadorId = usuarioId;
            
            return pedidoRepository.isPedidoOwnedBy(pedidoId, clienteId, restauranteId, entregadorId);
        } catch (Exception e) {
            return false;
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private boolean isTransicaoValida(StatusPedido atual, StatusPedido novo) {
        switch (atual) {
            case PENDENTE: return novo == StatusPedido.CONFIRMADO || novo == StatusPedido.CANCELADO;
            case CONFIRMADO: return novo == StatusPedido.PREPARANDO || novo == StatusPedido.CANCELADO;
            case PREPARANDO: return novo == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA: return novo == StatusPedido.ENTREGUE;
            default: return false;
        }
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }

    /**
     * Método central de conversão Entity -> DTO.
     */
   private PedidoResponseDTO mapToPedidoResponseDTO(Pedido pedido) {
    PedidoResponseDTO dto = new PedidoResponseDTO();
    
    // Mapeamento Básico de Pedido
    dto.setId(pedido.getId());
    dto.setStatus(pedido.getStatus().name());
    dto.setDataPedido(pedido.getDataPedido());
    
    // Valores Totais
    dto.setTotal(pedido.getValorTotal());
    dto.setSubtotal(pedido.getSubtotal());
    dto.setTaxaEntrega(pedido.getTaxaEntrega());

    // Cliente (Null Safety)
    if (pedido.getCliente() != null) {
        dto.setClienteId(pedido.getCliente().getId());
        dto.setClienteNome(pedido.getCliente().getNome());
    } else {
        dto.setClienteNome("Cliente Desconhecido"); 
    }
    
    // Restaurante
    if (pedido.getRestaurante() != null) {
        dto.setRestauranteId(pedido.getRestaurante().getId());
        dto.setRestauranteNome(pedido.getRestaurante().getNome());
    }
    
    // Endereço
    if (pedido.getEnderecoEntrega() != null) {
        Endereco end = pedido.getEnderecoEntrega();
        dto.setEnderecoEntrega(String.format("%s, %s - %s", end.getRua(), end.getNumero(), end.getBairro()));
    }
    
    // --- Entregador (Null Safety + Priorização de Nome/Email) ---
    if (pedido.getEntregador() != null) {
        Usuario entregador = pedido.getEntregador();
        dto.setEntregadorId(entregador.getId());
        
        // Pega o NOME do entregador, se não tiver nome, usa o email
        String nomeEntregador = entregador.getNome();
        if (nomeEntregador == null || nomeEntregador.trim().isEmpty()) {
            nomeEntregador = entregador.getEmail();
        }
        
        dto.setEntregadorNome(nomeEntregador);
    } else {
        dto.setEntregadorNome("Aguardando atribuição");
    }

    // Itens (Mapeamento e Cálculo do Subtotal)
    List<ItemPedidoResponseDTO> itensResponse = pedido.getItens().stream()
        .map(item -> {
            ItemPedidoResponseDTO iDTO = new ItemPedidoResponseDTO();
            // Dados essenciais para o Front
            iDTO.setNomeProduto(item.getProduto().getNome());
            iDTO.setQuantidade(item.getQuantidade());
            iDTO.setPrecoUnitario(item.getPrecoUnitario());
            iDTO.setObservacao(item.getObservacoes());

            // Cálculo do Subtotal do item
            if (item.getPrecoUnitario() != null) {
                BigDecimal sub = item.getPrecoUnitario().multiply(new BigDecimal(item.getQuantidade()));
                iDTO.setSubtotal(sub); 
            } else {
                iDTO.setSubtotal(BigDecimal.ZERO);
            }
                return iDTO;
            }).collect(Collectors.toList());

        dto.setItens(itensResponse);

        return dto;
    }
}