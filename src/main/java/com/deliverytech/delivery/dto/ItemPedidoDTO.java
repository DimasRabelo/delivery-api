package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO que representa um item do pedido.
 * Contém o produto, a quantidade desejada e observações.
 */
@Schema(description = "Item do pedido, contendo o produto, quantidade e observações")
public class ItemPedidoDTO {

    // --------------------------------------------------
    // ID do produto
    // Obrigatório e positivo
    // --------------------------------------------------
    @Schema(description = "ID do produto", example = "5", required = true)
    @NotNull(message = "Produto ID é obrigatório")
    @Positive(message = "Produto ID deve ser positivo")
    private Long produtoId;

    // --------------------------------------------------
    // Quantidade do produto
    // Obrigatória, mínima 1, máxima 50
    // --------------------------------------------------
    @Schema(description = "Quantidade do produto", example = "2", required = true, minimum = "1", maximum = "50")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 50, message = "Quantidade não pode exceder 50 unidades")
    private Integer quantidade;

    // --------------------------------------------------
    // Observações do item
    // Opcional, máximo 200 caracteres
    // --------------------------------------------------
    @Schema(description = "Observações sobre o item", example = "Sem cebola", required = false)
    @Size(max = 200, message = "Observações não podem exceder 200 caracteres")
    private String observacoes;

    // =======================
    // GETTERS E SETTERS
    // =======================
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
