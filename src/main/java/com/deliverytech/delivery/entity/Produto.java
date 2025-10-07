
package com.deliverytech.delivery.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;  


@Entity
@Data
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private BigDecimal preco;
    private String categoria;
    private Boolean disponivel;

    @ManyToOne
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    // Relacionamento com ItemPedido
    @OneToMany(mappedBy = "produto")
    private List<ItemPedido> itemPedido;
}
