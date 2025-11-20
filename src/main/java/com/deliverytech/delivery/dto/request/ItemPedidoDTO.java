package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

/**
 * DTO (Data Transfer Object) que representa um item
 * enviado pelo cliente ao criar ou calcular um pedido.
 *
 * @implNote Este DTO não contém 'precoUnitario' ou 'subtotal'.
 * O backend é o único responsável por definir preços e calcular
 * totais com base nos IDs dos produtos/opcionais.
 */
@Schema(description = "Item do pedido, contendo o produto, quantidade, observações e opcionais selecionados")
public class ItemPedidoDTO {

    @Schema(description = "ID do produto base", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Produto ID é obrigatório")
    @Positive(message = "Produto ID deve ser positivo")
    private Long produtoId;

    @Schema(description = "Quantidade do produto", example = "2", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "50")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 50, message = "Quantidade não pode exceder 50 unidades")
    private Integer quantidade;

    @Schema(description = "Observações sobre o item", example = "Sem cebola")
    @Size(max = 200, message = "Observações não podem exceder 200 caracteres")
    private String observacoes;

    @Schema(description = "Lista de IDs dos Itens Opcionais selecionados (ex: [5, 10])")
    private List<Long> opcionaisIds;

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public List<Long> getOpcionaisIds() {
        return opcionaisIds;
    }

    public void setOpcionaisIds(List<Long> opcionaisIds) {
        this.opcionaisIds = opcionaisIds;
    }
}