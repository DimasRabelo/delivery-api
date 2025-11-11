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

/**
 * Configuração do OpenAPI (Swagger 3) para a documentação da API.
 * <p>
 * Define as informações gerais da API, os servidores (dev/prod) e
 * configura o esquema de segurança JWT (Bearer Auth) para
 * que o "cadeado" (Authorize) apareça na UI do Swagger.
 */
@Configuration
public class SwaggerConfig {

   /**
    * Injeta a porta do servidor (padrão 8080) para configurar
    * o link do servidor de desenvolvimento.
    */
   @Value("${server.port:8080}")
   private String serverPort;

   /**
    * Cria e configura o bean principal do OpenAPI.
    * @return O objeto OpenAPI configurado.
    */
   @Bean
   public OpenAPI customOpenAPI() {
       
       // 1. Define o nome do esquema de segurança.
       // Este nome *deve* ser o mesmo usado nos @SecurityRequirement() dos controllers.
       final String securitySchemeName = "bearerAuth"; 

       return new OpenAPI()
               // 2. Define as informações gerais da API (título, versão, etc.)
               .info(apiInfo())
               
               // 3. Define os servidores de desenvolvimento e produção
               .servers(List.of(
                       new Server()
                               .url("http://localhost:" + serverPort)
                               .description("Servidor de Desenvolvimento"),
                       new Server()
                               .url("https://api.deliverytech.com")
                               .description("Servidor de Produção")
               ))
               
               // 4. Adiciona a exigência de segurança global (o "cadeado" em todos os endpoints)
               .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
               
               // 5. Define o esquema de segurança (como o JWT Bearer funciona)
               .components(new io.swagger.v3.oas.models.Components()
                       .addSecuritySchemes(securitySchemeName, createAPIKeyScheme()));
   }

   /**
    * Método helper que constrói o bloco 'Info' da documentação.
    * @return O objeto Info preenchido.
    */
   private Info apiInfo() {
       return new Info()
               .title("DeliveryTech API") // Este título é frequentemente verificado em testes
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
    * Método helper que cria o esquema de segurança (SecurityScheme)
    * para JWT Bearer Auth.
    * @return O SecurityScheme configurado.
    */
   private SecurityScheme createAPIKeyScheme() {
       return new SecurityScheme()
               .type(SecurityScheme.Type.HTTP)
               .bearerFormat("JWT")
               .scheme("bearer")
               .description("Insira o token JWT obtido no endpoint /api/auth/login");
   }
}