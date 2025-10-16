package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.PedidoDTO;
import com.deliverytech.delivery.dto.PedidoResponseDTO;
import com.deliverytech.delivery.dto.ItemPedidoDTO;
import com.deliverytech.delivery.dto.StatusPedidoDTO;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // Criar novo pedido
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody PedidoDTO dto) {
        PedidoResponseDTO pedido = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    // Buscar pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);
        return ResponseEntity.ok(pedido);
    }

    // Listar pedidos por cliente
   @GetMapping("/clientes/{clienteId}/pedidos")
public ResponseEntity<List<PedidoResponseDTO>> buscarPorCliente(@PathVariable Long clienteId) {
    List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
    return ResponseEntity.ok(pedidos);
}

    // Atualizar status do pedido
    @PatchMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusPedidoDTO statusDTO) {

        // Converter String para enum StatusPedido
        StatusPedido novoStatus;
        try {
            novoStatus = StatusPedido.valueOf(statusDTO.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Status inválido: " + statusDTO.getStatus());
        }

        PedidoResponseDTO pedido = pedidoService.atualizarStatusPedido(id, novoStatus);
        return ResponseEntity.ok(pedido);
    }

    // Cancelar pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(@PathVariable Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    // Calcular total do pedido
    @PostMapping("/calcular")
    public ResponseEntity<BigDecimal> calcularTotal(@Valid @RequestBody List<ItemPedidoDTO> itens) {
        BigDecimal total = pedidoService.calcularTotalPedido(itens);
        return ResponseEntity.ok(total);
    }
}
