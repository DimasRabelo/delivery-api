package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "Operações relacionadas aos pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // --------------------------------------------------------------------------
    // 🔹 CRIAR PEDIDO
    // --------------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente ou restaurante não encontrado"),
        @ApiResponse(responseCode = "409", description = "Produto indisponível")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> criarPedido(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do pedido a ser criado")
            PedidoDTO dto) {

        PedidoResponseDTO pedido = pedidoService.criarPedido(dto);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido criado com sucesso");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --------------------------------------------------------------------------
    // 🔹 BUSCAR POR ID
    // --------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Recupera um pedido específico com todos os detalhes")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {

        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido encontrado");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // 🔹 LISTAR PEDIDOS COM FILTROS
    // --------------------------------------------------------------------------
    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Lista pedidos com filtros opcionais e paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public ResponseEntity<PagedResponseWrapper<PedidoResponseDTO>> listar(
            @Parameter(description = "Status do pedido") @RequestParam(required = false) StatusPedido status,
            @Parameter(description = "Data inicial")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Parâmetros de paginação") Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidos(status, dataInicio, dataFim, pageable);
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // 🔹 ATUALIZAR STATUS
// --------------------------------------------------------------------------
@PatchMapping("/{id}/status")
@Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
    @ApiResponse(responseCode = "400", description = "Transição de status inválida")
})
public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> atualizarStatus(
        @Parameter(description = "ID do pedido") @PathVariable Long id,
        @Valid @RequestBody StatusPedidoDTO statusDTO) {

    try {
        // Converte a String recebida para o Enum
        StatusPedido novoStatus = StatusPedido.valueOf(statusDTO.getStatus().toUpperCase());

        PedidoResponseDTO pedido = pedidoService.atualizarStatusPedido(id, novoStatus);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Status atualizado com sucesso");

        return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
        // Caso o valor do status seja inválido
        ApiResponseWrapper<PedidoResponseDTO> errorResponse =
                new ApiResponseWrapper<>(false, null, "Status inválido: " + statusDTO.getStatus());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}


    // --------------------------------------------------------------------------
    // 🔹 CANCELAR PEDIDO
    // --------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido se possível")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pedido cancelado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "400", description = "Pedido não pode ser cancelado")
    })
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "ID do pedido") @PathVariable Long id) {

        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------------------
    // 🔹 HISTÓRICO DO CLIENTE
    // --------------------------------------------------------------------------
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Histórico do cliente", description = "Lista todos os pedidos de um cliente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorCliente(
            @Parameter(description = "ID do cliente") @PathVariable Long clienteId) {

        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Histórico recuperado com sucesso");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // 🔹 PEDIDOS POR RESTAURANTE
    // --------------------------------------------------------------------------
    @GetMapping("/restaurante/{restauranteId}")
    @Operation(summary = "Pedidos do restaurante", description = "Lista todos os pedidos de um restaurante")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedidos recuperados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorRestaurante(
            @Parameter(description = "ID do restaurante") @PathVariable Long restauranteId,
            @Parameter(description = "Status do pedido") @RequestParam(required = false) StatusPedido status) {

        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorRestaurante(restauranteId, status);
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Pedidos recuperados com sucesso");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // 🔹 CALCULAR TOTAL DO PEDIDO
    // --------------------------------------------------------------------------
    @PostMapping("/calcular")
    @Operation(summary = "Calcular total do pedido", description = "Calcula o total de um pedido sem salvá-lo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total calculado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<CalculoPedidoResponseDTO>> calcularTotal(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Itens para cálculo")
            CalculoPedidoDTO dto) {

        // TODO: Implementar cálculo real com base no CEP e regras de promoção, futuramente
        CalculoPedidoResponseDTO calculo = pedidoService.calcularTotalPedido(dto);

        ApiResponseWrapper<CalculoPedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, calculo, "Total calculado com sucesso");

        return ResponseEntity.ok(response);
    }
}
