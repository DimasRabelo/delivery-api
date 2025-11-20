package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade que representa um grupo de opções vinculadas a um produto.
 * Exemplo: "Escolha o tamanho" (min=1, max=1) ou "Escolha seus adicionais" (min=0, max=5).
 */
@Entity
@Table(name = "grupo_opcional")
@Getter
@Setter
@ToString(exclude = {"produto", "itensOpcionais"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Grupo de opções vinculadas a um produto (ex: 'Tamanho' ou 'Adicionais')")
public class GrupoOpcional {

    /** Identificador único do grupo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do grupo", example = "1")
    private Long id;

    /** Nome do grupo (ex: 'Escolha o tamanho') */
    @NotBlank
    @Schema(description = "Nome do grupo de opções", example = "Escolha o Tamanho", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    /** Número mínimo de seleções obrigatórias */
    @Min(0)
    @Schema(description = "Quantidade mínima de opções obrigatórias (0 = opcional)", example = "1", minimum = "0")
    private int minSelecao = 0;

    /** Número máximo de seleções permitidas */
    @Min(1)
    @Schema(description = "Quantidade máxima de opções permitidas (1 = única, 5 = múltiplas)", example = "1", minimum = "1")
    private int maxSelecao = 1;

    /** Produto ao qual o grupo pertence */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    @Schema(description = "Produto ao qual este grupo pertence")
    private Produto produto;

    /** Itens de opção dentro do grupo (ex: 'Pequena', 'Média', 'Grande') */
    @OneToMany(mappedBy = "grupoOpcional", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de itens pertencentes a este grupo")
    private Set<ItemOpcional> itensOpcionais = new HashSet<>();

    /** Construtor padrão */
    public GrupoOpcional() {
    }
}
