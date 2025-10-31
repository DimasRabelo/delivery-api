package com.deliverytech.delivery.service;
    
import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoDTO;
import com.deliverytech.delivery.dto.response.CalculoPedidoResponseDTO;
import com.deliverytech.delivery.dto.response.PedidoResponseDTO;
import com.deliverytech.delivery.enums.StatusPedido;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
    


   
    
    public interface PedidoService {
    
    PedidoResponseDTO criarPedido(PedidoDTO dto);
    
    PedidoResponseDTO buscarPedidoPorId(Long id);
    
    List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId);
    
   PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status);
    
   CalculoPedidoResponseDTO calcularTotalPedido(CalculoPedidoDTO dto);

    void cancelarPedido(Long id);

    List<PedidoResponseDTO> buscarPedidosPorRestaurante(Long restauranteId, StatusPedido status);

    Page<PedidoResponseDTO> listarPedidos(StatusPedido status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable);

      

      // 👇 ESTE ESTAVA FALTANDO (o do Cliente paginado)
    Page<PedidoResponseDTO> listarMeusPedidos(Pageable pageable);
}