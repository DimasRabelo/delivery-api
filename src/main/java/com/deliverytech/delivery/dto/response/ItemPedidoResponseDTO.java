package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "DTO de resposta para exibir os detalhes de um item do pedido")
public class ItemPedidoResponseDTO {

    @Schema(description = "Nome do produto", example = "X-Bacon")
    private String nomeProduto;

    @Schema(description = "Quantidade", example = "2")
    private Integer quantidade;

    @Schema(description = "Preço unitário no momento da compra", example = "25.50")
    private BigDecimal precoUnitario;

    @Schema(description = "Subtotal (qtd * preco)", example = "51.00")
    private BigDecimal subtotal;

    @Schema(description = "Observações do cliente", example = "Sem cebola")
    private String observacao;
}