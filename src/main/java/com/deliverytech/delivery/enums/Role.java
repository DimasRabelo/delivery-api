package com.deliverytech.delivery.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum que define os níveis de permissão (Roles) dos usuários no sistema.
 * É usado pela entidade Usuario e pelo Spring Security para autorização.
 * Documentado com @Schema para que o Swagger UI possa exibir
 * estes valores como uma lista de opções.
 */
@Schema(description = "Define os níveis de permissão (Roles) de um Usuário no sistema")
public enum Role {
    
    /**
     * Usuário padrão do aplicativo, que pode realizar pedidos.
     */
    CLIENTE,
    
    /**
     * Usuário dono ou funcionário de um restaurante, pode gerenciar produtos e pedidos.
     */
    RESTAURANTE,
    
    /**
     * Administrador da plataforma, com acesso total ao sistema.
     */
    ADMIN,
    
    /**
     * Usuário responsável por realizar as entregas dos pedidos.
     */
    ENTREGADOR
}