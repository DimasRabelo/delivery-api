package com.deliverytech.delivery.service.alert;

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
 * Serviço de alertas agendados (@Scheduled) para monitoramento de métricas e health checks.
 * 
 * - Usa métricas do Micrometer (MeterRegistry) para verificar taxa de erro e tempo de resposta.
 * - Usa HealthIndicator para monitorar Banco de Dados e serviços externos.
 * - Dispara alertas via logs ou integrações externas (Slack, PagerDuty, etc.).
 */
@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final MeterRegistry meterRegistry;
    private final HealthIndicator databaseHealthIndicator;
    private final HealthIndicator externalServiceHealthIndicator;

    // Thresholds (Limites) para alertas
    private static final double ERROR_RATE_THRESHOLD = 0.05;         // 5%
    private static final double RESPONSE_TIME_THRESHOLD_MS = 1000;   // 1000ms (1 segundo)

    @Autowired
    public AlertService(MeterRegistry meterRegistry,
                        @Qualifier("database") HealthIndicator databaseHealthIndicator,
                        @Qualifier("externalService") HealthIndicator externalServiceHealthIndicator) {
        this.meterRegistry = meterRegistry;
        this.databaseHealthIndicator = databaseHealthIndicator;
        this.externalServiceHealthIndicator = externalServiceHealthIndicator;
    }

    // ==========================================================
    // --- MÉTODO AGENDADO PARA VERIFICAÇÃO PERIÓDICA DE ALERTAS ---
    // ==========================================================
    @Scheduled(fixedRate = 30000) // Executa a cada 30 segundos
    public void verificarAlertas() {
        logger.info("[ALERTS] Verificando métricas e saúde do sistema...");
        verificarErrorRate();
        verificarResponseTime();
        verificarHealthStatus();
    }

    // ==========================================================
    // --- MÉTODOS PRIVADOS: LÓGICAS DE VERIFICAÇÃO ---
    // ==========================================================

    private void verificarErrorRate() {
        double totalRequests = getCounterValue("delivery.pedidos.total");
        double errorRequests = getCounterValue("delivery.pedidos.erro");

        if (totalRequests > 0) {
            double errorRate = errorRequests / totalRequests;
            if (errorRate > ERROR_RATE_THRESHOLD) {
                enviarAlerta("HIGH_ERROR_RATE",
                        String.format("Taxa de erro alta: %.2f%% (Limite: %.2f%%)",
                                errorRate * 100, ERROR_RATE_THRESHOLD * 100),
                        "CRITICAL");
            }
        }
    }

    private void verificarResponseTime() {
        double avgResponseTime = getTimerMean("delivery.pedido.processamento.tempo");

        if (avgResponseTime > RESPONSE_TIME_THRESHOLD_MS) {
            enviarAlerta("HIGH_RESPONSE_TIME",
                    String.format("Tempo de resposta alto: %.2fms (Limite: %.2fms)",
                            avgResponseTime, RESPONSE_TIME_THRESHOLD_MS),
                    "WARNING");
        }
    }

    private void verificarHealthStatus() {
        try {
            // 1. Banco de Dados
            Status dbStatus = databaseHealthIndicator.health().getStatus();
            if (!dbStatus.equals(Status.UP)) {
                enviarAlerta("DATABASE_DOWN",
                        "Banco de dados não está respondendo (Status: " + dbStatus + ")",
                        "CRITICAL");
            }

            // 2. Serviço Externo (Gateway de Pagamento)
            Status externalStatus = externalServiceHealthIndicator.health().getStatus();
            if (!externalStatus.equals(Status.UP)) {
                enviarAlerta("EXTERNAL_SERVICE_DOWN",
                        "Serviço externo não disponível (Status: " + externalStatus + ")",
                        "WARNING");
            }

        } catch (Exception e) {
            logger.error("[ALERTS] Erro ao verificar health status customizado", e);
        }
    }

    // ==========================================================
    // --- MÉTODOS AUXILIARES ---
    // ==========================================================

    private void enviarAlerta(String tipo, String mensagem, String severidade) {
        Map<String, Object> alerta = new HashMap<>();
        alerta.put("timestamp", System.currentTimeMillis());
        alerta.put("tipo", tipo);
        alerta.put("mensagem", mensagem);
        alerta.put("severidade", severidade);
        alerta.put("aplicacao", "delivery-api");

        logger.warn("ALERTA [{}] {}: {}", severidade, tipo, mensagem);

        // Aqui poderia ser enviada integração externa (Slack, PagerDuty, etc.)
    }

    private double getCounterValue(String name) {
        if (meterRegistry.find(name).counter() != null) {
            return meterRegistry.find(name).counter().count();
        }
        return 0.0;
    }

    private double getTimerMean(String name) {
        if (meterRegistry.find(name).timer() != null) {
            return meterRegistry.find(name).timer().mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }
}
