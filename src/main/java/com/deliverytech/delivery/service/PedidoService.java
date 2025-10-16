    package com.deliverytech.delivery.service;
    
    import com.deliverytech.delivery.dto.PedidoDTO;
    import com.deliverytech.delivery.dto.PedidoResponseDTO;
    import com.deliverytech.delivery.dto.ItemPedidoDTO;
    import com.deliverytech.delivery.enums.StatusPedido;
    import java.math.BigDecimal;
    import java.util.List;
    
    public interface PedidoService {
    
    PedidoResponseDTO criarPedido(PedidoDTO dto);
    
    PedidoResponseDTO buscarPedidoPorId(Long id);
    
    List<PedidoResponseDTO> buscarPedidosPorCliente(Long clienteId);
    
    PedidoResponseDTO atualizarStatusPedido(Long id, StatusPedido status);
    
    BigDecimal calcularTotalPedido(List<ItemPedidoDTO> itens);
    
    void cancelarPedido(Long id);
}