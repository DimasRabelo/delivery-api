package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.relatorio.*;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.service.RelatorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para relatórios do sistema")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    // ======================================================
    // Vendas por Restaurante
    // ======================================================
    @GetMapping("/vendas-por-restaurante")
    @Operation(summary = "Relatório de vendas por restaurante",
               description = "Retorna o total de vendas e quantidade de pedidos por restaurante")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Nenhum pedido encontrado no período informado"),
        @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    public ResponseEntity<ApiResponseWrapper<List<RelatorioVendasDTO>>> vendasPorRestaurante(
            @RequestParam("dataInicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("dataFim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<RelatorioVendasDTO> relatorio = relatorioService.gerarRelatorioVendas(inicio, fim);
      // Definindo a mensagem conforme se a lista está vazia ou não
    String mensagem = relatorio.isEmpty()
            ? "Nenhum produto vendido no período informado"
            : "Relatório de produtos gerado com sucesso";

    return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
}

    // ======================================================
    // Produtos mais vendidos
    // ======================================================
    @GetMapping("/produtos-mais-vendidos")
@Operation(summary = "Produtos mais vendidos",
           description = "Retorna os produtos com maior quantidade vendida no período")
public ResponseEntity<ApiResponseWrapper<List<RelatorioProdutosDTO>>> produtosMaisVendidos(
        @RequestParam("dataInicio")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
        @RequestParam("dataFim")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

    List<RelatorioProdutosDTO> relatorio = relatorioService.gerarRelatorioProdutos(inicio, fim);

    // Definindo a mensagem conforme se a lista está vazia ou não
    String mensagem = relatorio.isEmpty()
            ? "Nenhum produto vendido no período informado"
            : "Relatório de produtos gerado com sucesso";

    return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
}

    // ======================================================
    // Clientes mais ativos
    // ======================================================
    @GetMapping("/clientes-ativos")
    @Operation(summary = "Clientes mais ativos",
               description = "Lista os clientes que mais fizeram pedidos no período")
    public ResponseEntity<ApiResponseWrapper<List<RelatorioClientesDTO>>> clientesMaisAtivos(
            @RequestParam("dataInicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("dataFim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<RelatorioClientesDTO> relatorio = relatorioService.gerarRelatorioClientes(inicio, fim);
        // Definindo a mensagem conforme se a lista está vazia ou não
    String mensagem = relatorio.isEmpty()
            ? "Nenhum Cliente ativo no período informado"
            : "Relatório de produtos gerado com sucesso";

    return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
}

    // ======================================================
    // Pedidos por período
    // ======================================================
    @GetMapping("/pedidos-por-periodo")
    @Operation(summary = "Pedidos por período",
               description = "Retorna todos os pedidos realizados no período especificado")
    public ResponseEntity<ApiResponseWrapper<List<RelatorioPedidosDTO>>> pedidosPorPeriodo(
            @RequestParam("dataInicio")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam("dataFim")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        List<RelatorioPedidosDTO> relatorio = relatorioService.gerarRelatorioPedidos(inicio, fim);
       // Definindo a mensagem conforme se a lista está vazia ou não
    String mensagem = relatorio.isEmpty()
            ? "Nenhum Pedido no período informado"
            : "Relatório de produtos gerado com sucesso";

    return ResponseEntity.ok(new ApiResponseWrapper<>(true, relatorio, mensagem));
}
}
