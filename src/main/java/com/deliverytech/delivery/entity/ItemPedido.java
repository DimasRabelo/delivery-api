package com.deliverytech.delivery.entity;

import jakarta.persistence.*; 
import lombok.Data;

import java.math.BigDecimal;



@Entity
@Data
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    // Construtor sem argumentos (necessário para JPA e DataLoader)
    public ItemPedido() {}

    // Construtor conveniente
    public ItemPedido(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        if (produto != null && produto.getPreco() != null) {
            this.precoUnitario = produto.getPreco();
        } else {
            this.precoUnitario = BigDecimal.ZERO;
        }
        calcularSubtotal();
    }

    public void calcularSubtotal() {
        if (precoUnitario != null && quantidade > 0) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}
