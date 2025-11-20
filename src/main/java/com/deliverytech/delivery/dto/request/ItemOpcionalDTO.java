package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) que representa um item individual
 * dentro de um GrupoOpcional (ex: "Média", "Grande", "+Bacon").
 */
@Schema(description = "DTO para um item opcional (ex: 'Média' ou '+Bacon')")
public class ItemOpcionalDTO {

    @Schema(description = "ID do item opcional (usado apenas para atualização)")
    private Long id; // Útil para o método de 'atualizarProduto'

    @NotBlank
    @Schema(description = "Nome do item", example = "Média", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço adicional deste item", example = "5.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal precoAdicional;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrecoAdicional() { return precoAdicional; }
    public void setPrecoAdicional(BigDecimal precoAdicional) { this.precoAdicional = precoAdicional; }
}