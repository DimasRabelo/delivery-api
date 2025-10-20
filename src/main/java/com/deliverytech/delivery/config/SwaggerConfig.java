package com.deliverytech.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // --------------------------------------------------------------------------
    // 🔹 CONFIGURAÇÃO DO SWAGGER (OpenAPI)
    // --------------------------------------------------------------------------
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // Configura informações gerais da API
                .info(new Info()
                        .title("DeliveryTech API") // Título da API
                        .version("1.0.0") // Versão
                        .description("API REST completa para plataforma de delivery") // Descrição
                        .contact(new Contact() // Contato da equipe de desenvolvimento
                                .name("Equipe DeliveryTech")
                                .email("dev@deliverytech.com")
                                .url("https://deliverytech.com"))
                        .license(new License() // Licença da API
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Configura os servidores disponíveis
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080") // Servidor local para desenvolvimento
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.deliverytech.com") // Servidor de produção
                                .description("Servidor de Produção")
                ));
    }
}
