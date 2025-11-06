package com.deliverytech.delivery.service.impl;

// --- IMPORTS DE ENTIDADES, DTOS E REPOSITÓRIOS NOVOS ---
import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.entity.ItemOpcional;
import com.deliverytech.delivery.entity.ItemPedidoOpcional;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.ItemOpcionalRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
// --- FIM DOS NOVOS IMPORTS ---

import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
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
import java.util.UUID;
import java.util.stream.Collectors;
import com.deliverytech.delivery.service.metrics.MetricsService;
import io.micrometer.core.instrument.Timer;
import com.deliverytech.delivery.service.audit.AuditService;

@Service
public class PedidoServiceImpl implements PedidoService {

    // --- REPOSITÓRIOS ANTIGOS ---
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private RestauranteRepository restauranteRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private ModelMapper modelMapper;
    @Autowired private MetricsService metricsService;
    @Autowired private AuditService auditService;
    //@Autowired private ClienteRepository clienteRepository; // Mantido para ligar ao Pedido

    // --- NOVOS REPOSITÓRIOS (NECESSÁRIOS) ---
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ItemOpcionalRepository itemOpcionalRepository;

    /**
     * Cria um novo pedido no sistema (VERSÃO REFATORADA).
     * Usa a nova arquitetura de Entidades e DTOs.
     */
    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) { // <-- DTO refatorado
        Timer.Sample sample = metricsService.iniciarTimerPedido();
        metricsService.incrementarPedidosProcessados();
        
        Long usuarioId = SecurityUtils.getCurrentUserId(); // Pega o ID do usuário logado
        String usuarioIdLog = (usuarioId != null) ? usuarioId.toString() : "ANONIMO";

        try {
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_INICIO", "PedidoDTO", dto);

            // --- 1. BUSCAR ENTIDADES PRINCIPAIS ---
            if (usuarioId == null) {
                throw new BusinessException("Acesso negado. Usuário não autenticado.");
            }

            // Busca o Usuário (para checar 'ativo' e pegar o 'Cliente')
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

            // Busca o Perfil Cliente (ligado ao Usuário)
            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                throw new BusinessException("Este usuário não possui um perfil de cliente.");
            }

            // CORREÇÃO: 'ativo' agora está em Usuário (Decisão 1)
            if (!usuario.getAtivo()) {
                throw new BusinessException("Cliente inativo não pode fazer pedidos");
            }

            Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                    .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
            if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
                throw new BusinessException("Restaurante não está disponível");
            }

            // CORREÇÃO: Busca o Endereço (Gargalo 1)
            Endereco endereco = enderecoRepository.findById(dto.getEnderecoEntregaId())
                    .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

            // Validação de segurança: O endereço pertence ao usuário logado?
            if (!endereco.getUsuario().getId().equals(usuarioId)) {
                throw new BusinessException("Endereço de entrega inválido. Pertence a outro usuário.");
            }

            // --- 2. CRIAR A ENTIDADE PEDIDO (SHELL) ---
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setRestaurante(restaurante);
            pedido.setDataPedido(LocalDateTime.now());
            pedido.setStatus(StatusPedido.PENDENTE);
            pedido.setObservacoes(dto.getObservacoes());

            // CORREÇÃO: Setar os novos campos (Gargalo 1 e 3)
            pedido.setEnderecoEntrega(endereco); // <-- MUDOU DE STRING PARA ENTITY
            pedido.setMetodoPagamento(dto.getMetodoPagamento()); // <-- NOVO
            pedido.setTrocoPara(dto.getTrocoPara()); // <-- NOVO

            BigDecimal subtotal = BigDecimal.ZERO;

            // --- 3. LOOP DOS ITENS (LÓGICA DOS OPICIONAIS - Gargalo 2) ---
            for (ItemPedidoDTO itemDTO : dto.getItens()) {
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));

                // Validações de produto
                if (produto.getDisponivel() == null || !produto.getDisponivel()) {
                    throw new BusinessException("Produto indisponível: " + produto.getNome());
                }
                if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                    throw new BusinessException("Produto " + produto.getNome() + " não pertence ao restaurante selecionado");
                }
                if (produto.getEstoque() < itemDTO.getQuantidade()) {
                    throw new BusinessException("Estoque insuficiente para o produto: " + produto.getNome());
                }

                // --- INÍCIO DA LÓGICA DE PREÇO (REFATORADA) ---

                // 1. Começa com o preço base
                BigDecimal precoUnitarioCalculado = produto.getPrecoBase(); // <-- MUDOU (getPreco -> getPrecoBase)

                ItemPedido item = new ItemPedido();
                item.setProduto(produto);
                item.setQuantidade(itemDTO.getQuantidade());
                item.setPedido(pedido);

                // 2. Itera sobre os opcionais enviados (Gargalo 2)
                if (itemDTO.getOpcionaisIds() != null && !itemDTO.getOpcionaisIds().isEmpty()) {
                    for (Long opcionalId : itemDTO.getOpcionaisIds()) {
                        ItemOpcional opcional = itemOpcionalRepository.findById(opcionalId)
                                .orElseThrow(() -> new EntityNotFoundException("Opcional não encontrado: " + opcionalId));

                        // TODO: Adicionar validação se o 'opcional' pertence ao 'produto'
                        // (Verifica se opcional.getGrupoOpcional().getProduto().getId() é igual ao produto.getId())

                        // 3. Soma o preço do opcional
                        precoUnitarioCalculado = precoUnitarioCalculado.add(opcional.getPrecoAdicional());

                        // 4. Cria o "link" (ItemPedidoOpcional) e salva o preço
                        ItemPedidoOpcional linkOpcional = new ItemPedidoOpcional(item, opcional);
                        item.getOpcionaisSelecionados().add(linkOpcional);
                    }
                }

                // 5. Define o preço final calculado
                item.setPrecoUnitario(precoUnitarioCalculado);
                item.calcularSubtotal(); // Calcula (precoUnitarioCalculado * quantidade)

                // --- FIM DA LÓGICA DE PREÇO ---

                pedido.getItens().add(item); // Salvará em cascade
                subtotal = subtotal.add(item.getSubtotal());

                // Baixa no estoque
                produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
                produtoRepository.save(produto);
            }

            // --- 4. FINALIZAR O PEDIDO ---
            BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
            BigDecimal valorTotal = subtotal.add(taxaEntrega);

            pedido.setSubtotal(subtotal);
            pedido.setTaxaEntrega(taxaEntrega);
            pedido.setValorTotal(valorTotal);
            pedido.setNumeroPedido(UUID.randomUUID().toString().substring(0, 18)); // Um UUID mais curto

            Pedido pedidoSalvo = pedidoRepository.save(pedido);

            // --- 5. MÉTRICAS E RETORNO ---
            metricsService.incrementarPedidosComSucesso();
            metricsService.adicionarReceita(pedidoSalvo.getValorTotal().doubleValue());
            
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_SUCESSO", "Pedido", pedidoSalvo);

            return mapToPedidoResponseDTO(pedidoSalvo);

        } catch (Exception e) {
            metricsService.incrementarPedidosComErro();
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_FALHA", e.getClass().getSimpleName(), e.getMessage());
            throw e; // Relança a exceção para o GlobalExceptionHandler
        } finally {
            metricsService.finalizarTimerPedido(sample);
        }
    }

    /**
     * Calcula o total de um pedido (VERSÃO REFATORADA E CORRIGIDA).
     */
    @Override
    @Transactional(readOnly = true)
    public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {
        BigDecimal subtotal = BigDecimal.ZERO;

        // O DTO 'CalculoPedidoDTO' precisa ter a List<ItemPedidoDTO> com opcionais
        for (ItemPedidoDTO item : dto.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Produto não encontrado: " + item.getProdutoId()));
            
            // 1. Começa com o preço base
            BigDecimal precoItem = produto.getPrecoBase();

            // 2. Soma os opcionais
            if (item.getOpcionaisIds() != null) {
                for (Long opcionalId : item.getOpcionaisIds()) {
                    
                    // --- AQUI ESTAVA O BUG (AGORA CORRIGIDO) ---
                    // O tipo da variável é 'ItemOpcional'
                    ItemOpcional opcional = itemOpcionalRepository.findById(opcionalId)
                            .orElseThrow(() -> new EntityNotFoundException("Opcional não encontrado: " + opcionalId));
                    
                    // Agora esta linha funciona
                    precoItem = precoItem.add(opcional.getPrecoAdicional());
                }
            }
            
            // 3. Multiplica pela quantidade
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


    // ==========================================================
    // SEUS OUTROS MÉTODOS (A maioria deve funcionar agora)
    // ==========================================================

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

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByDataPedidoDesc(clienteId);
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        if (!isTransicaoValida(pedido.getStatus(), novoStatus)) {
            throw new BusinessException("Transição de status inválida: " +
                    pedido.getStatus() + " -> " + novoStatus);
        }
        
        // TODO: Adicionar lógica de atribuição de Entregador
        if (novoStatus == StatusPedido.SAIU_PARA_ENTREGA) {
             // 1. Encontrar um entregador disponível
             // Usuario entregador = ... (lógica para achar entregador)
             // 2. Atribuir ao pedido
             // pedido.setEntregador(entregador);
        }
        
        pedido.setStatus(novoStatus);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        return mapToPedidoResponseDTO(pedidoAtualizado);
    }

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
        if (usuarioIdLogado == null) {
            throw new BusinessException("Acesso negado. Usuário não autenticado.");
        }
        // Na arquitetura Decisão 1, o Cliente.id == Usuario.id
        Page<Pedido> paginaPedidos = pedidoRepository.findByClienteId(usuarioIdLogado, pageable);
        return paginaPedidos.map(this::mapToPedidoResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {
        List<Pedido> pedidos;
        if (status != null) {
            pedidos = pedidoRepository.findByRestauranteIdAndStatus(restauranteId, status, Pageable.unpaged()).getContent();
        } else {
            pedidos = pedidoRepository.findByRestauranteId(restauranteId, Pageable.unpaged()).getContent();
        }
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
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
                return false; // Nenhum status após entregue
            case CANCELADO:
                return false; // Nenhum status após cancelado
            default:
                return false;
        }
    }

    private boolean podeSerCancelado(StatusPedido status) {
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }

    /**
     * Mapeia a Entidade Pedido para o DTO de Resposta (REFATORADO).
     */
    private PedidoResponseDTO mapToPedidoResponseDTO(Pedido pedido) {
        PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);

        // Mapeia dados do Cliente (Decisão 1: Nome está em Cliente)
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome()); 
        }

        // Mapeia dados do Restaurante
        if (pedido.getRestaurante() != null) {
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
        }

        // CORREÇÃO: Mapear o Endereço (Gargalo 1)
        if (pedido.getEnderecoEntrega() != null) {
            Endereco end = pedido.getEnderecoEntrega();
            // Formata o endereço (Rua, Num - Bairro)
            String enderecoFormatado = String.format("%s, %s - %s, %s/%s",
                    end.getRua(),
                    end.getNumero(),
                    end.getBairro(),
                    end.getCidade(),
                    end.getEstado());
            dto.setEnderecoEntrega(enderecoFormatado); // Seta a string formatada no DTO
        }

        dto.setTotal(pedido.getValorTotal());

        // Mapeia os itens
        dto.setItens(pedido.getItens().stream()
                .map(item -> {
                    // Nota: O ItemPedidoDTO aqui é o que será enviado na RESPOSTA.
                    // Está OK usar o mesmo DTO que refatoramos.
                    ItemPedidoDTO iDTO = new ItemPedidoDTO();
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    
                    // (Opcional, mas recomendado) Mapeia os opcionais escolhidos
                    if (item.getOpcionaisSelecionados() != null) {
                         iDTO.setOpcionaisIds(item.getOpcionaisSelecionados().stream()
                            .map(opcionalLink -> opcionalLink.getItemOpcional().getId())
                            .collect(Collectors.toList()));
                    }
                    return iDTO;
                }).collect(Collectors.toList()));

        return dto;
    }
}