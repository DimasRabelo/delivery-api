package com.deliverytech.delivery.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String telefone;
    private String endereco;
    private boolean ativo;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();

    public void inativar() {
        this.ativo = false;
    }

   @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Pedido> pedidos = new ArrayList<>();
}
