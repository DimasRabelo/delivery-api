
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
        item.setPedido(this);           // mantém referência bidirecional
        this.itens.add(item);           // adiciona à lista
        recalcularSubtotal();           // atualiza subtotal automaticamente
    }

    public void recalcularSubtotal() {
        this.subtotal = itens.stream()
                             .map(ItemPedido::getSubtotal)
                             .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void confirmar() {
        this.status = StatusPedido.CONFIRMADO;
        this.valorTotal = subtotal.add(taxaEntrega != null ? taxaEntrega : BigDecimal.ZERO);
        this.dataPedido = LocalDateTime.now();
    }

    @Column(columnDefinition = "TEXT")
private String observacoes;
}
    


