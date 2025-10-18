package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Dados para cadastro de restaurante")
public class RestauranteDTO {

    @Schema(description = "Nome do restaurante", example = "Pizza Express", required = true)
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Schema(description = "Categoria do restaurante", example = "Italiana", allowableValues = {"Italiana", "Brasileira", "Japonesa", "Mexicana", "Árabe"})
    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;

    @Schema(description = "Endereço completo do restaurante", example = "Rua das Flores, 123 - Centro")
    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;

    @Schema(description = "Telefone para contato", example = "11999999999")
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    @Schema(description = "Taxa de entrega em reais", example = "5.50", minimum = "0")
    @NotNull(message = "Taxa de entrega é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Taxa de entrega não pode ser negativa")
    private BigDecimal taxaEntrega;

    @Schema(description = "Avaliação do restaurante", example = "4.5", minimum = "0")
    @NotNull(message = "Avaliação é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Avaliação não pode ser negativa")
    private BigDecimal avaliacao;

    @Schema(description = "Indica se o restaurante está ativo", example = "true")
    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    // GETTERS E SETTERS
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public BigDecimal getAvaliacao() { return avaliacao; }
    public void setAvaliacao(BigDecimal avaliacao) { this.avaliacao = avaliacao; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
