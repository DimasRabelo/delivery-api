package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa um produto do cardápio de um restaurante.
 * Possui preço base e grupos opcionais para personalização.
 */
@Entity
@Getter
@Setter
@ToString(exclude = {"restaurante", "itensPedido", "gruposOpcionais"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Entidade que representa um produto do cardápio de um restaurante")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do produto", example = "101")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    @Schema(description = "Nome do produto", example = "Pizza Margherita", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Size(max = 255)
    @Schema(description = "Descrição detalhada do produto", example = "Molho de tomate fresco, mozzarella e manjericão")
    private String descricao;

    @NotNull(message = "Preço base é obrigatório")
    @PositiveOrZero(message = "Preço base deve ser zero ou positivo")
    @Column(name = "preco_base")
    @Schema(description = "Preço base do produto (opcionais podem somar a este valor)", example = "40.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precoBase;

    @Schema(description = "Categoria do produto", example = "Pizzas Tradicionais")
    private String categoria;

    @Schema(description = "Indica se o produto está disponível", example = "true", defaultValue = "true")
    private Boolean disponivel;

    @PositiveOrZero(message = "Estoque deve ser zero ou positivo")
    @Schema(description = "Quantidade em estoque (se aplicável)", example = "50", minimum = "0")
    private int estoque;

    /** Restaurante ao qual o produto pertence */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id")
    @Schema(description = "Restaurante responsável por este produto")
    private Restaurante restaurante;

    /** Grupos de personalização (ex: Tamanho, Adicionais) */
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de grupos opcionais do produto")
    private Set<GrupoOpcional> gruposOpcionais = new HashSet<>();

    /** Itens de pedido que contêm este produto */
    @OneToMany(mappedBy = "produto")
    @Schema(description = "Itens de pedido associados a este produto")
    private List<ItemPedido> itensPedido = new ArrayList<>();

    public Produto() {}
}
