package com.deliverytech.delivery.service.audit;

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
 * Logs são enviados para o logger "AUDIT", configurado no logback-spring.xml
 * para gravar em 'logs/delivery-api-audit.log'.
 */
@Service
public class AuditService {

    // ==========================================================
    // --- LOGGERS E UTILITÁRIOS ---
    // ==========================================================
    
    // 1. Logger específico para auditoria
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    // 2. ObjectMapper para converter mapas em JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==========================================================
    // --- MÉTODOS PÚBLICOS: AÇÕES DE AUDITORIA ---
    // ==========================================================

    /**
     * Loga uma ação genérica do usuário.
     *
     * @param userId   ID do usuário
     * @param action   Ação executada
     * @param resource Recurso afetado
     * @param details  Detalhes adicionais (objeto)
     */
    public void logUserAction(String userId, String action, String resource, Object details) {
        try {
            Map<String, Object> auditEvent = new HashMap<>();
            auditEvent.put("timestamp", LocalDateTime.now().toString());
            auditEvent.put("userId", userId);
            auditEvent.put("action", action);
            auditEvent.put("resource", resource);
            auditEvent.put("details", details);
            auditEvent.put("correlationId", MDC.get("correlationId"));
            auditEvent.put("sessionId", MDC.get("sessionId"));

            String jsonLog = objectMapper.writeValueAsString(auditEvent);
            auditLogger.info(jsonLog);

        } catch (Exception e) {
            auditLogger.error("Erro ao registrar evento de auditoria", e);
        }
    }

    /**
     * Loga uma mudança de dados (ex: atualização de status de pedido).
     *
     * @param userId    ID do usuário que realizou a mudança
     * @param entity    Nome da entidade alterada
     * @param entityId  ID da entidade
     * @param oldValue  Valor antigo
     * @param newValue  Valor novo
     * @param operation Tipo de operação (UPDATE, DELETE, CREATE)
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
     * Loga eventos de segurança (ex: falha de login, tentativas suspeitas).
     *
     * @param userId  ID do usuário
     * @param event   Nome do evento
     * @param details Detalhes adicionais
     * @param success Indica se o evento foi bem-sucedido
     */
    public void logSecurityEvent(String userId, String event, String details, boolean success) {
        try {
            Map<String, Object> securityEvent = new HashMap<>();
            securityEvent.put("timestamp", LocalDateTime.now().toString());
            securityEvent.put("userId", userId);
            securityEvent.put("event", event);
            securityEvent.put("details", details);
            securityEvent.put("success", success);
            securityEvent.put("correlationId", MDC.get("correlationId"));
            securityEvent.put("ipAddress", MDC.get("clientIp"));
            securityEvent.put("userAgent", MDC.get("userAgent"));

            String jsonLog = objectMapper.writeValueAsString(securityEvent);
            auditLogger.info(jsonLog);

        } catch (Exception e) {
            auditLogger.error("Erro ao registrar evento de segurança", e);
        }
    }
}
