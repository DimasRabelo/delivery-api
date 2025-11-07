package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
// import com.deliverytech.delivery.validation.ValidCEP; // REMOVIDO (não precisamos mais)
import java.math.BigDecimal; // IMPORT ADICIONADO
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) para a criação de um novo Pedido.
 * (REFATORADO para suportar Endereço por ID, Pagamento com Troco e Opcionais nos Itens)
 */
@Schema(description = "Dados para criação de um novo pedido")
public class PedidoDTO {

    // --- Identificação (Quem e De Onde) ---

    // O 'clienteId' FOI REMOVIDO. O serviço deve pegá-lo do usuário logado.
    // @Schema(description = "ID do cliente que está realizando o pedido", ...)
    // private Long clienteId;

    @Schema(description = "ID do restaurante onde o pedido será realizado", example = "2", required = true)
    @NotNull(message = "Restaurante ID é obrigatório")
    @Positive(message = "Restaurante ID deve ser positivo")
    private Long restauranteId;

    // --- Entrega (GARGALO 1 CORRIGIDO) ---

    // Os campos 'String enderecoEntrega' e 'String cep' FORAM REMOVIDOS.
    // Substituídos pelo ID do endereço selecionado.
    @Schema(description = "ID do Endereço de entrega (selecionado da lista de endereços do cliente)", example = "10", required = true)
    @NotNull(message = "ID do Endereço de entrega é obrigatório")
    @Positive(message = "ID do Endereço deve ser positivo")
    private Long enderecoEntregaId; // <-- CAMPO NOVO


    // --- Detalhes do Pedido (GARGALO 3 CORRIGIDO) ---

    @Schema(description = "Observações adicionais do pedido", example = "Deixar na portaria", required = false)
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres")
    private String observacoes;

    @Schema(description = "Método de pagamento", example = "DINHEIRO", required = true)
    @NotBlank(message = "Método de pagamento é obrigatório")
    @Pattern(
            regexp = "^(DINHEIRO|CARTAO_CREDITO|CARTAO_DEBITO|PIX)$",
            message = "Método de pagamento deve ser: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO ou PIX"
    )
    private String metodoPagamento; // <-- RENOMEADO (para consistência com a Entidade Pedido)

    @Schema(description = "Valor para troco (obrigatório se metodoPagamento=DINHEIRO)", example = "100.00", required = false)
    @Positive(message = "Valor do troco deve ser positivo")
    private BigDecimal trocoPara; // <-- CAMPO NOVO


    // --- Itens do Pedido (GARGALO 2 CORRIGIDO) ---

    @Schema(description = "Lista de itens do pedido (agora com suporte a opcionais)", required = true)
    @NotEmpty(message = "Pedido deve ter pelo menos um item")
    @Valid // Valida cada ItemPedidoDTO (que agora tem 'opcionaisIds')
    private List<ItemPedidoDTO> itens = new ArrayList<>(); // <-- OK (usa o DTO que refatoramos antes)

    // ===================================================
    // GETTERS E SETTERS
    // ===================================================
    
    // Getter/Setter para clienteId REMOVIDO

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    // Getters/Setters de 'enderecoEntrega' e 'cep' REMOVIDOS
    
    // Novo Getter/Setter para 'enderecoEntregaId'
    public Long getEnderecoEntregaId() { return enderecoEntregaId; }
    public void setEnderecoEntregaId(Long enderecoEntregaId) { this.enderecoEntregaId = enderecoEntregaId; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    // Getters/Setters para 'metodoPagamento' (renomeado)
    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    // Novo Getter/Setter para 'trocoPara'
    public BigDecimal getTrocoPara() { return trocoPara; }
    public void setTrocoPara(BigDecimal trocoPara) { this.trocoPara = trocoPara; }
    
    // Getter/Setter para 'itens' (sem mudança)
    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}