
package com.deliverytech.delivery.entity;




import com.deliverytech.delivery.enums.StatusPedido;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataPedido;
    private String enderecoEntrega;
    private BigDecimal subtotal;
    private BigDecimal taxaEntrega;
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens = new ArrayList<>();

  

    @Column(unique = true)
private String numeroPedido;

// ======================
    // Métodos para gerenciar itens
    // ======================

    public void adicionarItem(ItemPedido item) {
    item.setPedido(this);            // mantém referência bidirecional
    this.itens.add(item);            // adiciona à lista
    recalcularSubtotal();            // atualiza subtotal
    this.valorTotal = calcularValorTotal(); // atualiza valor total automaticamente
}

public void recalcularSubtotal() {
    this.itens.forEach(ItemPedido::calcularSubtotal);
    this.subtotal = itens.stream()
                         .map(ItemPedido::getSubtotal)
                         .reduce(BigDecimal.ZERO, BigDecimal::add);
}

public BigDecimal calcularValorTotal() {
    BigDecimal taxa = taxaEntrega != null ? taxaEntrega : BigDecimal.ZERO;
    return (subtotal != null ? subtotal : BigDecimal.ZERO).add(taxa);
}

public void confirmar() {
    this.status = StatusPedido.CONFIRMADO;
    this.dataPedido = LocalDateTime.now();
    this.valorTotal = calcularValorTotal();
}


    @Column(columnDefinition = "TEXT")
private String observacoes;
}
    


