package com.deliverytech.delivery.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch; // Para o cronômetro
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate; // O Bean que acabamos de criar

@Component("externalService")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    // ⬇️ CORREÇÃO 1: Injetando o Bean
    private final RestTemplate restTemplate;

    public ExternalServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Health health() {
        // ⬇️ CORREÇÃO 2: Medindo o tempo real
        StopWatch stopWatch = new StopWatch();
        String url = "https://httpbin.org/status/200";
        
        try {
            stopWatch.start();
            restTemplate.getForObject(url, String.class);
            stopWatch.stop();
            long responseTime = stopWatch.getTotalTimeMillis();

            return Health.up()
                    .withDetail("service", "Payment Gateway")
                    .withDetail("url", url)
                    .withDetail("status", "Disponível")
                    .withDetail("responseTime", responseTime + "ms") // Tempo real
                    .build();
                    
        } catch (RestClientException e) { // ⬇️ CORREÇÃO 3: Captura específica
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            return Health.down()
                    .withDetail("service", "Payment Gateway")
                    .withDetail("url", url)
                    .withDetail("status", "Indisponível")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}