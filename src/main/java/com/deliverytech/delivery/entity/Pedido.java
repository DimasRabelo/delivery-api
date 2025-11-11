package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um pedido completo, com cliente, restaurante, itens, entrega e pagamento.
 */
@Entity
@Data
@ToString(exclude = {"cliente", "restaurante", "itens", "enderecoEntrega", "entregador"})
@Schema(description = "Entidade que representa um pedido completo no sistema")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do pedido", example = "1001")
    private Long id;

    @PastOrPresent(message = "A data do pedido não pode ser no futuro")
    @Schema(description = "Data e hora em que o pedido foi confirmado", example = "2024-06-05T19:30:00")
    private LocalDateTime dataPedido;

    @NotNull(message = "Endereço de entrega é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_entrega_id")
    @Schema(description = "Endereço selecionado pelo cliente para entrega")
    private Endereco enderecoEntrega;

    @PositiveOrZero
    @Schema(description = "Valor somado de todos os itens", example = "85.00")
    private BigDecimal subtotal;

    @PositiveOrZero
    @Schema(description = "Valor da taxa de entrega", example = "5.00")
    private BigDecimal taxaEntrega;

    @PositiveOrZero
    @Schema(description = "Valor total (subtotal + taxa de entrega)", example = "90.00")
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Status atual do pedido", example = "PENDENTE")
    private StatusPedido status;

    @NotBlank(message = "Método de pagamento é obrigatório")
    @Schema(description = "Método de pagamento escolhido", example = "DINHEIRO", required = true)
    private String metodoPagamento;

    @PositiveOrZero
    @Schema(description = "Se o método for DINHEIRO, valor para o qual o cliente precisa de troco", example = "100.00")
    private BigDecimal trocoPara;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @Schema(description = "Cliente que realizou o pedido")
    private Cliente cliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id")
    @Schema(description = "Restaurante que receberá o pedido")
    private Restaurante restaurante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id")
    @Schema(description = "Entregador atribuído ao pedido (opcional)")
    private Usuario entregador;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de itens que compõem o pedido")
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(unique = true)
    @NotBlank
    @Schema(description = "Número de identificação único do pedido", example = "PED-20240605-1001", required = true)
    private String numeroPedido;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Observações ou instruções adicionais do cliente", example = "Tirar a cebola.")
    private String observacoes;

    /** Adiciona um item ao pedido e recalcula o total. */
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
        recalcularSubtotal();
        this.valorTotal = calcularValorTotal();
    }

    /** Recalcula o subtotal com base nos itens. */
    public void recalcularSubtotal() {
        this.itens.forEach(ItemPedido::calcularSubtotal);
        this.subtotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Calcula o valor total do pedido (subtotal + taxa de entrega). */
    public BigDecimal calcularValorTotal() {
        BigDecimal taxa = (taxaEntrega != null) ? taxaEntrega : BigDecimal.ZERO;
        BigDecimal sub = (subtotal != null) ? subtotal : BigDecimal.ZERO;
        return sub.add(taxa);
    }

    /** Confirma o pedido e registra data e valor total. */
    public void confirmar() {
        this.status = StatusPedido.CONFIRMADO;
        this.dataPedido = LocalDateTime.now();
        this.valorTotal = calcularValorTotal();
    }
}
