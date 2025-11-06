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
 * Tabela de Ligação (Join Table).
 * Registra que um 'ItemOpcional' (ex: "+Borda") foi selecionado 
 * para um 'ItemPedido' (ex: a "Pizza Média" no pedido 100).
 */
@Entity
@Table(name = "item_pedido_opcional")
@Getter
@Setter
@ToString(exclude = {"itemPedido", "itemOpcional"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Registra um opcional que foi selecionado para um item do pedido")
public class ItemPedidoOpcional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único da seleção do opcional", example = "1001")
    private Long id;

    // O Item do Pedido (Ex: A "Pizza Média" no Pedido 100)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_pedido_id", nullable = false)
    @Schema(description = "O item de pedido ao qual este opcional se aplica")
    private ItemPedido itemPedido;

    // O Opcional Escolhido (Ex: "+Borda Catupiry")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_opcional_id", nullable = false)
    @Schema(description = "O item opcional que foi selecionado")
    private ItemOpcional itemOpcional; // <-- CORRIGIDO (era ItemPedidoOpcional)

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço do opcional no momento da compra (snapshot)", example = "8.00")
    private BigDecimal precoRegistrado; // Preço "congelado" do opcional
    
    public ItemPedidoOpcional() {
    }

    /**
     * Construtor de conveniência
     * @param itemPedido O item do pedido
     * @param itemOpcional O opcional selecionado
     */
    public ItemPedidoOpcional(ItemPedido itemPedido, ItemOpcional itemOpcional) { // <-- CORRIGIDO (era ItemPedidoOpcional)
        this.itemPedido = itemPedido;
        this.itemOpcional = itemOpcional;
        
        // Agora esta linha funciona, pois ItemOpcional TEM o método getPrecoAdicional()
        if (itemOpcional != null && itemOpcional.getPrecoAdicional() != null) { // <-- CORRIGIDO
            this.precoRegistrado = itemOpcional.getPrecoAdicional(); // <-- CORRIGIDO
        } else {
            this.precoRegistrado = BigDecimal.ZERO;
        }
    }
}