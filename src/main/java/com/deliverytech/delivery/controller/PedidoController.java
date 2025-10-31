package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.*;
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

/**
 * Controller responsável por todas as operações de gerenciamento de Pedidos.
 *
 * Expõe endpoints para criação, consulta, atualização de status e cancelamento
 * de pedidos, com regras de segurança granulares baseadas na role do usuário
 * (CLIENTE, RESTAURANTE, ADMIN) e na propriedade do pedido.
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "5. Pedidos", description = "Gerenciamento de pedidos e operações relacionadas")
@SecurityRequirement(name = "bearerAuth") // Aplica o cadeado a todos os endpoints
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * Cria um novo pedido no sistema.
     * Acesso restrito a usuários com a role 'CLIENTE'.
     *
     * @param dto DTO contendo os itens, clienteId e restauranteId do pedido.
     * @return ResponseEntity 201 (Created) com os dados do pedido criado.
     *
     * @implNote A lógica de segurança no {@link PedidoService#criarPedido(PedidoDTO)}
     * deve garantir que o 'clienteId' no DTO seja ignorado e substituído
     * pelo ID do usuário autenticado ({@code principal.id}), para evitar que um
     * cliente crie um pedido em nome de outro.
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Criar pedido (CLIENTE)",
               description = "Cria um novo pedido no sistema. Requer role 'CLIENTE'.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso",
                         content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
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
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um pedido específico pelo seu ID.
     * Acesso permitido para 'ADMIN' ou para usuários (CLIENTE/RESTAURANTE)
     * que tenham acesso ao pedido (verificado por {@code @pedidoService.canAccess}).
     *
     * @param id O ID (Long) do pedido.
     * @return ResponseEntity 200 (OK) com os dados do pedido.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Buscar pedido por ID (ADMIN ou Dono/Restaurante)",
               description = "Recupera um pedido específico. Requer ADMIN ou ser o cliente/restaurante do pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                         content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<PedidoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do pedido que você deseja buscar", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "O ID do pedido deve ser maior que zero") Long id) {

        PedidoResponseDTO pedido = pedidoService.buscarPedidoPorId(id);
        ApiResponseWrapper<PedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, pedido, "Pedido encontrado");
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os pedidos do sistema de forma paginada.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param status     (Opcional) Filtra pedidos pelo status.
     * @param dataInicio (Opcional) Filtra pedidos a partir desta data.
     * @param dataFim    (Opcional) Filtra pedidos até esta data.
     * @param pageable   Objeto de paginação.
     * @return ResponseEntity 200 (OK) com a página de pedidos.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os pedidos (ADMIN)",
               description = "Lista todos os pedidos com filtros e paginação. Requer role 'ADMIN'.")
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

            @Parameter(description = "Informações de paginação") Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidos(status, dataInicio, dataFim, pageable);
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista os pedidos do CLIENTE autenticado, de forma paginada.
     * Acesso restrito ao próprio cliente.
     *
     * @param pageable Objeto de paginação.
     * @return ResponseEntity 200 (OK) com a página de pedidos do cliente.
     */
    @GetMapping("/meus")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar meus pedidos (CLIENTE)",
               description = "Lista os pedidos do cliente autenticado com paginação. Requer role 'CLIENTE'.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é CLIENTE)")
    })
    public ResponseEntity<PagedResponseWrapper<PedidoResponseDTO>> listarMeusPedidos(
            @Parameter(description = "Informações de paginação") Pageable pageable) {

        Page<PedidoResponseDTO> pedidos = pedidoService.listarMeusPedidos(pageable);
        
        PagedResponseWrapper<PedidoResponseDTO> response = new PagedResponseWrapper<>(pedidos);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza o status de um pedido (ex: PENDENTE -> EM_PREPARO).
     * Acesso permitido para 'ADMIN' ou para usuários (CLIENTE/RESTAURANTE)
     * que tenham acesso ao pedido.
     *
     * @param id        O ID do pedido a ser atualizado.
     * @param statusDTO DTO contendo o novo status (String).
     * @return ResponseEntity 200 (OK) com os dados do pedido atualizado.
     *
     * @implNote A lógica de *quais* transições de status são permitidas para
     * *qual* usuário (CLIENTE vs RESTAURANTE) deve ser implementada
     * dentro do {@link PedidoService#atualizarStatusPedido(Long, StatusPedido)}.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Atualizar status do pedido (ADMIN ou Dono/Restaurante)",
               description = "Atualiza o status de um pedido (ex: EM_PREPARO, ENTREGUE, CANCELADO).")
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

            @Parameter(description = "Novo status do pedido", required = true,
                       content = @Content(schema = @Schema(implementation = StatusPedidoDTO.class)))
            @Valid @RequestBody StatusPedidoDTO statusDTO) {

        // Validação manual para converter a String do DTO para o Enum
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

    /**
     * Cancela um pedido.
     * Acesso permitido para 'ADMIN' ou para usuários (CLIENTE/RESTAURANTE)
     * que tenham acesso ao pedido.
     *
     * @param id O ID do pedido a ser cancelado.
     * @return ResponseEntity 204 (No Content).
     *
     * @implNote A lógica de negócio no {@link PedidoService#cancelarPedido(Long)}
     * deve verificar se o pedido *ainda pode* ser cancelado
     * (ex: se o status ainda for 'PENDENTE' ou 'EM_PREPARO').
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @pedidoService.canAccess(#id)")
    @Operation(summary = "Cancelar pedido (ADMIN ou Dono/Restaurante)",
               description = "Cancela um pedido. Requer ADMIN ou ser o cliente/restaurante do pedido.")
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

    /**
     * Busca o histórico de pedidos de um cliente específico.
     * Acesso permitido para 'ADMIN' ou para o próprio 'CLIENTE'.
     *
     * @param clienteId O ID do cliente.
     * @return ResponseEntity 200 (OK) com a lista de pedidos do cliente.
     */
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and #clienteId == principal.id)")
    @Operation(summary = "Histórico de pedidos do cliente (ADMIN ou Dono)",
               description = "Retorna todos os pedidos de um cliente. Requer ADMIN ou ser o próprio cliente.")
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
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Histórico recuperado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Busca todos os pedidos de um restaurante específico.
     * Acesso permitido para 'ADMIN' ou para o 'RESTAURANTE' proprietário.
     *
     * @param restauranteId O ID do restaurante.
     * @param status        (Opcional) Filtra os pedidos do restaurante por status.
     * @return ResponseEntity 200 (OK) com a lista de pedidos do restaurante.
     */
    @GetMapping("/restaurante/{restauranteId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('RESTAURANTE') and #restauranteId == principal.restauranteId)")
    @Operation(summary = "Pedidos por restaurante (ADMIN ou Dono)",
               description = "Lista todos os pedidos de um restaurante. Requer ADMIN ou ser o dono do restaurante.")
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
        ApiResponseWrapper<List<PedidoResponseDTO>> response =
                new ApiResponseWrapper<>(true, pedidos, "Pedidos recuperados com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Calcula o total de um pedido (subtotal, taxa de entrega, total)
     * sem salvá-lo no banco de dados. Endpoint público.
     *
     * @param dto DTO contendo os itens e o CEP de entrega para o cálculo.
     * @return ResponseEntity 200 (OK) com os valores calculados.
     */
    @PostMapping("/calcular")
    @Operation(summary = "Calcular total do pedido (Público)",
               description = "Calcula o total de um pedido (itens + taxa) sem salvar no banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculado com sucesso",
                         content = @Content(schema = @Schema(implementation = CalculoPedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: CEP)"),
            @ApiResponse(responseCode = "404", description = "Restaurante ou produto não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth", scopes = {}) // Remove o cadeado apenas para este endpoint
    public ResponseEntity<ApiResponseWrapper<CalculoPedidoResponseDTO>> calcularTotal(
            @Parameter(description = "Itens do pedido para cálculo", required = true)
            @Valid @RequestBody CalculoPedidoDTO dto) {

        CalculoPedidoResponseDTO calculo = pedidoService.calcularTotalPedido(dto);
        ApiResponseWrapper<CalculoPedidoResponseDTO> response =
                new ApiResponseWrapper<>(true, calculo, "Total calculado com sucesso");
        return ResponseEntity.ok(response);
    }
}