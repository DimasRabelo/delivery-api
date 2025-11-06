package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// Importações de campos duplicados (Email, LocalDateTime) foram removidas
import java.util.ArrayList;
import java.util.List;

/**
 * Representa o perfil cadastral de um Cliente.
 * Armazena informações pessoais (nome, cpf, telefone) ligadas a um Usuário.
 */
@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@Schema(description = "Entidade com os dados cadastrais (perfil) de um Cliente")
public class Cliente {

    // --- CHAVE PRIMÁRIA (MESMO ID DO USUÁRIO) ---
    @Id
    @Schema(description = "Identificador único (Chave Primária e Estrangeira, mesmo ID do Usuário)", example = "1")
    private Long id; // NÃO é @GeneratedValue

    // --- LINK COM A ENTIDADE DE AUTENTICAÇÃO ---
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Diz ao JPA: "O ID desta entidade (Cliente) é mapeado pelo 'usuario'"
    @JoinColumn(name = "id") // A coluna 'id' é a PK e a FK
    @ToString.Exclude
    @Schema(description = "O usuário (de autenticação) ao qual este perfil pertence")
    private Usuario usuario;

    // --- CAMPOS DE PERFIL (DADOS CADASTRAIS) ---

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Schema(description = "Nome completo do cliente", example = "João da Silva", required = true)
    private String nome; // O 'nome' agora fica aqui

    @Column(unique = true, nullable = false, length = 11)
    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter apenas números")
    @Schema(description = "CPF do cliente (apenas números)", example = "12345678901", required = true)
    private String cpf;

    @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres")
    @Schema(description = "Telefone de contato do cliente (com DDD)", example = "11912345678")
    private String telefone;

    // --- CAMPO 'endereco' (STRING) REMOVIDO ---
    // Os endereços agora estão em 'usuario.getEnderecos()'

    // --- RELACIONAMENTO COM PEDIDOS ---
    @OneToMany(
            mappedBy = "cliente",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @ToString.Exclude
    @Schema(description = "Lista de pedidos realizados pelo cliente")
    private List<Pedido> pedidos = new ArrayList<>();

    // --- Construtores ---
    public Cliente() {
    }

    /**
     * Construtor de conveniência para criar o perfil do cliente.
     */
    public Cliente(Usuario usuario, String nome, String cpf, String telefone) {
        this.usuario = usuario;
        this.id = usuario.getId(); // Seta a PK/FK
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }

   
}