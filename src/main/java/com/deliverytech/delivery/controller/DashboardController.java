package com.deliverytech.delivery.controller; // Seu pacote de controller

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Controller; // ❗️ Note: @Controller, não @RestController
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody; // Para o método da API

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit; // Import para o TimeUnit

/**
 * Controller para servir o Dashboard HTML
 * e a API de métricas que o alimenta.
 */
@Controller // @Controller normal (para servir HTML)
@RequestMapping("/dashboard")
public class DashboardController {

    private final MeterRegistry meterRegistry;

    public DashboardController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Mapeamento para a página HTML.
     * Retorna o nome do template: "dashboard"
     * O Spring vai procurar por: /resources/templates/dashboard.html
     */
    @GetMapping
    public String dashboard() {
        return "dashboard"; // Retorna o nome do arquivo HTML
    }

    /**
     * Endpoint de API que o JavaScript do dashboard vai chamar.
     * @ResponseBody diz ao Spring para retornar JSON, não um HTML.
     */
    @GetMapping("/api/metrics")
    @ResponseBody
    public Map<String, Object> getMetricsData() {
        Map<String, Object> metrics = new HashMap<>();

        // Métricas de pedidos (da Atividade 2)
        metrics.put("pedidos_total", getCounterValue("delivery.pedidos.total"));
        metrics.put("pedidos_sucesso", getCounterValue("delivery.pedidos.sucesso"));
        metrics.put("pedidos_erro", getCounterValue("delivery.pedidos.erro"));
        // Converte centavos para Reais
        metrics.put("receita_total", getCounterValue("delivery.receita.total") / 100.0);

        // Métricas de performance (da Atividade 2)
        metrics.put("tempo_medio_pedido", getTimerMean("delivery.pedido.processamento.tempo"));
        metrics.put("tempo_medio_banco", getTimerMean("delivery.database.consulta.tempo"));

        // Métricas de estado (da Atividade 2)
        metrics.put("usuarios_ativos", getGaugeValue("delivery.usuarios.ativos"));
        metrics.put("produtos_estoque", getGaugeValue("delivery.produtos.estoque"));

        // (O gabarito não incluía as métricas de CPU/Memória, 
        // mas você pode adicioná-las aqui se quiser)
        
        return metrics;
    }

    // ==========================================================
    // MÉTODOS AUXILIARES (do gabarito - os mesmos do AlertService)
    // ==========================================================
    
    private double getCounterValue(String name) {
        if (meterRegistry.find(name).counter() != null) {
            return meterRegistry.find(name).counter().count();
        }
        return 0.0;
    }

    private double getTimerMean(String name) {
        if (meterRegistry.find(name).timer() != null) {
            // Converte para Milissegundos
            return meterRegistry.find(name).timer().mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }

    private double getGaugeValue(String name) {
        if (meterRegistry.find(name).gauge() != null) {
            return meterRegistry.find(name).gauge().value();
        }
        return 0.0;
    }
}