package com.deliverytech.delivery.entity; // (Ou o seu pacote 'model')

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
 * Define um item de escolha dentro de um GrupoOpcional.
 * Ex: "Pequena" (Preço: 0.00), "Média" (Preço: 5.00), "+Borda" (Preço: 8.00).
 */
@Entity
@Table(name = "item_opcional")
@Getter
@Setter
@ToString(exclude = {"grupoOpcional"})
@EqualsAndHashCode(of = "id")
@Schema(description = "Um item de opção dentro de um grupo (ex: 'Média' ou '+Bacon')")
public class ItemOpcional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do item opcional", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Nome do item de opção", example = "Grande", required = true)
    private String nome;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço adicional deste item (pode ser 0.00)", example = "15.00", required = true, minimum = "0")
    private BigDecimal precoAdicional; // Valor a somar ao 'precoBase' do produto

    // Link para o Grupo (Dono desta opção)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_opcional_id", nullable = false)
    @Schema(description = "Grupo ao qual este item opcional pertence")
    private GrupoOpcional grupoOpcional;

    public ItemOpcional() {
    }
}