package com.deliverytech.delivery.entity;

import jakarta.persistence.*; import lombok.Data;

import java.math.BigDecimal;

@Entity @Data
@Table(name = "itens_pedido")
public class ItemPedido { @Id
@GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

private int quantidade;
private BigDecimal precoUnitario; private BigDecimal subtotal;

@ManyToOne
@JoinColumn(name = "pedido_id") private Pedido pedido;

@ManyToOne
@JoinColumn(name = "produto_id") private Produto produto;

public void calcularSubtotal() {
    if (precoUnitario != null) {
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    } else {
        this.subtotal = BigDecimal.ZERO;
    }
}



}
