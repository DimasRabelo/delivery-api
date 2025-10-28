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
    
    @Column(unique = true, nullable = false) // <-- BOA PRÁTICA
    private String email;

    private String telefone;
    private String endereco;
    private boolean ativo;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();

    // ==============================================
    // ADICIONAR ESTE CAMPO
    // ==============================================
    @Column(unique = true, nullable = false, length = 11) // Define como único, não nulo e com tamanho 11
    private String cpf;
    // ==============================================

    public void inativar() {
        this.ativo = false;
    }

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pedido> pedidos = new ArrayList<>();
}