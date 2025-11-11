package com.deliverytech.delivery.exception;

/**
 * Exceção lançada quando uma entidade não é encontrada no sistema.
 * Herda de BusinessException e define um código de erro padrão.
 */
public class EntityNotFoundException extends BusinessException {

    private String entityName;
    private Object entityId;

    // ------------------------------------------------------
    // Construtor principal — gera mensagem automática
    // ------------------------------------------------------
    public EntityNotFoundException(String entityName, Object entityId) {
        super(String.format("%s com ID %s não foi encontrado(a)", entityName, entityId));
        this.entityName = entityName;
        this.entityId = entityId;
        this.setErrorCode("ENTITY_NOT_FOUND");
    }

    // ------------------------------------------------------
    // Construtor alternativo — permite mensagem personalizada
    // ------------------------------------------------------
    public EntityNotFoundException(String message) {
        super(message);
        this.setErrorCode("ENTITY_NOT_FOUND");
    }

    // ------------------------------------------------------
    // Getters
    // ------------------------------------------------------
    public String getEntityName() {
        return entityName;
    }

    public Object getEntityId() {
        return entityId;
    }
}
