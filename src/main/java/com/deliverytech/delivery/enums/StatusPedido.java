package com.deliverytech.delivery.enums;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger

/**
 * Enum que define os possíveis status de um Pedido.
 * Documentado com @Schema para que o Swagger UI possa exibir
 * estes valores como uma lista de opções.
 */
@Schema(description = "Define os possíveis status de um Pedido no sistema") // Documentação a nível de classe
public enum StatusPedido {
    
    // Lista de valores possíveis do Enum
    PENDENTE("Pendente"),
    CONFIRMADO("Confirmado"),
    PREPARANDO("Preparando"),
    SAIU_PARA_ENTREGA("Saiu para Entrega"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    // Atributo interno para armazenar a descrição amigável
    private final String descricao;

    /**
     * Construtor do Enum.
     * @param descricao A descrição amigável do status.
     */
    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna a descrição amigável do status (ex: "Pendente").
     * @return A descrição.
     */
    public String getDescricao() {
        return descricao;
    }
}