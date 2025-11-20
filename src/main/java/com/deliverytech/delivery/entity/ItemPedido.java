package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa um item dentro de um pedido.
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"pedido", "produto", "opcionaisSelecionados"})
@EqualsAndHashCode(of = "id")
@Table(name = "itens_pedido")
@Schema(description = "Item pertencente a um pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do item", example = "501")
    private Long id;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser de pelo menos 1")
    @Schema(description = "Quantidade do produto no pedido", example = "2", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1")
    private Integer quantidade;

    @NotNull(message = "Preço unitário é obrigatório")
    @PositiveOrZero(message = "Preço unitário não pode ser negativo")
    @Schema(description = "Preço unitário calculado (produto + opcionais)", example = "63.00", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private BigDecimal precoUnitario;

    @NotNull(message = "Subtotal é obrigatório")
    @PositiveOrZero(message = "Subtotal não pode ser negativo")
    @Schema(description = "Subtotal = quantidade * preço unitário", example = "126.00", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private BigDecimal subtotal;

    @Schema(description = "Observações específicas deste item", example = "Sem cebola, bem passado")
    @Column(length = 255) // Opcional: define tamanho no banco
    private String observacoes;

    // Relação com o pedido ao qual o item pertence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    @NotNull(message = "O item deve estar associado a um pedido")
    private Pedido pedido;

    // Produto associado ao item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    @NotNull(message = "O item deve estar associado a um produto")
    private Produto produto;

    // Opcionais selecionados para este item
    @OneToMany(mappedBy = "itemPedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de opcionais selecionados")
    private List<ItemPedidoOpcional> opcionaisSelecionados = new ArrayList<>();

    public ItemPedido() {
    }

    /**
     * Calcula o subtotal do item (quantidade * preço unitário).
     * Deve ser chamado após definir o preço unitário.
     */
    public void calcularSubtotal() {
        if (precoUnitario != null && quantidade != null && quantidade > 0) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
