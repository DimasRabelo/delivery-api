package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.relatorio.*;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter; 
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; 
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull; 
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.validation.annotation.Validated; 
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "7. Relatórios (Admin)", description = "Endpoints para relatórios do sistema. Requer role ADMIN.")
@Validated 
@SecurityRequirement(name = "bearerAuth") 
public class RelatorioController {

    private final RelatorioService relatorioService;

    /**
     * Construtor para injeção de dependência do RelatorioService.
     * @param relatorioService O serviço que gera os relatórios.
     */
    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/vendas-por-restaurante")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Relatório de vendas por restaurante (ADMIN)",
               description = "Retorna o total de vendas e pedidos por restaurante. Requer role ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos (ex: datas)"),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)")
    })
    public ResponseEntity<ApiResponseWrapper<List<RelatorioVendasDTO>>> vendasPorRestaurante(
            @Parameter(description = "Data de início (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            
            @Parameter(description = "Data de fim (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<RelatorioVendasDTO> relatorio = relatorioService.gerarRelatorioVendas(dataInicio, dataFim);
        String mensagem = "Relatório de vendas por restaurante gerado com sucesso";
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
    }

    @GetMapping("/produtos-mais-vendidos")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Produtos mais vendidos (ADMIN)",
               description = "Retorna os produtos com maior quantidade vendida. Requer role ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponseWrapper<List<RelatorioProdutosDTO>>> produtosMaisVendidos(
            @Parameter(description = "Data de início (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            
            @Parameter(description = "Data de fim (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<RelatorioProdutosDTO> relatorio = relatorioService.gerarRelatorioProdutos(dataInicio, dataFim);
        String mensagem = "Relatório de produtos mais vendidos gerado com sucesso";
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
    }

    @GetMapping("/clientes-ativos")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Clientes mais ativos (ADMIN)",
               description = "Lista os clientes que mais fizeram pedidos. Requer role ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponseWrapper<List<RelatorioClientesDTO>>> clientesMaisAtivos(
            @Parameter(description = "Data de início (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            
            @Parameter(description = "Data de fim (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<RelatorioClientesDTO> relatorio = relatorioService.gerarRelatorioClientes(dataInicio, dataFim);
        String mensagem = "Relatório de clientes mais ativos gerado com sucesso";
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
    }

    @GetMapping("/pedidos-por-periodo")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Pedidos por período (ADMIN)",
               description = "Retorna todos os pedidos realizados no período. Requer role ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
        @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponseWrapper<List<RelatorioPedidosDTO>>> pedidosPorPeriodo(
            @Parameter(description = "Data de início (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            
            @Parameter(description = "Data de fim (YYYY-MM-DD)", required = true)
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<RelatorioPedidosDTO> relatorio = relatorioService.gerarRelatorioPedidos(dataInicio, dataFim);
        String mensagem = "Relatório de pedidos por período gerado com sucesso";
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
    }
}