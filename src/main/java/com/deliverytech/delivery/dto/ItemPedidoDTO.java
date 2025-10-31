package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.validation.constraints.*; // Importações para Validação (Bean Validation)

/**
 * DTO (Data Transfer Object) que representa um item do pedido.
 * Esta classe é usada na lista 'itens' dentro do PedidoDTO para
 * definir o que o cliente deseja comprar.
 */
@Schema(description = "Item do pedido, contendo o produto, quantidade e observações") // Documentação a nível de classe
public class ItemPedidoDTO {

    // --------------------------------------------------
    // ID do produto
    // Obrigatório e positivo
    // --------------------------------------------------
    @Schema(description = "ID do produto", example = "5", required = true) // Documentação Swagger
    @NotNull(message = "Produto ID é obrigatório") // Validação: Não pode ser nulo
    @Positive(message = "Produto ID deve ser positivo") // Validação: Deve ser > 0
    private Long produtoId;

    // --------------------------------------------------
    // Quantidade do produto
    // Obrigatória, mínima 1, máxima 50
    // --------------------------------------------------
    @Schema(description = "Quantidade do produto", example = "2", required = true, minimum = "1", maximum = "50") // Documentação Swagger
    @NotNull(message = "Quantidade é obrigatória") // Validação: Não pode ser nulo
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1") // Validação: Valor mínimo
    @Max(value = 50, message = "Quantidade não pode exceder 50 unidades") // Validação: Valor máximo
    private Integer quantidade;

    // --------------------------------------------------
    // Observações do item
    // Opcional, máximo 200 caracteres
    // --------------------------------------------------
    @Schema(description = "Observações sobre o item", example = "Sem cebola", required = false) // Documentação Swagger (opcional)
    @Size(max = 200, message = "Observações não podem exceder 200 caracteres") // Validação: Tamanho máximo (permite nulo ou vazio)
    private String observacoes;

    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}