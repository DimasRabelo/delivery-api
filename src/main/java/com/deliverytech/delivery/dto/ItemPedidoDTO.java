package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// ==================================================
// DTO que representa um item do pedido
// Contém o produto e a quantidade desejada
// ==================================================
@Schema(description = "Item do pedido, contendo o produto e a quantidade desejada")
public class ItemPedidoDTO {

    // --------------------------------------------------
    // ID do produto
    // @Schema → documenta o campo no Swagger, com exemplo e obrigatoriedade
    // @NotNull → validação para garantir que o campo não seja nulo
    // --------------------------------------------------
    @Schema(description = "ID do produto", example = "5", required = true)
    @NotNull(message = "Produto é obrigatório")
    private Long produtoId;

    // --------------------------------------------------
    // Quantidade do produto
    // @Schema → documenta o campo no Swagger, define exemplo, mínimo e máximo
    // @NotNull → validação obrigatória
    // @Min / @Max → validação do intervalo permitido (1 a 10)
    // --------------------------------------------------
    @Schema(description = "Quantidade do produto", example = "2", required = true, minimum = "1", maximum = "10")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 10, message = "Quantidade máxima é 10")
    private Integer quantidade;

    // =======================
    // GETTERS E SETTERS
    // =======================
    
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
