package com.deliverytech.delivery.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@ToString(exclude = {"produtos", "pedidos"}) // evita LazyInitializationException
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String categoria;
    private String endereco;
    private String telefone;

    @Column(name = "taxa_entrega")
    private BigDecimal taxaEntrega;

    private Boolean ativo;

    @OneToMany(mappedBy = "restaurante")
    private List<Produto> produtos;

    @OneToMany(mappedBy = "restaurante")
    private List<Pedido> pedidos;

    @Column(precision = 3, scale = 2)
    private BigDecimal avaliacao;

    // NOVOS CAMPOS
    @Column(name = "tempo_entrega")
    private Integer tempoEntrega;

    @Column(name = "horario_funcionamento")
    private String horarioFuncionamento;
}
