package com.deliverytech.delivery.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro Servlet que intercepta todas as requisições para:
 * 1. Garantir que um ID de correlação (X-Correlation-ID) exista (gerando um novo se necessário).
 * 2. Adicionar o ID de correlação e outros dados da requisição (IP, URI, etc.) ao
 * MDC (Mapped Diagnostic Context) do SLF4J.
 * 3. Adicionar o ID de correlação à resposta HTTP.
 *
 * Isso permite que todos os logs gerados durante o processamento de uma
 * única requisição possam ser rastreados usando o mesmo ID.
 */
@Component
public class CorrelationIdFilter implements Filter {

    /**
     * O nome do header HTTP para o ID de correlação.
     */
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * A chave usada para armazenar o ID de correlação no MDC (para logs).
     */
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 1. Tenta obter o ID de correlação do header da requisição
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                // 2. Gera um novo ID se não existir na requisição
                correlationId = generateCorrelationId();
            }

            // 3. Adiciona todos os dados relevantes ao MDC para logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put("clientIp", getClientIpAddress(httpRequest));
            MDC.put("requestUri", httpRequest.getRequestURI());
            MDC.put("httpMethod", httpRequest.getMethod());
            
            String userAgent = httpRequest.getHeader("User-Agent");
            MDC.put("userAgent", userAgent != null ? userAgent : "unknown");
            
            if (httpRequest.getSession(false) != null) {
                MDC.put("sessionId", httpRequest.getSession().getId());
            }

            // 4. Adiciona o ID de correlação à resposta (para o cliente ver)
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            // 5. Continua a cadeia de filtros
            chain.doFilter(request, response);

        } finally {
            // 6. Limpa o MDC no final da requisição (MUITO IMPORTANTE)
            // para evitar que o ID vaze para outras threads no pool.
            MDC.clear();
        }
    }

    /**
     * Gera um ID de correlação único (UUID curto de 16 caracteres).
     * @return Uma string de 16 caracteres.
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Obtém o endereço IP real do cliente, considerando proxies (X-Forwarded-For).
     * @param request A requisição HTTP.
     * @return O endereço IP do cliente.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Pega o primeiro IP se houver uma cadeia de proxies
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}