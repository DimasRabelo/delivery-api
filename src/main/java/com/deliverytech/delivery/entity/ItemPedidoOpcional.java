package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entidade que representa o vínculo entre um item do pedido e um opcional selecionado.
 */
@Entity
@Table(name = "item_pedido_opcional")
@Getter
@Setter
@ToString(exclude = {"itemPedido", "itemOpcional"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Registra um opcional selecionado para um item do pedido")
public class ItemPedidoOpcional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do registro", example = "1001")
    private Long id;

    // Item do pedido ao qual o opcional pertence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_pedido_id", nullable = false)
    @Schema(description = "Item do pedido ao qual este opcional pertence")
    private ItemPedido itemPedido;

    // Opcional selecionado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_opcional_id", nullable = false)
    @Schema(description = "Opcional selecionado para o item do pedido")
    private ItemOpcional itemOpcional;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço do opcional no momento da compra", example = "8.00")
    private BigDecimal precoRegistrado;

    public ItemPedidoOpcional() {
    }

    /**
     * Cria uma nova associação entre um item de pedido e um opcional.
     * O preço é registrado no momento da seleção.
     */
    public ItemPedidoOpcional(ItemPedido itemPedido, ItemOpcional itemOpcional) {
        this.itemPedido = itemPedido;
        this.itemOpcional = itemOpcional;
        this.precoRegistrado = (itemOpcional != null && itemOpcional.getPrecoAdicional() != null)
                ? itemOpcional.getPrecoAdicional()
                : BigDecimal.ZERO;
    }
}
