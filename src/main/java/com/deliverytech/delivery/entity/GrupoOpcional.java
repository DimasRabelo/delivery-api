package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Define um grupo de escolhas para um Produto.
 * Ex: "Escolha o Tamanho" (min=1, max=1) ou "Escolha seus Adicionais" (min=0, max=5).
 */
@Entity
@Table(name = "grupo_opcional")
@Getter
@Setter
@ToString(exclude = {"produto", "itensOpcionais"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Um grupo de opções para um produto (ex: 'Tamanho' ou 'Adicionais')")
public class GrupoOpcional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do grupo", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Nome do grupo de escolha", example = "Escolha o Tamanho", required = true)
    private String nome;

    @Min(0)
    @Schema(description = "Quantas opções o cliente DEVE escolher (0=opcional, 1=obrigatório)", example = "1", minimum = "0")
    private int minSelecao = 0;

    @Min(1)
    @Schema(description = "Quantas opções o cliente PODE escolher (1=escolha única, 5=múltipla escolha)", example = "1", minimum = "1")
    private int maxSelecao = 1;

    // Link para o Produto (Dono deste grupo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    @Schema(description = "Produto ao qual este grupo de opcionais pertence")
    private Produto produto;

    // Link para as Opções (Ex: "Pequena", "Média", "Grande")
    @OneToMany(mappedBy = "grupoOpcional", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de itens de opção dentro deste grupo (ex: 'Pequena', 'Média', 'Grande')")
    private List<ItemOpcional> itensOpcionais = new ArrayList<>();

    public GrupoOpcional() {
    }
}