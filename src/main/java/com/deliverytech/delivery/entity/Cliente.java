package com.deliverytech.delivery.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // Import necessário
import lombok.Getter;           // Import necessário
import lombok.Setter;           // Import necessário
import lombok.ToString;         // Import necessário
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
// Substituímos @Data por anotações mais específicas
@Getter // Gera getters para todos os campos
@Setter // Gera setters para todos os campos
@ToString // Gera o método toString() (excluindo a lista de pedidos por padrão para evitar loops)
@EqualsAndHashCode(of = "id") // Gera equals() e hashCode() baseados APENAS no campo 'id'
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    private String telefone;
    private String endereco;

    // Inicializamos 'ativo' como true para novos clientes
    @Column(nullable = false) // Boa prática garantir que não seja nulo no banco
    private boolean ativo = true;

    @Column(name = "data_cadastro", nullable = false, updatable = false) // Boa prática: não nulo e não atualizável
    private LocalDateTime dataCadastro = LocalDateTime.now(); // Mantém a inicialização padrão

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    // Relacionamento com Pedido
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY) // fetch = FetchType.LAZY é geralmente recomendado para coleções
    @ToString.Exclude // Exclui a lista do toString() gerado pelo Lombok para evitar recursão infinita e logs enormes
    private List<Pedido> pedidos = new ArrayList<>();

    // Método utilitário para inativar (mantido)
    public void inativar() {
        this.ativo = false;
    }

    // --- Construtores ---
    // Lombok não gera construtores com @Getter/@Setter, então adicionamos se necessário
    // Construtor padrão (necessário para JPA)
    public Cliente() {
    }

    // Você pode adicionar outros construtores se precisar
    public Cliente(String nome, String email, String telefone, String endereco, String cpf) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.cpf = cpf;
        // 'ativo' e 'dataCadastro' já têm valores padrão
    }
}