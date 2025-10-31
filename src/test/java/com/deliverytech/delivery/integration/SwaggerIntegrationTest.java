package com.deliverytech.delivery.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SwaggerIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testSwaggerUIAccessible() {
        String url = "http://localhost:" + port + "/swagger-ui.html";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // --- CORREÇÃO DEFINITIVA ---
        // 1. Chame getBody() APENAS UMA VEZ e guarde em uma variável
        String body = response.getBody();
        
        // 2. Agora verifique e use a variável
        assertNotNull(body, "O corpo da resposta do Swagger UI não pode ser nulo");
        assertTrue(body.contains("swagger"));
    }

    @Test
    public void testApiDocsAccessible() {
        String url = "http://localhost:" + port + "/api-docs";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // --- CORREÇÃO DEFINITIVA ---
        String body = response.getBody();
        assertNotNull(body, "O corpo da resposta do /v3/api-docs não pode ser nulo");
        
        assertTrue(body.contains("openapi"));
        assertTrue(body.contains("DeliveryTech API"));
    }

    @Test
    public void testApiDocsContainsExpectedEndpoints() {
        String url = "http://localhost:" + port + "/api-docs";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        // --- CORREÇÃO DEFINITIVA ---
        String body = response.getBody();
        assertNotNull(body, "O corpo da resposta do /v3/api-docs não pode ser nulo");

        assertTrue(body.contains("/api/restaurantes"));
        assertTrue(body.contains("/api/produtos"));
        assertTrue(body.contains("/api/pedidos"));
        assertTrue(body.contains("/api/auth"));
    }
}