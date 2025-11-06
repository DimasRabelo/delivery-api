package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List; // IMPORT ADICIONADO

/**
 * DTO (Data Transfer Object) que representa um item do pedido.
 * Esta classe é usada na lista 'itens' dentro do PedidoDTO para
 * definir o que o cliente deseja comprar (incluindo opcionais).
 */
@Schema(description = "Item do pedido, contendo o produto, quantidade, observações e opcionais selecionados")
public class ItemPedidoDTO {

    // --------------------------------------------------
    // ID do produto (Seu código original)
    // --------------------------------------------------
    @Schema(description = "ID do produto base", example = "5", required = true)
    @NotNull(message = "Produto ID é obrigatório")
    @Positive(message = "Produto ID deve ser positivo")
    private Long produtoId;

    // --------------------------------------------------
    // Quantidade do produto (Seu código original)
    // --------------------------------------------------
    @Schema(description = "Quantidade do produto", example = "2", required = true, minimum = "1", maximum = "50")
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    @Max(value = 50, message = "Quantidade não pode exceder 50 unidades")
    private Integer quantidade;

    // --------------------------------------------------
    // Observações do item (Seu código original)
    // --------------------------------------------------
    @Schema(description = "Observações sobre o item", example = "Sem cebola", required = false)
    @Size(max = 200, message = "Observações não podem exceder 200 caracteres")
    private String observacoes;


    // --- MUDANÇA (GARGALO 2) ---
    /**
     * Lista de IDs dos Itens Opcionais que o cliente selecionou.
     * Ex: [5, 10] (Onde 5=Tamanho Grande, 10=+Borda)
     */
    @Schema(description = "Lista de IDs dos Itens Opcionais selecionados (ex: [5, 10])", required = false)
    private List<Long> opcionaisIds; // <-- CAMPO ADICIONADO


    // ===================================================
    // GETTERS E SETTERS
    // ===================================================
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    // --- NOVO GETTER/SETTER ---
    public List<Long> getOpcionaisIds() { return opcionaisIds; }
    public void setOpcionaisIds(List<Long> opcionaisIds) { this.opcionaisIds = opcionaisIds; }
}