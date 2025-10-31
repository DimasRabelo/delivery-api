package com.deliverytech.delivery.config; // Pacote do seu arquivo atual

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
// Import necessário para ler o application.properties
import org.springframework.beans.factory.annotation.Value; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // Lê a porta do application.properties, usando 8080 como padrão
    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 1. Usa o método privado apiInfo()
                .info(apiInfo()) 
                
                // 2. Define os servidores
                .servers(List.of(
                        new Server()
                                // Usa a variável serverPort para a URL local
                                .url("http://localhost:" + serverPort) 
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.deliverytech.com")
                                .description("Servidor de Produção")
                ))
                
                // 3. Adiciona a exigência de segurança
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                
                // 4. Define o esquema de segurança JWT usando o método privado
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    /**
     * Método privado para criar as informações da API.
     */
    private Info apiInfo() {
        return new Info()
                .title("DeliveryTech API")
                .description("API REST para sistema de delivery de comida")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Equipe DeliveryTech")
                        .email("dev@deliverytech.com")
                        .url("https://deliverytech.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Método privado para criar o esquema de segurança Bearer.
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Insira o token JWT obtido no endpoint /api/auth/login");
    }
}