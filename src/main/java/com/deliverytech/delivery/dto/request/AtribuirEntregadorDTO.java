package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO (Data Transfer Object) para encapsular o ID do entregador
 * em requisições de atribuição.
 */
@Data
@Schema(description = "DTO para atribuir um entregador a um pedido")
public class AtribuirEntregadorDTO {

    @Schema(description = "ID do Usuário (com ROLE_ENTREGADOR) a ser atribuído", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "O ID do entregador é obrigatório")
    @Positive(message = "O ID do entregador deve ser um número positivo")
    private Long entregadorId;
}