package com.deliverytech.delivery.entity;


import jakarta.persistence.*; import lombok.Data;
import java.math.BigDecimal; import java.util.List;



@Entity @Data
public class Restaurante { @Id
@GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
private String nome; private String categoria; 

private String endereco; 

private String telefone;

@Column(name = "taxa_entrega")
private BigDecimal taxaEntrega;

private Boolean ativo;

@OneToMany(mappedBy = "restaurante")   // Relacionamento JPA
                     // Para evitar loop infinito no JSON
    private List<Produto> produtos;

    @OneToMany(mappedBy = "restaurante")   // Relacionamento JPA
                     // Para evitar loop infinito no JSON
    private List<Pedido> pedidos;

    @Column(precision = 3, scale = 2)
    private BigDecimal avaliacao;

}
