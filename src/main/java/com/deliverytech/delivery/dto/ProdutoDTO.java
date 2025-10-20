package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO usado para criar ou atualizar produtos.
 * Documentado no Swagger para fornecer informações claras sobre cada campo.
 */
@Schema(description = "Dados para cadastro ou atualização de produto")
public class ProdutoDTO {

    // ---------------------------------------------------
    // Nome do produto
    // Exemplo: "Pizza Margherita"
    // Campo obrigatório
    // ---------------------------------------------------
    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true)
    @NotBlank(message = "Nome do produto é obrigatório")
    private String nome;

    // ---------------------------------------------------
    // Descrição do produto
    // Exemplo: "Pizza com molho de tomate, mussarela e manjericão"
    // Não obrigatório
    // ---------------------------------------------------
    @Schema(description = "Descrição do produto", example = "Pizza com molho de tomate, mussarela e manjericão")
    private String descricao;

    // ---------------------------------------------------
    // Preço do produto
    // Exemplo: 25.50
    // Campo obrigatório, mínimo 0.01
    // ---------------------------------------------------
    @Schema(description = "Preço do produto em reais", example = "25.50", minimum = "0.01", required = true)
    @NotNull(message = "Preço do produto é obrigatório")
    @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
    private BigDecimal preco;

    // ---------------------------------------------------
    // ID do restaurante ao qual o produto pertence
    // Exemplo: 1
    // Campo obrigatório
    // ---------------------------------------------------
    @Schema(description = "ID do restaurante ao qual o produto pertence", example = "1", required = true)
    @NotNull(message = "ID do restaurante é obrigatório")
    private Long restauranteId;

    // ---------------------------------------------------
    // Disponibilidade do produto
    // Exemplo: true
    // Campo opcional
    // ---------------------------------------------------
    @Schema(description = "Disponibilidade do produto", example = "true")
    private Boolean disponivel;

    // ---------------------------------------------------
    // Categoria do produto
    // Exemplo: "Italiana"
    // Campo opcional
    // ---------------------------------------------------
    @Schema(description = "Categoria do produto", example = "Italiana")
    private String categoria;

    // =======================
    // GETTERS E SETTERS
    // =======================
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}
