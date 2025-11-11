package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) para a criação de um novo Pedido.
 *
 * @implNote Este DTO não contém 'clienteId', pois o serviço
 * deve obtê-lo do usuário autenticado (logado). O endereço
 * é especificado pelo 'enderecoEntregaId' (um ID de um endereço
 * já salvo) e não por campos de texto.
 */
@Schema(description = "Dados para criação de um novo pedido")
public class PedidoDTO {

    @Schema(description = "ID do restaurante onde o pedido será realizado", example = "2", required = true)
    @NotNull(message = "Restaurante ID é obrigatório")
    @Positive(message = "Restaurante ID deve ser positivo")
    private Long restauranteId;

    @Schema(description = "ID do Endereço de entrega (selecionado da lista de endereços do cliente)", example = "10", required = true)
    @NotNull(message = "ID do Endereço de entrega é obrigatório")
    @Positive(message = "ID do Endereço deve ser positivo")
    private Long enderecoEntregaId;

    @Schema(description = "Observações adicionais do pedido", example = "Deixar na portaria", required = false)
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;

    @Schema(description = "Método de pagamento", example = "DINHEIRO", required = true)
    @NotBlank(message = "Método de pagamento é obrigatório")
    @Pattern(
            regexp = "^(DINHEIRO|CARTAO_CREDITO|CARTAO_DEBITO|PIX)$",
            message = "Método de pagamento deve ser: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO ou PIX"
    )
    private String metodoPagamento;

    @Schema(description = "Valor para troco (obrigatório se metodoPagamento=DINHEIRO)", example = "100.00", required = false)
    @Positive(message = "Valor do troco deve ser positivo")
    private BigDecimal trocoPara;

    @Schema(description = "Lista de itens do pedido (agora com suporte a opcionais)", required = true)
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid // Valida cada ItemPedidoDTO
    private List<ItemPedidoDTO> itens = new ArrayList<>();

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public Long getEnderecoEntregaId() { return enderecoEntregaId; }
    public void setEnderecoEntregaId(Long enderecoEntregaId) { this.enderecoEntregaId = enderecoEntregaId; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public BigDecimal getTrocoPara() { return trocoPara; }
    public void setTrocoPara(BigDecimal trocoPara) { this.trocoPara = trocoPara; }
    
    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}