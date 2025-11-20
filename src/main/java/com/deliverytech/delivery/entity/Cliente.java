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
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa o perfil cadastral de um cliente,
 * vinculado a um usuário do sistema.
 */
@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@Schema(description = "Entidade com os dados cadastrais (perfil) de um cliente")
public class Cliente {

    /** Identificador único (mesmo ID do usuário) */
    @Id
    @Schema(description = "Identificador único (chave primária e estrangeira do usuário)", example = "1")
    private Long id;

    /** Usuário associado ao cliente */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    @ToString.Exclude
    @Schema(description = "Usuário ao qual este perfil pertence")
    private Usuario usuario;

    /** Nome completo do cliente */
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Schema(description = "Nome completo do cliente", example = "João da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    /** CPF do cliente (apenas números) */
    @Column(unique = true, nullable = false, length = 11)
    @NotBlank(message = "CPF é obrigatório")
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter apenas números")
    @Schema(description = "CPF do cliente", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cpf;

    /** Telefone de contato */
    @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres")
    @Schema(description = "Telefone do cliente com DDD", example = "11912345678")
    private String telefone;

    /** Pedidos realizados pelo cliente */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Schema(description = "Lista de pedidos realizados pelo cliente")
    private List<Pedido> pedidos = new ArrayList<>();

    /** Construtor padrão */
    public Cliente() {
    }

    /** Construtor de conveniência */
    public Cliente(Usuario usuario, String nome, String cpf, String telefone) {
        this.usuario = usuario;
        this.id = usuario.getId();
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
    }
}
