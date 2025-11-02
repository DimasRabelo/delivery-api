package com.deliverytech.delivery.health; // Seu pacote

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Health Check customizado para o Banco de Dados.
 * Esta classe implementa a interface HealthIndicator do Actuator.
 * A anotação @Component("database") registra este bean no Spring com o
 * nome "database". O Actuator vai encontrá-lo automaticamente e exibi-lo
 * no endpoint /actuator/health.
 */
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator { // <-- Veja, agora está limpo!

    private final DataSource dataSource;

    // O Spring injeta automaticamente o DataSource que já está configurado
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        // Tenta pegar uma conexão do "pool" de conexões
        try (Connection connection = dataSource.getConnection()) {
            
            // Tenta validar a conexão com o banco (timeout de 1 segundo)
            if (connection.isValid(1)) {
                return Health.up() // Retorna "UP" (Saudável)
                        .withDetail("database", "H2")
                        .withDetail("status", "Conectado")
                        .withDetail("validationQuery", "isValid(1)")
                        .build();
            } else {
                // Se a conexão não for válida
                return Health.down() // Retorna "DOWN" (Inoperante)
                        .withDetail("database", "H2")
                        .withDetail("error", "Conexão inválida")
                        .build();
            }
        } catch (SQLException e) {
            // Se houver uma exceção ao tentar pegar a conexão (ex: pool esgotado)
            return Health.down()
                    .withDetail("database", "H2")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}