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
import java.util.List;

/**
 * Representa a entidade Produto (item de cardápio).
 * Agora suporta um 'precoBase' e 'gruposOpcionais' para personalização.
 */
@Entity
@Getter // Usando Getter/Setter explícito para evitar problemas do @Data com relacionamentos
@Setter
@ToString(exclude = {"restaurante", "itensPedido", "gruposOpcionais"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Entidade que representa um produto (item de cardápio) de um restaurante")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do produto", example = "101")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true)
    private String nome;

    @Size(max = 255)
    @Schema(description = "Descrição detalhada do produto", example = "Molho de tomate fresco, mozzarella e manjericão")
    private String descricao;

    // --- MUDANÇA CRÍTICA ---
    // O campo 'preco' foi renomeado para 'precoBase'
    @NotNull(message = "Preço base é obrigatório")
    @PositiveOrZero(message = "Preço base deve ser zero ou positivo")
    @Column(name = "preco_base") // Renomeado no banco
    @Schema(description = "Preço base do produto (opcionais podem somar a este valor)", example = "40.00", required = true, minimum = "0")
    private BigDecimal precoBase; // Ex: Preço da Pizza Pequena (o menor)

    @Schema(description = "Categoria do produto", example = "Pizzas Tradicionais")
    private String categoria;

    @Schema(description = "Indica se o produto está disponível", example = "true", defaultValue = "true")
    private Boolean disponivel;

    @PositiveOrZero(message = "Estoque deve ser zero ou positivo")
    @Schema(description = "Quantidade em estoque (se aplicável)", example = "50", minimum = "0")
    private int estoque;

    // Relacionamento com Restaurante (Seu código original)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id")
    @Schema(description = "Restaurante ao qual este produto pertence")
    private Restaurante restaurante;

    // --- MUDANÇA 2: LINK PARA OS GRUPOS DE OPÇÕES ---
    /**
     * Define os grupos de personalização do produto.
     * Ex: 'Grupo 1: Tamanho', 'Grupo 2: Adicionais', 'Grupo 3: Remover ingredientes'.
     */
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lista de grupos de opcionais para este produto (Tamanho, Adicionais, etc.)")
    private List<GrupoOpcional> gruposOpcionais = new ArrayList<>();

    // Relacionamento com ItemPedido (Seu código original)
    @OneToMany(mappedBy = "produto")
    @Schema(description = "Lista de itens de pedido associados a este produto")
    private List<ItemPedido> itensPedido = new ArrayList<>();

    public Produto() {
    }
}