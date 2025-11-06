package com.deliverytech.delivery.integration;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
//import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
public class SwaggerIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    public void testSwaggerUIAccessible() {
        String url = "http://localhost:" + port + "/swagger-ui/index.html";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "Swagger UI não respondeu 200 OK");

        String body = response.getBody();
        assertNotNull(body, "O corpo da resposta não pode ser nulo");
        assertTrue(body.toLowerCase().contains("swagger"), "Conteúdo do Swagger UI não encontrado");
    }

    @Test
    public void testApiDocsAccessible() {
        String url = "http://localhost:" + port + "/v3/api-docs";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode(), "API docs não respondeu 200 OK");

        String body = response.getBody();
        assertNotNull(body, "O corpo da resposta do /v3/api-docs não pode ser nulo");
        assertTrue(body.contains("openapi"), "Campo 'openapi' não encontrado no /v3/api-docs");
    }
}
