package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.*;
import com.deliverytech.delivery.dto.response.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador responsável por gerenciar operações relacionadas aos pedidos.
 * Inclui criação, listagem, atualização de status, cancelamento e cálculos.
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Validated // Ativa validação também em parâmetros de URL e query params
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos e operações relacionadas")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // --------------------------------------------------------------------------
    // CRIAR PEDIDO
    // --------------------------------------------------------------------------
    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido no sistema com os itens e informações do cliente.")
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
    // BUSCAR PEDIDO POR ID
    // --------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID", description = "Recupera um pedido específico e seus detalhes.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do pedido") 
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {

        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido encontrado");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // LISTAR PEDIDOS COM FILTROS E PAGINAÇÃO
    // --------------------------------------------------------------------------
    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Lista pedidos com filtros opcionais de status e data, com suporte à paginação.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public ResponseEntity<PagedResponseWrapper<PedidoResponseDTO>> listar(
            @Parameter(description = "Status do pedido") @RequestParam(required = false) StatusPedido status,
            @Parameter(description = "Data inicial do filtro")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do filtro")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @Parameter(description = "Parâmetros de paginação") Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidos(status, dataInicio, dataFim, pageable);
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // ATUALIZAR STATUS DO PEDIDO
    // --------------------------------------------------------------------------
    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido (exemplo: EM_PREPARO, ENTREGUE, CANCELADO).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "400", description = "Transição de status inválida")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> atualizarStatus(
            @Parameter(description = "ID do pedido") 
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id,
            @Valid @RequestBody StatusPedidoDTO statusDTO) {

        StatusPedido novoStatus;
        try {
            novoStatus = StatusPedido.valueOf(statusDTO.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            ApiResponseWrapper<PedidoResponseDTO> errorResponse =
                    new ApiResponseWrapper<>(false, null, "Status inválido: " + statusDTO.getStatus());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        PedidoResponseDTO pedido = pedidoService.atualizarStatusPedido(id, novoStatus);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Status atualizado com sucesso");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // CANCELAR PEDIDO
    // --------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido caso ainda esteja em um status permitido para cancelamento.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Pedido cancelado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
        @ApiResponse(responseCode = "400", description = "Pedido não pode ser cancelado")
    })
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "ID do pedido") 
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {

        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------------------
    // HISTÓRICO DE PEDIDOS DO CLIENTE
    // --------------------------------------------------------------------------
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Histórico do cliente", description = "Retorna todos os pedidos realizados por um cliente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorCliente(
            @Parameter(description = "ID do cliente") 
            @PathVariable @Min(value = 1, message = "O ID do cliente deve ser maior que zero") Long clienteId) {

        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Histórico recuperado com sucesso");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // PEDIDOS POR RESTAURANTE
    // --------------------------------------------------------------------------
    @GetMapping("/restaurante/{restauranteId}")
    @Operation(summary = "Pedidos do restaurante", description = "Lista todos os pedidos de um restaurante, com filtro opcional por status.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Pedidos recuperados com sucesso"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorRestaurante(
            @Parameter(description = "ID do restaurante") 
            @PathVariable @Min(value = 1, message = "O ID do restaurante deve ser maior que zero") Long restauranteId,
            @Parameter(description = "Status do pedido") @RequestParam(required = false) StatusPedido status) {

        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorRestaurante(restauranteId, status);
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Pedidos recuperados com sucesso");

        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // CALCULAR TOTAL DO PEDIDO
    // --------------------------------------------------------------------------
    @PostMapping("/calcular")
    @Operation(summary = "Calcular total do pedido", description = "Realiza o cálculo total de um pedido com base nos itens enviados, sem salvar no banco de dados.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Total calculado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<CalculoPedidoResponseDTO>> calcularTotal(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Itens e quantidades para cálculo")
            CalculoPedidoDTO dto) {

        CalculoPedidoResponseDTO calculo = pedidoService.calcularTotalPedido(dto);
        ApiResponseWrapper<CalculoPedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, calculo, "Total calculado com sucesso");

        return ResponseEntity.ok(response);
    }
}
