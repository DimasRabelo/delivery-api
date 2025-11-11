package com.deliverytech.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Classe principal (Main Class) e ponto de entrada
 * da aplicação Spring Boot.
 * <p>
 * Esta classe inicializa todo o contexto do Spring.
 */
@SpringBootApplication
@EnableScheduling // Habilita o scheduler do Spring (para tarefas agendadas, ex: @Scheduled)
@EnableCaching // Habilita o suporte a Cache (ex: @Cacheable, @CacheEvict)
public class DeliveryApiApplication {

    /**
     * Método principal (main) que inicializa a aplicação Spring.
     * @param args Argumentos de linha de comando (se houver).
     */
    public static void main(String[] args) {
        SpringApplication.run(DeliveryApiApplication.class, args);
    }

}
