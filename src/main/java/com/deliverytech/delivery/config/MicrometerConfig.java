package com.deliverytech.delivery.config; 

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração central do Micrometer.
 * Define tags comuns para todas as métricas e filtros.
 */
@Configuration
public class MicrometerConfig {

    /**
     * Este Bean personaliza o MeterRegistry (o "chefão" do Micrometer).
     * @return Um customizador que será aplicado pelo Spring Boot.
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                    // 1. Adiciona tags globais a TODAS as métricas
                    .commonTags("application", "delivery-api") // Nome da sua app
                    .commonTags("environment", "development") // Ambiente
                    .commonTags("version", "1.0.0") // Versão (do gabarito)
                    
                    // 2. Filtra (ignora) métricas desnecessárias
                    .meterFilter(MeterFilter.deny(id -> {
                        // Ignora métricas sobre os próprios endpoints do Actuator
                        String uri = id.getTag("uri");
                        return uri != null && uri.startsWith("/actuator");
                    }));
        };
    }
}