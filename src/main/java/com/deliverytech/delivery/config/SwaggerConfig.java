package com.deliverytech.delivery.config; 

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {

   @Value("${server.port:8080}")
   private String serverPort;

   @Bean
   public OpenAPI customOpenAPI() {
       
       // --- CORREÇÃO AQUI ---
       // O nome DEVE ser "bearerAuth" para bater com o @SecurityRequirement dos controllers.
       final String securitySchemeName = "bearerAuth"; 

       return new OpenAPI()
               .info(apiInfo())
               .servers(List.of(
                       new Server()
                               .url("http://localhost:" + serverPort)
                               .description("Servidor de Desenvolvimento"),
                       new Server()
                               .url("https://api.deliverytech.com")
                               .description("Servidor de Produção")
               ))
               
               // 3. Adiciona a exigência de segurança (usando o nome corrigido)
               .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
               
               // 4. Define o esquema de segurança JWT (usando o nome corrigido)
               .components(new io.swagger.v3.oas.models.Components()
                       .addSecuritySchemes(securitySchemeName, createAPIKeyScheme()));
   }

   /**
    * Método privado para criar as informações da API.
    */
   private Info apiInfo() {
       return new Info()
               .title("DeliveryTech API") // <-- O teste 'testApiDocsAccessible' procura por isso!
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