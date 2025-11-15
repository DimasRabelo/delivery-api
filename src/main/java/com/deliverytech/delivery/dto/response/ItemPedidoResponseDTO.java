package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal; // Importação essencial para trabalhar com valores monetários precisos

/**
 * DTO de resposta usado para detalhar um item específico dentro de um pedido já existente.
 * Ele armazena uma "fotografia" do estado do item no momento da compra.
 */
@Data // Anotação do Lombok para gerar getters, setters, equals, hashCode e toString.
@Schema(description = "DTO de resposta para exibir os detalhes de um item do pedido")
public class ItemPedidoResponseDTO {

    @Schema(description = "ID do produto (necessário para o Front-end)", example = "100")
    private Long produtoId;
    
    // Detalhes do produto
    @Schema(description = "Nome do produto", example = "X-Bacon")
    private String nomeProduto;

    @Schema(description = "Quantidade de itens comprados", example = "2")
    private Integer quantidade;

    /**
     * Preço do produto no momento em que o pedido foi fechado.
     * É crucial para que o preço não mude se o restaurante alterar o valor do produto depois.
     */
    @Schema(description = "Preço unitário no momento da compra", example = "25.50")
    private BigDecimal precoUnitario;

    // Campos de cálculo
    @Schema(description = "Subtotal (quantidade * precoUnitario)", example = "51.00")
    private BigDecimal subtotal;

    // Informações adicionais
    @Schema(description = "Observações ou personalizações feitas pelo cliente", example = "Sem cebola")
    private String observacao;
}