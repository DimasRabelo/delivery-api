package com.deliverytech.delivery.service.impl;


import com.deliverytech.delivery.dto.request.ItemPedidoDTO;
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.dto.request.StatusPedidoDTO;
import com.deliverytech.delivery.entity.*;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.*;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.service.PedidoService;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.metrics.MetricsService;
import com.deliverytech.delivery.service.audit.AuditService;

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


    /**
     * Processa e salva um novo pedido no sistema.
     * Valida usuário, restaurante, produtos, estoque e regras de opcionais.
     */
    @Override
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoDTO dto) {
        // Métricas: Inicia o timer e incrementa o contador de pedidos processados
        Timer.Sample sample = metricsService.iniciarTimerPedido();
        metricsService.incrementarPedidosProcessados();

        Long usuarioId = SecurityUtils.getCurrentUserId();
        String usuarioIdLog = (usuarioId != null) ? usuarioId.toString() : "ANONIMO";

        try {
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_INICIO", "PedidoDTO", dto);

            // --- 1. Validação do Usuário e Cliente ---
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

            // --- 2. Validação do Restaurante ---
            Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                    .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
            if (restaurante.getAtivo() == null || !restaurante.getAtivo()) {
                throw new BusinessException("Restaurante não está disponível");
            }

            // --- 3. Validação do Endereço de Entrega ---
            Endereco endereco = enderecoRepository.findById(dto.getEnderecoEntregaId())
                    .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
            // Garante que o endereço selecionado pertence ao usuário logado
            if (!endereco.getUsuario().getId().equals(usuarioId)) {
                throw new BusinessException("Endereço de entrega inválido. Pertence a outro usuário.");
            }

            // --- 4. Inicialização do Pedido ---
            Pedido pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setRestaurante(restaurante);
            pedido.setDataPedido(LocalDateTime.now());
            pedido.setStatus(StatusPedido.PENDENTE); // Status inicial
            pedido.setObservacoes(dto.getObservacoes());
            pedido.setEnderecoEntrega(endereco);
            pedido.setMetodoPagamento(dto.getMetodoPagamento());
            pedido.setTrocoPara(dto.getTrocoPara());
            // Zera os valores iniciais para cálculo no loop
            pedido.setSubtotal(BigDecimal.ZERO);
            pedido.setTaxaEntrega(BigDecimal.ZERO);
            pedido.setValorTotal(BigDecimal.ZERO);

            BigDecimal subtotal = BigDecimal.ZERO;

            // --- 5. Processamento e Validação dos Itens do Pedido ---
            for (ItemPedidoDTO itemDTO : dto.getItens()) {
                // Valida o produto
                Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDTO.getProdutoId()));
                if (produto.getDisponivel() == null || !produto.getDisponivel()) {
                    throw new BusinessException("Produto indisponível: " + produto.getNome());
                }
                if (!produto.getRestaurante().getId().equals(dto.getRestauranteId())) {
                    throw new BusinessException("Produto " + produto.getNome() + " não pertence ao restaurante selecionado");
                }
                // Valida o estoque
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

                // --- 5a. Processamento e Validação dos Opcionais ---
                if (!opcionaisIdsEnviados.isEmpty()) {
                    // Busca todos os opcionais de uma vez para melhor performance
                    Set<Long> uniqueIds = new HashSet<>(opcionaisIdsEnviados);
                    Map<Long, ItemOpcional> mapaOpcionais = itemOpcionalRepository.findAllById(uniqueIds).stream()
                            .collect(Collectors.toMap(ItemOpcional::getId, Function.identity()));

                    List<ItemOpcional> opcionaisSelecionados = new ArrayList<>();
                    for (Long id : opcionaisIdsEnviados) {
                        ItemOpcional opcional = mapaOpcionais.get(id);
                        if (opcional == null) {
                            throw new EntityNotFoundException("Opcional não encontrado: " + id);
                        }
                         // Valida se o opcional realmente pertence ao produto
                         if (opcional.getGrupoOpcional() == null ||
                             opcional.getGrupoOpcional().getProduto() == null ||
                             !opcional.getGrupoOpcional().getProduto().getId().equals(produto.getId())) {
                             throw new BusinessException("Opcional inválido: '" + opcional.getNome() +
                                     "' não pertence ao produto '" + produto.getNome() + "'");
                         }
                        opcionaisSelecionados.add(opcional);
                    }

                    // Valida regras de Mínimo e Máximo por Grupo
                    Map<GrupoOpcional, Long> contagemPorGrupo = opcionaisSelecionados.stream()
                            .collect(Collectors.groupingBy(ItemOpcional::getGrupoOpcional, Collectors.counting()));
                    
                    List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());

                    for (GrupoOpcional grupo : gruposDoProduto) {
                        long contagem = contagemPorGrupo.getOrDefault(grupo, 0L);
                        // Valida seleção mínima
                        if (contagem < grupo.getMinSelecao()) {
                            throw new BusinessException(String.format(
                                "Seleção obrigatória para '%s'. Mínimo: %d, Enviado: %d",
                                grupo.getNome(), grupo.getMinSelecao(), contagem
                            ));
                        }
                        // Valida seleção máxima
                        if (contagem > grupo.getMaxSelecao()) {
                            throw new BusinessException(String.format(
                                "Seleção máxima excedida para '%s'. Máximo: %d, Enviado: %d",
                                grupo.getNome(), grupo.getMaxSelecao(), contagem
                            ));
                        }
                    }

                    // Adiciona o preço dos opcionais e salva a relação
                    for (ItemOpcional opcional : opcionaisSelecionados) {
                        precoUnitarioCalculado = precoUnitarioCalculado.add(opcional.getPrecoAdicional());
                        ItemPedidoOpcional linkOpcional = new ItemPedidoOpcional(item, opcional); // Cria a entidade de ligação
                        item.getOpcionaisSelecionados().add(linkOpcional);
                    }
                } else {
                    // Se nenhum opcional foi enviado, verifica se algum grupo obrigatório foi ignorado
                     List<GrupoOpcional> gruposDoProduto = grupoOpcionalRepository.findByProdutoId(produto.getId());
                     for (GrupoOpcional grupo : gruposDoProduto) {
                        if (grupo.getMinSelecao() > 0) { // Se o mínimo é > 0, era obrigatório
                             throw new BusinessException(String.format(
                                "Seleção obrigatória para '%s'. Mínimo: %d, Enviado: 0",
                                grupo.getNome(), grupo.getMinSelecao()
                            ));
                        }
                     }
                }

                // --- 5b. Finalização do Item ---
                item.setPrecoUnitario(precoUnitarioCalculado); // Preço base + opcionais
                item.calcularSubtotal(); // (Preço Unitário * Quantidade)
                pedido.getItens().add(item);
                subtotal = subtotal.add(item.getSubtotal()); // Soma ao subtotal geral

                // Abate o estoque
                produto.setEstoque(produto.getEstoque() - itemDTO.getQuantidade());
                produtoRepository.save(produto);
            }

            // --- 6. Cálculo Final e Salvamento ---
            BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
            BigDecimal valorTotal = subtotal.add(taxaEntrega);

            pedido.setSubtotal(subtotal);
            pedido.setTaxaEntrega(taxaEntrega);
            pedido.setValorTotal(valorTotal);
            pedido.setNumeroPedido(UUID.randomUUID().toString().substring(0, 18)); // Gera um número de pedido curto

            Pedido pedidoSalvo = pedidoRepository.save(pedido);

            // --- 7. Métricas e Auditoria Pós-Sucesso ---
            metricsService.incrementarPedidosComSucesso();
            BigDecimal totalFinal = pedidoSalvo.getValorTotal() != null ? pedidoSalvo.getValorTotal() : BigDecimal.ZERO;
            metricsService.adicionarReceita(totalFinal.doubleValue());
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_SUCESSO", "Pedido", pedidoSalvo);

            return mapToPedidoResponseDTO(pedidoSalvo);

        } catch (Exception e) {
            // Em caso de qualquer falha, registra métrica de erro e auditoria
            metricsService.incrementarPedidosComErro();
            auditService.logUserAction(usuarioIdLog, "CRIAR_PEDIDO_FALHA", e.getClass().getSimpleName(), e.getMessage());
            throw e; // Relança a exceção para o ControllerAdvice tratar
        } finally {
            // Garante que o timer seja finalizado, com sucesso ou falha
            metricsService.finalizarTimerPedido(sample);
        }
    }

    /**
     * Calcula o total de um pedido com base nos itens e restaurante,
     * sem salvar o pedido. Usado para pré-visualização no front-end.
     */
    @Override
    @Transactional(readOnly = true)
    public CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto) {
        BigDecimal subtotal = BigDecimal.ZERO;

        // Loop pelos itens para calcular o subtotal
        for (ItemPedidoDTO item : dto.getItens()) {
            Produto produto = produtoRepository.findById(item.getProdutoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + item.getProdutoId()));
            
            BigDecimal precoItem = produto.getPrecoBase();

            List<Long> opcionaisIdsEnviados = (item.getOpcionaisIds() != null)
                    ? item.getOpcionaisIds()
                    : new ArrayList<>();

            // Lógica de validação e cálculo de opcionais (similar ao criarPedido)
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
                    // Valida se o opcional pertence ao produto
                    if (opcional.getGrupoOpcional() == null ||
                        opcional.getGrupoOpcional().getProduto() == null ||
                        !opcional.getGrupoOpcional().getProduto().getId().equals(produto.getId())) {
                        throw new BusinessException("Opcional inválido: '" + opcional.getNome() + "'");
                    }
                    opcionaisSelecionados.add(opcional);
                }

                // Valida regras de Min/Max dos grupos
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

                // Soma o preço dos opcionais
                for (ItemOpcional opcional : opcionaisSelecionados) {
                    precoItem = precoItem.add(opcional.getPrecoAdicional());
                }
            } else {
                 // Valida se grupos obrigatórios foram ignorados
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
            // Adiciona ao subtotal (Preço do item com opcionais * Quantidade)
            subtotal = subtotal.add(precoItem.multiply(BigDecimal.valueOf(item.getQuantidade())));
        }

        // Busca a taxa de entrega do restaurante
        Restaurante restaurante = restauranteRepository.findById(dto.getRestauranteId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado"));
        
        BigDecimal taxaEntrega = restaurante.getTaxaEntrega() != null ? restaurante.getTaxaEntrega() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(taxaEntrega);

        // Monta a resposta
        CalculoPedidoResponseDTO response = new CalculoPedidoResponseDTO();
        response.setSubtotal(subtotal);
        response.setTaxaEntrega(taxaEntrega);
        response.setTotal(total);
        return response;
    }


    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPedidoPorId(Long id) {
        // Timer para monitorar consulta ao banco
        Timer.Sample sample = metricsService.iniciarTimerBanco();
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com ID: " + id));
            return mapToPedidoResponseDTO(pedido);
        } finally {
            metricsService.finalizarTimerBanco(sample); // Finaliza o timer
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

    /**
     * Atualiza o status de um pedido, validando a transição de estado.
     * Requer a atribuição manual de um entregador se o novo status for "SAIU_PARA_ENTREGA".
     */
    @Override
    @Transactional
    public PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedidoDTO dto) {
        // 1. Busca o pedido
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        // 2. Converte a string do DTO para o Enum
        StatusPedido novoStatusEnum;
        try {
            // Garante que o status vindo do DTO seja comparável ao Enum (ex: "preparando" -> "PREPARANDO")
            novoStatusEnum = StatusPedido.valueOf(dto.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + dto.getStatus());
        }

        // 3. Valida a transição (ex: PENDENTE -> CONFIRMADO é válido)
        if (!isTransicaoValida(pedido.getStatus(), novoStatusEnum)) {
            throw new BusinessException("Transição de status inválida: " +
                    pedido.getStatus() + " -> " + novoStatusEnum);
        }

        // 4. Lógica de Atribuição MANUAL para "SAIU_PARA_ENTREGA"
        if (novoStatusEnum == StatusPedido.SAIU_PARA_ENTREGA) {

            // 5. Valida se o ID do entregador foi enviado no DTO
            if (dto.getEntregadorId() == null) {
                throw new BusinessException("É obrigatório selecionar um entregador para o status SAIU_PARA_ENTREGA.");
            }

            // 6. Busca o entregador enviado pelo front-end
            Usuario entregador = usuarioRepository.findById(dto.getEntregadorId())
                    .orElseThrow(() -> new EntityNotFoundException("Entregador não encontrado: " + dto.getEntregadorId()));

            // 7. Valida se o usuário encontrado é de fato um entregador
            if (entregador.getRole() != Role.ENTREGADOR) {
                throw new BusinessException("Usuário selecionado não é um entregador.");
            }
            
            // 8. Opcional: Checar se o entregador já está ocupado
            // (Descomente se esta regra de negócio for necessária)
            // boolean estaEmEntrega = pedidoRepository.existsByEntregadorAndStatus(
            //         entregador, StatusPedido.SAIU_PARA_ENTREGA
            // );
            // if (estaEmEntrega) {
            //     throw new BusinessException("Este entregador já está em uma entrega no momento.");
            // }

            // 9. ATRIBUI O ENTREGADOR MANUALMENTE ao pedido
            pedido.setEntregador(entregador);
        }

        // 10. Salva o novo status no pedido
        pedido.setStatus(novoStatusEnum);
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);

        // 11. Retorna o DTO de resposta
        return mapToPedidoResponseDTO(pedidoAtualizado);
    }

    /**
     * Cancela um pedido, se o status atual permitir.
     */
    @Override
    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        
        // Verifica se o status (ex: PENDENTE ou CONFIRMADO) permite cancelamento
        if (!podeSerCancelado(pedido.getStatus())) {
            throw new BusinessException("Pedido não pode ser cancelado no status: " + pedido.getStatus());
        }
        
        pedido.setStatus(StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);
    }

    /**
     * Lista pedidos de forma paginada, com filtros opcionais de status e período.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        Page<Pedido> pedidos;
        LocalDateTime inicio = null, fim = null;

        // Converte datas para LocalDateTime (início do dia e fim do dia)
        if (dataInicio != null && dataFim != null) {
            inicio = dataInicio.atStartOfDay();
            fim = dataFim.plusDays(1).atStartOfDay(); // Usa o início do dia seguinte para incluir a data fim
        }

        // Lógica de consulta dinâmica baseada nos filtros fornecidos
        if (status != null && inicio != null) {
            pedidos = pedidoRepository.findByStatusAndDataPedidoBetween(status, inicio, fim, pageable);
        } else if (status != null) {
            pedidos = pedidoRepository.findByStatus(status, pageable);
        } else if (inicio != null) {
            pedidos = pedidoRepository.findByDataPedidoBetween(inicio, fim, pageable);
        } else {
            // Nenhum filtro, busca todos
            pedidos = pedidoRepository.findAll(pageable);
        }
        
        return pedidos.map(this::mapToPedidoResponseDTO);
    }

    /**
     * Lista os pedidos paginados pertencentes ao usuário (cliente) logado.
     */
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

    /**
     * Busca pedidos de um restaurante específico, otimizado para carregar
     * itens e opcionais (resolvendo N+1).
     */
    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status) {
        // 1. Busca pedidos e seus itens (Join Fetch)
        List<Pedido> pedidos = pedidoRepository.findPedidosByRestauranteIdAndStatusComItens(restauranteId, status);
        
        if (!pedidos.isEmpty()) {
            // 2. Coleta todos os itens de todos os pedidos
            List<ItemPedido> todosOsItens = pedidos.stream()
                                                  .flatMap(pedido -> pedido.getItens().stream())
                                                  .collect(Collectors.toList());
            
            // 3. Busca todos os opcionais para esses itens em uma única consulta
            if (!todosOsItens.isEmpty()) {
                pedidoRepository.fetchOpcionaisParaItens(todosOsItens);
            }
        }
        
        return pedidos.stream()
                .map(this::mapToPedidoResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Método de segurança para verificar se o usuário logado (seja cliente,
     * restaurante ou entregador) tem permissão para acessar um pedido específico.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canAccess(Long pedidoId) {
        try {
            Long usuarioIdLogado = SecurityUtils.getCurrentUserId();
            if (usuarioIdLogado == null) {
                return false; // Não autenticado
            }
            
            Long clienteId = null;
            Long restauranteLogadoId = null;
            Long entregadorId = null;

            // Define qual ID usar na consulta com base na ROLE do usuário
            if (SecurityUtils.isCliente()) {
                clienteId = usuarioIdLogado;
            } else if (SecurityUtils.isRestaurante()) {
                restauranteLogadoId = SecurityUtils.getCurrentRestauranteId();
            } else if (SecurityUtils.isEntregador()) {
                entregadorId = usuarioIdLogado;
            }
            
            // O repositório faz a verificação (OWNER do pedido)
            return pedidoRepository.isPedidoOwnedBy(
                pedidoId,
                clienteId,
                restauranteLogadoId,
                entregadorId
            );
            
        } catch (Exception e) {
            return false; // Em caso de erro, nega o acesso
        }
    }

    // --- Métodos Privados (Helper) ---

    /**
     * Define a "máquina de estados" do pedido, controlando quais
     * transições de status são permitidas.
     */
    private boolean isTransicaoValida(StatusPedido statusAtual, StatusPedido novoStatus) {
        switch (statusAtual) {
            case PENDENTE:
                // Um pedido pendente só pode ser confirmado ou cancelado
                return novoStatus == StatusPedido.CONFIRMADO || novoStatus == StatusPedido.CANCELADO;
            case CONFIRMADO:
                // Um pedido confirmado pode ir para preparo ou ser cancelado
                return novoStatus == StatusPedido.PREPARANDO || novoStatus == StatusPedido.CANCELADO;
            case PREPARANDO:
                // Em preparo só pode sair para entrega
                return novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA:
                // Saiu para entrega só pode ser marcado como entregue
                return novoStatus == StatusPedido.ENTREGUE;
            case ENTREGUE:
                return false; // Entregue é um estado final
            case CANCELADO:
                return false; // Cancelado é um estado final
            default:
                return false;
        }
    }

    /**
     * Regra de negócio para definir quais status permitem o cancelamento.
     */
    private boolean podeSerCancelado(StatusPedido status) {
        // Só pode cancelar se estiver PENDENTE ou já CONFIRMADO (antes de iniciar o preparo)
        return status == StatusPedido.PENDENTE || status == StatusPedido.CONFIRMADO;
    }

    /**
     * Converte a entidade Pedido para um DTO de resposta (PedidoResponseDTO),
     * formatando dados e resolvendo IDs.
     */
    private PedidoResponseDTO mapToPedidoResponseDTO(Pedido pedido) {
        // O ModelMapper cuida dos campos com nomes iguais
        PedidoResponseDTO dto = modelMapper.map(pedido, PedidoResponseDTO.class);
        
        // --- Mapeamentos Manuais (para campos com nomes diferentes ou formatação) ---
        
        // Cliente
        if (pedido.getCliente() != null) {
            dto.setClienteId(pedido.getCliente().getId());
            dto.setClienteNome(pedido.getCliente().getNome());
        }
        
        // Restaurante
        if (pedido.getRestaurante() != null) {
            dto.setRestauranteId(pedido.getRestaurante().getId());
            dto.setRestauranteNome(pedido.getRestaurante().getNome());
        }
        
        // Endereço (Formata para uma string legível)
        if (pedido.getEnderecoEntrega() != null) {
            Endereco end = pedido.getEnderecoEntrega();
            String enderecoFormatado = String.format("%s, %s - %s, %s/%s",
                    end.getRua(), end.getNumero(), end.getBairro(), end.getCidade(), end.getEstado());
            dto.setEnderecoEntrega(enderecoFormatado);
        }
        
        // Entregador (Se houver um atribuído)
        if (pedido.getEntregador() != null) {
            Usuario entregador = pedido.getEntregador();
            dto.setEntregadorId(entregador.getId());
            dto.setEntregadorNome(entregador.getEmail()); // (ou getNome() se Usuario tiver nome)
        }
        
        // Total (Mapeamento explícito para garantir)
        dto.setTotal(pedido.getValorTotal());
        
        // Itens (Mapeia a lista de entidades para lista de DTOs)
        dto.setItens(pedido.getItens().stream()
                .map(item -> {
                    ItemPedidoDTO iDTO = new ItemPedidoDTO();
                    iDTO.setProdutoId(item.getProduto().getId());
                    iDTO.setQuantidade(item.getQuantidade());
                    
                    // Mapeia os IDs dos opcionais selecionados para aquele item
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