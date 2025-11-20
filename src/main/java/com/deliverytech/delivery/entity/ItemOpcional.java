package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

/**
 * Entidade que representa um item opcional dentro de um grupo de opções.
 * Exemplo: "Pequena" (R$ 0,00), "Média" (R$ 5,00), "+Borda" (R$ 8,00).
 */
@Entity
@Table(name = "item_opcional")
@Getter
@Setter
@ToString(exclude = "grupoOpcional")
@EqualsAndHashCode(of = "id")
@Schema(description = "Item de opção pertencente a um grupo (ex: 'Grande' ou '+Bacon')")
public class ItemOpcional {

    /** Identificador único do item opcional */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do item opcional", example = "1")
    private Long id;

    /** Nome do item de opção */
    @NotBlank
    @Schema(description = "Nome do item opcional", example = "Grande", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    /** Preço adicional cobrado pelo item (pode ser zero) */
    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço adicional deste item", example = "15.00", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
    private BigDecimal precoAdicional;

    /** Grupo de opções ao qual o item pertence */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_opcional_id", nullable = false)
    @Schema(description = "Grupo ao qual este item pertence")
    private GrupoOpcional grupoOpcional;

    /** Construtor padrão */
    public ItemOpcional() {
    }
}
