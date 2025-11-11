package com.deliverytech.delivery.service;
    
import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.enums.StatusPedido;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// ==========================================================
// <-- 1. ADICIONE ESTE IMPORT
// (Baseado no pacote do seu StatusPedidoDTO)
// ==========================================================
import com.deliverytech.delivery.dto.request.StatusPedidoDTO;

    
public interface PedidoService {
    
    PedidoResponseDTO criarPedido(PedidoDTO dto);
    
    PedidoResponseDTO buscarPedidoPorId(Long id);
    
    List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId);
    
    // ==========================================================
    // <-- 2. AQUI ESTÁ A MUDANÇA
    // ==========================================================
    
    // MÉTODO ANTIGO (com erro de compilação):
    //PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status);

    // MÉTODO NOVO (corrigido para aceitar o DTO):
    PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedidoDTO statusPedidoDTO);
    
    // ==========================================================
    
    CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto);

    void cancelarPedido(Long id);

    List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status);

    Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);
    
    Page<PedidoResponseDTO> listarMeusPedidos(Pageable pageable);

    boolean canAccess(Long pedidoId);
}