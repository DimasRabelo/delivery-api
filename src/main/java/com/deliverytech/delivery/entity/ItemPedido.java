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
 * Representa a "linha" de um pedido.
 * Agora armazena o 'precoUnitario' calculado (base + opcionais) e a lista de
 * opcionais que foram selecionados para este item.
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"pedido", "produto", "opcionaisSelecionados"})
@EqualsAndHashCode(of = "id")
@Table(name = "itens_pedido")
@Schema(description = "Entidade que representa um item dentro de um pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do item no pedido", example = "501")
    private Long id;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "A quantidade deve ser de pelo menos 1")
    @Schema(description = "Quantidade deste produto no pedido", example = "2", required = true, minimum = "1")
    private Integer quantidade;

    // --- MUDANÇA CRÍTICA ---
    // Este preço não vem mais direto do Produto. Ele deve ser CALCULADO 
    // (precoBase + precoAdicional dos opcionais) pelo seu Service.
    @NotNull(message = "Preço unitário é obrigatório")
    @PositiveOrZero(message = "Preço unitário não pode ser negativo")
    @Schema(description = "Preço unitário JÁ CALCULADO (com opcionais) no momento da compra (snapshot)", example = "63.00", required = true, minimum = "0")
    private BigDecimal precoUnitario; 

    @NotNull(message = "Subtotal é obrigatório")
    @PositiveOrZero(message = "Subtotal não pode ser negativo")
    @Schema(description = "Subtotal (quantidade * precoUnitario)", example = "126.00", required = true, minimum = "0")
    private BigDecimal subtotal; // (quantidade * precoUnitario)

    // --- Relacionamentos (Seu código original) ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    @NotNull(message = "O item deve estar associado a um pedido")
    @Schema(description = "O Pedido ao qual este item pertence")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    @NotNull(message = "O item deve estar associado a um produto")
    @Schema(description = "O Produto que está sendo comprado")
    private Produto produto;

    // --- MUDANÇA 2: LINK PARA OS OPCIONAIS ESCOLHIDOS ---
    /**
     * Registra quais 'ItemOpcional' foram selecionados pelo cliente
     * para ESTE 'ItemPedido' específico.
     */
    @OneToMany(mappedBy = "itemPedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de opcionais que foram selecionados para este item")
    private List<ItemPedidoOpcional> opcionaisSelecionados = new ArrayList<>();

    // --- Construtores ---

    /**
     * Construtor padrão (vazio).
     * Necessário para o funcionamento da JPA.
     */
    public ItemPedido() {
    }

    // --- Lógica de Negócio ---

    /**
     * Lógica de Negócio: Calcula (ou recalcula) o subtotal deste item.
     * Deve ser chamado pelo Service DEPOIS de definir o 'precoUnitario' calculado.
     */
    public void calcularSubtotal() {
        if (precoUnitario != null && quantidade != null && quantidade > 0) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}