package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.PedidoDTO;
import com.deliverytech.delivery.dto.request.StatusPedidoDTO;
import com.deliverytech.delivery.dto.response.*;
import com.deliverytech.delivery.enums.StatusPedido;
import com.deliverytech.delivery.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "5. Pedidos", description = "Gerenciamento de pedidos e operações relacionadas")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Criar pedido (CLIENTE)", description = "Cria um novo pedido no sistema. Requer role 'CLIENTE'.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso", content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: itens vazios)"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é CLIENTE)"),
            @ApiResponse(responseCode = "404", description = "Cliente ou restaurante não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto indisponível ou restaurante fechado")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> criarPedido(
            @Parameter(description = "Informações do pedido a ser criado", required = true)
            @Valid @RequestBody PedidoDTO dto) {
        PedidoResponseDTO pedido = pedidoService.criarPedido(dto);
        ApiResponseWrapper<PedidoResponseDTO> response = new ApiResponseWrapper<>(true, pedido, "Pedido criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Buscar pedido por ID (ADMIN ou Dono/Restaurante)", description = "Recupera um pedido específico. Requer ADMIN ou ser o cliente/restaurante do pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado", content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do pedido que você deseja buscar", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);
        ApiResponseWrapper<PedidoResponseDTO> response = new ApiResponseWrapper<>(true, pedido, "Pedido encontrado");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os pedidos (ADMIN)", description = "Lista todos os pedidos com filtros e paginação. Requer role 'ADMIN'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)")
    })
    public ResponseEntity<PagedResponseWrapper<PedidoResponseDTO>> listar(
            @Parameter(description = "Filtra pedidos pelo status", example = "EM_PREPARO")
            @RequestParam(required = false) StatusPedido status,
            @Parameter(description = "Data inicial do filtro (YYYY-MM-DD)", example = "2025-10-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do filtro (YYYY-MM-DD)", example = "2025-10-23")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @ParameterObject Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidos(status, dataInicio, dataFim, pageable);
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar meus pedidos (CLIENTE)", description = "Lista os pedidos do cliente autenticado com paginação. Requer role 'CLIENTE'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é CLIENTE)")
    })
    public ResponseEntity<PagedResponseWrapper<PedidoResponseDTO>> listarMeusPedidos(
            @ParameterObject Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarMeusPedidos(pageable);
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Atualizar status do pedido (ADMIN ou Dono/Restaurante)", description = "Atualiza o status de um pedido (ex: EM_PREPARO, ENTREGUE, CANCELADO).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status inválido ou transição não permitida"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> atualizarStatus(
            @Parameter(description = "ID do pedido que será atualizado", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id,

            @Parameter(description = "Novo status e (opcionalmente) ID do entregador", required = true, content = @Content(schema = @Schema(implementation = StatusPedidoDTO.class)))
            @Valid @RequestBody StatusPedidoDTO statusDTO) {

        // Passa o DTO inteiro para o Service, que contém a lógica de transição e atribuição
        PedidoResponseDTO pedido = pedidoService.atualizarStatusPedido(id, statusDTO);

        ApiResponseWrapper<PedidoResponseDTO> response = new ApiResponseWrapper<>(true, pedido, "Status atualizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Cancelar pedido (ADMIN ou Dono/Restaurante)", description = "Cancela um pedido. Requer ADMIN ou ser o cliente/restaurante do pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido cancelado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado"),
            @ApiResponse(responseCode = "409", description = "Não é mais possível cancelar este pedido")
    })
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "ID do pedido que será cancelado", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {
        pedidoService.cancelarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and #clienteId == principal.id)")
    @Operation(summary = "Histórico de pedidos do cliente (ADMIN ou Dono)", description = "Retorna todos os pedidos de um cliente. Requer ADMIN ou ser o próprio cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico recuperado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou o dono da conta)"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorCliente(
            @Parameter(description = "ID do cliente para consulta", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do cliente deve ser maior que zero") Long clienteId) {
        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorCliente(clienteId);
        ApiResponseWrapper<List<PedidoResponseDTO>> response = new ApiResponseWrapper<>(true, pedidos, "Histórico recuperado com sucesso");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/restaurante/{restauranteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANTE') and #restauranteId == principal.restauranteId)")
    @Operation(summary = "Pedidos por restaurante (ADMIN ou Dono)", description = "Lista todos os pedidos de um restaurante. Requer ADMIN ou ser o dono do restaurante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos recuperados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou o dono do restaurante)"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<PedidoResponseDTO>>> buscarPorRestaurante(
            @Parameter(description = "ID do restaurante para consulta", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do restaurante deve ser maior que zero") Long restauranteId,
            @Parameter(description = "Filtra pedidos pelo status", example = "ENTREGUE")
            @RequestParam(required = false) StatusPedido status) {
        List<PedidoResponseDTO> pedidos = pedidoService.buscarPedidosPorRestaurante(restauranteId, status);
        ApiResponseWrapper<List<PedidoResponseDTO>> response = new ApiResponseWrapper<>(true, pedidos, "Pedidos recuperados com sucesso");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calcular")
    @Operation(summary = "Calcular total do pedido (Público)", description = "Calcula o total de um pedido (itens + taxa) sem salvar no banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado com sucesso", content = @Content(schema = @Schema(implementation = CalculoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: CEP)"),
            @ApiResponse(responseCode = "404", description = "Restaurante ou produto não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth", scopes = {})
    public ResponseEntity<ApiResponseWrapper<CalculoPedidoResponseDTO>> calcularTotal(
            @Parameter(description = "Itens do pedido para cálculo", required = true)
            @Valid @RequestBody CalculoPedidoDTO dto) {
        
        CalculoPedidoResponseDTO calculo = pedidoService.calcularTotalPedido(dto);
        
        // Agora sim, com o nome correto: CalculoPedidoResponseDTO
        ApiResponseWrapper<CalculoPedidoResponseDTO> response = new ApiResponseWrapper<>(true, calculo, "Total calculado com sucesso");
        
        return ResponseEntity.ok(response);
    }
}