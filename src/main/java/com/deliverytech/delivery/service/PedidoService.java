package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.request.StatusPedidoDTO; 
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface de serviço para gerenciamento de Pedidos.
 * Contém operações de criação, atualização, cancelamento e consultas.
 */
public interface PedidoService {

    // ==========================================================
    // --- CRIAÇÃO DE PEDIDO ---
    // ==========================================================
    /**
     * Cria um novo pedido com os itens e dados fornecidos no DTO.
     * @param dto Dados do pedido
     * @return PedidoResponseDTO com informações do pedido criado
     */
    PedidoResponseDTO criarPedido(PedidoDTO dto);

    // ==========================================================
    // --- CONSULTAS ---
    // ==========================================================
    /**
     * Busca um pedido pelo seu ID.
     * @param id ID do pedido
     * @return PedidoResponseDTO
     */
    PedidoResponseDTO buscarPedidoPorId(Long id);

    /**
     * Busca todos os pedidos de um cliente específico.
     * @param clienteId ID do cliente
     * @return Lista de PedidoResponseDTO
     */
    List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId);

    /**
     * Busca pedidos de um restaurante filtrando por status.
     * @param restauranteId ID do restaurante
     * @param status Status do pedido
     * @return Lista de PedidoResponseDTO
     */
    List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status);

    /**
     * Lista pedidos paginados, podendo filtrar por status e período.
     * @param status Status do pedido
     * @param dataInicio Data inicial
     * @param dataFim Data final
     * @param pageable Paginação
     * @return Página de PedidoResponseDTO
     */
    Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);

    /**
     * Lista os pedidos do usuário logado (meus pedidos) paginados.
     * @param pageable Paginação
     * @return Página de PedidoResponseDTO
     */
    Page<PedidoResponseDTO> listarMeusPedidos(Pageable pageable);

    // ==========================================================
    // --- ATUALIZAÇÃO DE PEDIDO ---
    // ==========================================================
    /**
     * Atualiza o status de um pedido.
     * Agora utiliza StatusPedidoDTO para compatibilidade com validação/DTO.
     * @param id ID do pedido
     * @param statusPedidoDTO DTO contendo o novo status
     * @return PedidoResponseDTO atualizado
     */
    PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedidoDTO statusPedidoDTO);

    // ==========================================================
    // --- CÁLCULOS / LOGÍCA DE NEGÓCIO ---
    // ==========================================================
    /**
     * Calcula o total de um pedido baseado nos itens, opcionais e taxas.
     * @param dto DTO com informações do pedido
     * @return DTO com valores calculados
     */
    CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto);

    // ==========================================================
    // --- CANCELAMENTO ---
    // ==========================================================
    /**
     * Cancela um pedido pelo ID.
     * @param id ID do pedido
     */
    void cancelarPedido(Long id);

    // ==========================================================
    // --- AUTORIZAÇÃO / ACESSO ---
    // ==========================================================
    /**
     * Verifica se o usuário logado tem permissão para acessar o pedido.
     * @param pedidoId ID do pedido
     * @return true se pode acessar, false caso contrário
     */
    boolean canAccess(Long pedidoId);

    // ==========================================================
    // --- MÉTODO CONTAR PEDIDOS ---
    // ==========================================================
    /**
     * Conta quantos pedidos ativos o cliente tem no momento.
     * @return Quantidade de pedidos (Long)
     */
    Long contarPedidosAtivosDoCliente();



    // --- MÉTODO PARA O ENTREGADOR ---
    List<PedidoResponseDTO> buscarPedidosPendentesEntregador();
}