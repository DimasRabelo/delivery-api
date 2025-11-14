package com.deliverytech.delivery.controller; // Seu pacote de controller

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody; 
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit; 

import com.deliverytech.delivery.service.metrics.MetricsService;
import org.springframework.web.bind.annotation.PathVariable; 

/**
 * Controller para servir o Dashboard HTML
 * e a API de métricas que o alimenta.
 */
@Controller 
@RequestMapping("/dashboard")
public class DashboardController {

    private final MeterRegistry meterRegistry;
    private final MetricsService metricsService;

    // Injeta os serviços de métricas necessários
    public DashboardController(MeterRegistry meterRegistry, MetricsService metricsService) {
        this.meterRegistry = meterRegistry;
        this.metricsService = metricsService;
    }

    /**
     * Mapeamento para a página HTML.
     */
    @GetMapping
    public String dashboard() {
        return "dashboard"; // Retorna o nome do template HTML (ex: dashboard.html)
    }

    /**
     * Endpoint de API que o JavaScript do dashboard vai chamar
     * para buscar os dados de métricas.
     */
    @GetMapping("/api/metrics")
    @ResponseBody
    public Map<String, Object> getMetricsData() {
        Map<String, Object> metrics = new HashMap<>();

        // Busca os valores atuais das métricas registradas no MeterRegistry
        metrics.put("pedidos_total", getCounterValue("delivery.pedidos.total"));
        metrics.put("pedidos_sucesso", getCounterValue("delivery.pedidos.sucesso"));
        metrics.put("pedidos_erro", getCounterValue("delivery.pedidos.erro"));
        metrics.put("receita_total", getCounterValue("delivery.receita.total") / 100.0); // Converte centavos para Reais
        metrics.put("tempo_medio_pedido", getTimerMean("delivery.pedido.processamento.tempo"));
        metrics.put("tempo_medio_banco", getTimerMean("delivery.database.consulta.tempo"));
        metrics.put("usuarios_ativos", getGaugeValue("delivery.usuarios.ativos"));
        metrics.put("produtos_estoque", getGaugeValue("delivery.produtos.estoque"));
        
        return metrics;
    }
    
    /**
     * Endpoint de TESTE para simular usuários ativos.
     * Chama o MetricsService para atualizar o Gauge.
     */
    @GetMapping("/api/set-users/{count}")
    @ResponseBody
    public String setActiveUsers(
            @PathVariable("count") int count) {
        
        // Chama o método do serviço para atualizar o valor do Gauge
        metricsService.setUsuariosAtivos(count);
        
        return "TESTE: Usuários Ativos definido para: " + count;
    }

    // --- Métodos Auxiliares ---
    
    /**
     * Busca o valor de um contador (Counter) pelo nome.
     */
    private double getCounterValue(String name) {
        if (meterRegistry.find(name).counter() != null) {
            return meterRegistry.find(name).counter().count();
        }
        return 0.0;
    }

    /**
     * Busca o tempo médio (em milissegundos) de um Timer pelo nome.
     */
    private double getTimerMean(String name) {
        if (meterRegistry.find(name).timer() != null) {
            return meterRegistry.find(name).timer().mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }

    /**
     * Busca o valor atual de um medidor (Gauge) pelo nome.
     */
    private double getGaugeValue(String name) {
        if (meterRegistry.find(name).gauge() != null) {
            return meterRegistry.find(name).gauge().value();
        }
        return 0.0;
    }
}