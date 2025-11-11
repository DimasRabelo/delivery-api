package com.deliverytech.delivery.service.alert; // Seu novo pacote

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Serviço agendado (@Scheduled) que verifica métricas e health checks
 * periodicamente para disparar alertas.
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    private final MeterRegistry meterRegistry;

    // ⬇️ CORREÇÃO DA "PEGADINHA": Injetando nossos HealthChecks da Atividade 1 ⬇️
    private final HealthIndicator databaseHealthIndicator;
    private final HealthIndicator externalServiceHealthIndicator;

    // Thresholds (Limites) para Alertas (do gabarito)
    private static final double ERROR_RATE_THRESHOLD = 0.05; // 5%
    private static final double RESPONSE_TIME_THRESHOLD_MS = 1000; // 1000ms (1 segundo)

    @Autowired
    public AlertService(MeterRegistry meterRegistry,
                        @Qualifier("database") HealthIndicator databaseHealthIndicator,
                        @Qualifier("externalService") HealthIndicator externalServiceHealthIndicator) {
        this.meterRegistry = meterRegistry;
        this.databaseHealthIndicator = databaseHealthIndicator;
        this.externalServiceHealthIndicator = externalServiceHealthIndicator;
    }

    /**
     * Roda a cada 30 segundos para verificar a saúde do sistema.
     */
    @Scheduled(fixedRate = 30000) // 30.000 milissegundos = 30 segundos
    public void verificarAlertas() {
        logger.info("[ALERTS] Verificando métricas e saúde do sistema...");
        verificarErrorRate();
        verificarResponseTime();
        verificarHealthStatus();
    }

    /**
     * Verifica a taxa de erro dos pedidos (Métricas da Atividade 2)
     */
    private void verificarErrorRate() {
        // Usamos os métodos auxiliares (getCounterValue) para evitar NPE
        double totalRequests = getCounterValue("delivery.pedidos.total");
        double errorRequests = getCounterValue("delivery.pedidos.erro");

        if (totalRequests > 0) { // Evita divisão por zero
            double errorRate = errorRequests / totalRequests;
            if (errorRate > ERROR_RATE_THRESHOLD) {
                enviarAlerta("HIGH_ERROR_RATE",
                        String.format("Taxa de erro alta: %.2f%% (Limite: %.2f%%)", 
                                errorRate * 100, ERROR_RATE_THRESHOLD * 100),
                        "CRITICAL");
            }
        }
    }

    /**
     * Verifica o tempo médio de resposta dos pedidos (Métricas da Atividade 2)
     */
    private void verificarResponseTime() {
        double avgResponseTime = getTimerMean("delivery.pedido.processamento.tempo");

        if (avgResponseTime > RESPONSE_TIME_THRESHOLD_MS) {
            enviarAlerta("HIGH_RESPONSE_TIME",
                    String.format("Tempo de resposta alto: %.2fms (Limite: %.2fms)", 
                            avgResponseTime, RESPONSE_TIME_THRESHOLD_MS),
                    "WARNING");
        }
    }

    /**
     * Verifica os Health Checks customizados (Saúde da Atividade 1)
     */
    private void verificarHealthStatus() {
        try {
            // ⬇️ CORREÇÃO DA "PEGADINHA": Verificando os beans reais ⬇️
            
            // 1. Verifica o Banco de Dados
            Status dbStatus = databaseHealthIndicator.health().getStatus();
            if (!dbStatus.equals(Status.UP)) {
                enviarAlerta("DATABASE_DOWN",
                        "Banco de dados não está respondendo (Status: " + dbStatus + ")",
                        "CRITICAL");
            }

            // 2. Verifica o Serviço Externo
            Status externalStatus = externalServiceHealthIndicator.health().getStatus();
            if (!externalStatus.equals(Status.UP)) {
                enviarAlerta("EXTERNAL_SERVICE_DOWN",
                        "Serviço externo (Gateway de Pagamento) não está disponível (Status: " + externalStatus + ")",
                        "WARNING");
            }

        } catch (Exception e) {
            logger.error("[ALERTS] Erro ao verificar health status customizado", e);
        }
    }

    // ==========================================================
    // MÉTODOS AUXILIARES (do gabarito - corretos)
    // ==========================================================

    private void enviarAlerta(String tipo, String mensagem, String severidade) {
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("timestamp", System.currentTimeMillis());
        alerta.put("tipo", tipo);
        alerta.put("mensagem", mensagem);
        alerta.put("severidade", severidade);
        alerta.put("aplicacao", "delivery-api");

        // Loga o alerta como WARN para se destacar no console
        logger.warn("ALERTA [{}] {}: {}", severidade, tipo, mensagem);
        
        // (Aqui entraria a integração com Slack, PagerDuty, etc.)
    }

    // Método seguro para pegar um contador (evita NullPointerException)
    private double getCounterValue(String name) {
        if (meterRegistry.find(name).counter() != null) {
            return meterRegistry.find(name).counter().count();
        }
        return 0.0;
    }

    // Método seguro para pegar a média de um Timer (em Milissegundos)
    private double getTimerMean(String name) {
        if (meterRegistry.find(name).timer() != null) {
            return meterRegistry.find(name).timer().mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }
}