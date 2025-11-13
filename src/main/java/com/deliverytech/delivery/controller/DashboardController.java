package com.deliverytech.delivery.controller;



import com.deliverytech.delivery.dto.response.ApiResponseWrapper;

import com.deliverytech.delivery.service.RelatorioService; // Usaremos este serviço

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



import java.math.BigDecimal;

import java.util.HashMap;

import java.util.Map;



@RestController

@RequestMapping("/api/dashboard")

@Tag(name = "6. Dashboard Admin", description = "Métricas e visões consolidadas da plataforma")

@SecurityRequirement(name = "bearerAuth")

public class DashboardController {



@Autowired

private RelatorioService relatorioService;



@GetMapping("/metrics")

@PreAuthorize("hasRole('ADMIN')") // Proteção obrigatória

public ResponseEntity<ApiResponseWrapper<Map<String, Object>>> getPlatformMetrics() {


// --- CHAMA O SERVIÇO DE RELATÓRIO (Onde o COUNT(*) está) ---

Long totalUsuarios = relatorioService.contarTotalUsuarios();

Long totalRestaurantes = relatorioService.contarTotalRestaurantes();

BigDecimal faturamento = relatorioService.calcularVendasUltimos30Dias();


// --- MONTAGEM DA RESPOSTA ---

Map<String, Object> metrics = new HashMap<>();


// Colocamos os valores REAIS do banco

metrics.put("total_usuarios", totalUsuarios); // AGORA VAI SER 6

metrics.put("total_restaurantes", totalRestaurantes); // AGORA VAI SER 2

metrics.put("faturamento_mensal", faturamento.toPlainString());

metrics.put("status_saude", "OK (Relatórios Ativos)");



ApiResponseWrapper<Map<String, Object>> response = new ApiResponseWrapper<>(true, metrics, "Métricas carregadas com sucesso");

return ResponseEntity.ok(response);

}

}