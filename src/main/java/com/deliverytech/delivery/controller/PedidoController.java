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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos e operações relacionadas")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    // --------------------------------------------------------------------------
    // CRIAR PEDIDO - CLIENTE
    // --------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido no sistema com os itens e informações do cliente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente ou restaurante não encontrado"),
        @ApiResponse(responseCode = "409", description = "Produto indisponível")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> criarPedido(
            @Parameter(description = "Informações do pedido a ser criado", required = true)
            @Valid @RequestBody PedidoDTO dto) {

        PedidoResponseDTO pedido = pedidoService.criarPedido(dto);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --------------------------------------------------------------------------
    // BUSCAR PEDIDO POR ID - ADMIN OU ACESSO PERMITIDO
    // --------------------------------------------------------------------------
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Buscar pedido por ID", description = "Recupera um pedido específico e seus detalhes.")
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do pedido que você deseja buscar", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {

        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido encontrado");
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // LISTAR PEDIDOS - ADMIN
    // --------------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar pedidos", description = "Lista pedidos com filtros opcionais de status e data, com suporte à paginação.")
    public ResponseEntity<PagedResponseWrapper<PedidoResponseDTO>> listar(
            @Parameter(description = "Filtra pedidos pelo status", example = "EM_PREPARO")
            @RequestParam(required = false) StatusPedido status,

            @Parameter(description = "Data inicial do filtro (YYYY-MM-DD)", example = "2025-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,

            @Parameter(description = "Data final do filtro (YYYY-MM-DD)", example = "2025-10-23")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,

            @Parameter(description = "Informações de paginação, como número da página e tamanho") Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidos(status, dataInicio, dataFim, pageable);
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // ATUALIZAR STATUS DO PEDIDO - ADMIN OU PROPRIEDADE
    // --------------------------------------------------------------------------
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido (ex: EM_PREPARO, ENTREGUE, CANCELADO).")
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> atualizarStatus(
            @Parameter(description = "ID do pedido que será atualizado", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id,

            @Parameter(description = "Novo status do pedido", required = true, example = "ENTREGUE")
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
    // CANCELAR PEDIDO - ADMIN OU CLIENTE PROPRIETÁRIO
    // --------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Cancelar pedido", description = "Cancela um pedido caso ainda esteja em um status permitido para cancelamento.")
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "ID do pedido que será cancelado", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {

        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------------------
    // HISTÓRICO DE PEDIDOS DO CLIENTE - ADMIN OU CLIENTE
    // --------------------------------------------------------------------------
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and #clienteId == principal.id)")
    @Operation(summary = "Histórico do cliente", description = "Retorna todos os pedidos realizados por um cliente.")
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorCliente(
            @Parameter(description = "ID do cliente para o qual deseja consultar o histórico", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do cliente deve ser maior que zero") Long clienteId) {

        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Histórico recuperado com sucesso");
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // PEDIDOS POR RESTAURANTE - ADMIN OU RESTAURANTE
    // --------------------------------------------------------------------------
    @GetMapping("/restaurante/{restauranteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANTE') and #restauranteId == principal.restauranteId)")
    @Operation(summary = "Pedidos do restaurante", description = "Lista todos os pedidos de um restaurante, com filtro opcional por status.")
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorRestaurante(
            @Parameter(description = "ID do restaurante para o qual deseja consultar pedidos", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do restaurante deve ser maior que zero") Long restauranteId,

            @Parameter(description = "Filtra pedidos pelo status", example = "ENTREGUE")
            @RequestParam(required = false) StatusPedido status) {

        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorRestaurante(restauranteId, status);
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Pedidos recuperados com sucesso");
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // CALCULAR TOTAL DO PEDIDO - PODE SER PÚBLICO
    // --------------------------------------------------------------------------
    @PostMapping("/calcular")
    @Operation(summary = "Calcular total do pedido", description = "Realiza o cálculo total de um pedido com base nos itens enviados, sem salvar no banco de dados.")
    public ResponseEntity<ApiResponseWrapper<CalculoPedidoResponseDTO>> calcularTotal(
            @Parameter(description = "Itens do pedido para cálculo do total", required = true)
            @Valid @RequestBody CalculoPedidoDTO dto) {

        CalculoPedidoResponseDTO calculo = pedidoService.calcularTotalPedido(dto);
        ApiResponseWrapper<CalculoPedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, calculo, "Total calculado com sucesso");
        return ResponseEntity.ok(response);
    }
}
