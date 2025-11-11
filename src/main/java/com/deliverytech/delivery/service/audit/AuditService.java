package com.deliverytech.delivery.service.audit; // Seu novo pacote

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço centralizado para registrar eventos de auditoria e segurança.
 * Escreve logs no logger "AUDIT", que o logback-spring.xml direciona
 * para o arquivo 'logs/delivery-api-audit.log'.
 */
@Service
public class AuditService {

    // 1. Pega o Logger "AUDIT" (não o logger da classe)
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    // 2. Um ObjectMapper para converter nossos Mapas para JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Loga uma ação genérica do usuário.
     */
    public void logUserAction(String userId, String action, String resource, Object details) {
        try {
            Map<String, Object> auditEvent = new HashMap<>();
            auditEvent.put("timestamp", LocalDateTime.now().toString());
            auditEvent.put("userId", userId);
            auditEvent.put("action", action);
            auditEvent.put("resource", resource);
            auditEvent.put("details", details);
            
            // 3. Enriquecemos o log de auditoria com os dados do MDC (do Filtro)
            auditEvent.put("correlationId", MDC.get("correlationId"));
            auditEvent.put("sessionId", MDC.get("sessionId")); // <-- Agora existe!

            String jsonLog = objectMapper.writeValueAsString(auditEvent);
            auditLogger.info(jsonLog); // 4. Loga no nível INFO (para o logger AUDIT)

        } catch (Exception e) {
            auditLogger.error("Erro ao registrar evento de auditoria", e);
        }
    }

    /**
     * Loga uma mudança de dados (ex: Status do Pedido).
     */
    public void logDataChange(String userId, String entity, String entityId, 
                              Object oldValue, Object newValue, String operation) {
        try {
            Map<String, Object> changeEvent = new HashMap<>();
            changeEvent.put("timestamp", LocalDateTime.now().toString());
            changeEvent.put("userId", userId);
            changeEvent.put("entity", entity);
            changeEvent.put("entityId", entityId);
            changeEvent.put("operation", operation);
            changeEvent.put("oldValue", oldValue);
            changeEvent.put("newValue", newValue);
            changeEvent.put("correlationId", MDC.get("correlationId"));

            String jsonLog = objectMapper.writeValueAsString(changeEvent);
            auditLogger.info(jsonLog);

        } catch (Exception e) {
            auditLogger.error("Erro ao registrar mudança de dados", e);
        }
    }

    /**
     * Loga um evento de segurança (ex: Falha de Login).
     */
    public void logSecurityEvent(String userId, String event, String details, boolean success) {
        try {
            Map<String, Object> securityEvent = new HashMap<>();
            securityEvent.put("timestamp", LocalDateTime.now().toString());
            securityEvent.put("userId", userId);
            securityEvent.put("event", event);
            securityEvent.put("details", details);
            securityEvent.put("success", success);
            
            // 3. Enriquecemos o log de segurança com os dados do MDC (do Filtro)
            securityEvent.put("correlationId", MDC.get("correlationId"));
            securityEvent.put("ipAddress", MDC.get("clientIp"));
            securityEvent.put("userAgent", MDC.get("userAgent")); // <-- Agora existe!

            String jsonLog = objectMapper.writeValueAsString(securityEvent);
            auditLogger.info(jsonLog);

        } catch (Exception e) {
            auditLogger.error("Erro ao registrar evento de segurança", e);
        }
    }
}