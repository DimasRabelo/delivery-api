package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.deliverytech.delivery.validation.ValidCEP;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO usado para criar ou atualizar pedidos.
 * Documentado no Swagger para fornecer informações claras sobre cada campo.
 */
@Schema(description = "Dados para criação ou atualização de um pedido")
public class PedidoDTO {

    // -------------------------------------------------
    // ID do cliente que está realizando o pedido
    // Obrigatório, positivo
    // -------------------------------------------------
    @Schema(description = "ID do cliente que está realizando o pedido", example = "1", required = true)
    @NotNull(message = "Cliente ID é obrigatório")
    @Positive(message = "Cliente ID deve ser positivo")
    private Long clienteId;

    // -------------------------------------------------
    // ID do restaurante onde o pedido será realizado
    // Obrigatório, positivo
    // -------------------------------------------------
    @Schema(description = "ID do restaurante onde o pedido será realizado", example = "2", required = true)
    @NotNull(message = "Restaurante ID é obrigatório")
    @Positive(message = "Restaurante ID deve ser positivo")
    private Long restauranteId;

    // -------------------------------------------------
    // Endereço completo de entrega
    // Obrigatório, máximo 200 caracteres
    // -------------------------------------------------
    @Schema(description = "Endereço completo para entrega", example = "Rua das Flores, 123 - Centro", required = true)
    @NotBlank(message = "Endereço de entrega é obrigatório")
    @Size(max = 200, message = "Endereço não pode exceder 200 caracteres")
    private String enderecoEntrega;

    // -------------------------------------------------
    // CEP de entrega
    // Obrigatório e validado com @ValidCEP
    // -------------------------------------------------
    @Schema(description = "CEP do endereço de entrega", example = "12345-678", required = true)
    @NotBlank(message = "CEP é obrigatório")
    @ValidCEP
    private String cep;

    // -------------------------------------------------
    // Observações do pedido
    // Opcional, máximo 500 caracteres
    // -------------------------------------------------
    @Schema(description = "Observações adicionais do pedido", example = "Deixar na portaria", required = false)
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;

    // -------------------------------------------------
    // Forma de pagamento
    // Obrigatório, aceita DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO ou PIX
    // -------------------------------------------------
    @Schema(description = "Forma de pagamento", example = "PIX", required = true)
    @NotBlank(message = "Forma de pagamento é obrigatória")
    @Pattern(
        regexp = "^(DINHEIRO|CARTAO_CREDITO|CARTAO_DEBITO|PIX)$",
        message = "Forma de pagamento deve ser: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO ou PIX"
    )
    private String formaPagamento;

    // -------------------------------------------------
    // Lista de itens do pedido
    // Obrigatória e validada individualmente com @Valid
    // -------------------------------------------------
    @Schema(description = "Lista de itens do pedido", required = true)
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid
    private List<ItemPedidoDTO> itens = new ArrayList<>();

    // =======================
    // GETTERS E SETTERS
    // =======================
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}
