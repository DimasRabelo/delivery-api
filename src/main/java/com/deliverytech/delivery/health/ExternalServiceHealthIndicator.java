package com.deliverytech.delivery.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch; 
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementação de HealthIndicator do Spring Boot Actuator.
 * Verifica a saúde de um serviço externo vital (ex: Gateway de Pagamento)
 * e mede o tempo de resposta.
 * <p>
 * Este indicador será exposto no endpoint '/actuator/health'.
 */
@Component("externalService")
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;

    /**
     * Injeta o RestTemplate configurado (com timeouts)
     * para fazer a checagem de saúde.
     *
     * @param restTemplate Bean RestTemplate gerenciado pelo Spring.
     */
    public ExternalServiceHealthIndicator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Executa a verificação de saúde.
     * Tenta acessar uma URL externa e mede o tempo de resposta.
     *
     * @return Health UP (com detalhes de tempo) ou DOWN (com detalhes do erro).
     */
    @Override
    public Health health() {
        // 1. Prepara o cronômetro e a URL de teste
        StopWatch stopWatch = new StopWatch();
        String url = "https://httpbin.org/status/200"; // URL de simulação
        
        try {
            // 2. Inicia o cronômetro e faz a chamada externa
            stopWatch.start();
            restTemplate.getForObject(url, String.class);
            stopWatch.stop();
            long responseTime = stopWatch.getTotalTimeMillis();

            // 3. Retorna 'UP' (Saudável) com detalhes
            return Health.up()
                    .withDetail("service", "Payment Gateway")
                    .withDetail("url", url)
                    .withDetail("status", "Disponível")
                    .withDetail("responseTime", responseTime + "ms")
                    .build();
                    
        } catch (RestClientException e) { 
            // 4. Em caso de erro (timeout, 500, etc.), para o cronômetro
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            
            // 5. Retorna 'DOWN' (Inoperante) com o erro
            return Health.down()
                    .withDetail("service", "Payment Gateway")
                    .withDetail("url", url)
                    .withDetail("status", "Indisponível")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}