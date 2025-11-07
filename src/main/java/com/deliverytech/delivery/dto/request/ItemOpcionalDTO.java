package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Schema(description = "DTO para um item opcional (ex: 'Média' ou '+Bacon')")
public class ItemOpcionalDTO {

    @Schema(description = "ID do item opcional (usado apenas para atualização)")
    private Long id; // Útil para o método de 'atualizarProduto'

    @NotBlank
    @Schema(description = "Nome do item", example = "Média", required = true)
    private String nome;

    @NotNull
    @PositiveOrZero
    @Schema(description = "Preço adicional deste item", example = "5.00", required = true)
    private BigDecimal precoAdicional;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrecoAdicional() { return precoAdicional; }
    public void setPrecoAdicional(BigDecimal precoAdicional) { this.precoAdicional = precoAdicional; }
}