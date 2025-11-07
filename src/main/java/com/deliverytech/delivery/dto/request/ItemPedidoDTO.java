package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
// import java.math.BigDecimal; // <-- Import removido, não é mais necessário
import java.util.List;

/**
 * DTO (Data Transfer Object) que representa um item do pedido.
 * (ARQUIVO CORRIGIDO: Removidos 'precoUnitario' e 'subtotal'.
 * O backend é agora responsável por buscar o preço no banco.)
 */
@Schema(description = "Item do pedido, contendo o produto, quantidade, observações e opcionais selecionados")
public class ItemPedidoDTO {

    // --------------------------------------------------
    // ID do produto
    // --------------------------------------------------
    @Schema(description = "ID do produto base", example = "5", required = true)
    @NotNull(message = "Produto ID é obrigatório")
    @Positive(message = "Produto ID deve ser positivo")
    private Long produtoId;

    // --------------------------------------------------
    // Quantidade do produto
    // --------------------------------------------------
    @Schema(description = "Quantidade do produto", example = "2", required = true, minimum = "1", maximum = "50")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 50, message = "Quantidade não pode exceder 50 unidades")
    private Integer quantidade;

    // --------------------------------------------------
    // Observações
    // --------------------------------------------------
    @Schema(description = "Observações sobre o item", example = "Sem cebola", required = false)
    @Size(max = 200, message = "Observações não podem exceder 200 caracteres")
    private String observacoes;

    // --------------------------------------------------
    // Opcionais
    // --------------------------------------------------
    @Schema(description = "Lista de IDs dos Itens Opcionais selecionados (ex: [5, 10])", required = false)
    private List<Long> opcionaisIds;

    // --------------------------------------------------
    // Preço unitário (REMOVIDO)
    // O cliente não deve enviar o preço. O backend irá buscá-lo.
    // --------------------------------------------------
    
    // --------------------------------------------------
    // Subtotal (REMOVIDO)
    // O cliente não deve enviar o subtotal. O backend irá calculá-lo.
    // --------------------------------------------------

    // ===================================================
    // GETTERS E SETTERS
    // ===================================================
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

    // --- Getters e Setters para precoUnitario e subtotal REMOVIDOS ---
}