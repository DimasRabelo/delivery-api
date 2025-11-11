package com.deliverytech.delivery.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = generateCorrelationId();
            }

            // --- Adicionando dados ao MDC ---
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put("clientIp", getClientIpAddress(httpRequest));
            MDC.put("requestUri", httpRequest.getRequestURI());
            MDC.put("httpMethod", httpRequest.getMethod());

            // ==========================================================
            // ⬇️ ATUALIZAÇÃO: ADICIONANDO OS CAMPOS QUE FALTAVAM ⬇️
            // ==========================================================
            // O AuditService vai ler esses valores
            String userAgent = httpRequest.getHeader("User-Agent");
            MDC.put("userAgent", userAgent != null ? userAgent : "unknown");
            
            if (httpRequest.getSession(false) != null) {
                MDC.put("sessionId", httpRequest.getSession().getId());
            }
            // ==========================================================

            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}