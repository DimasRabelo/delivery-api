package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO usado para criar ou atualizar produtos.
 * Documentado no Swagger para fornecer informações claras sobre cada campo.
 */
@Schema(description = "Dados para cadastro ou atualização de produto")
public class ProdutoDTO {

    // ... (campos nome, descricao, preco, categoria, restauranteId, disponivel, imagemUrl) ...
    // [Seus campos existentes permanecem iguais]

    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
    private String nome;

    @Schema(description = "Descrição do produto", example = "Pizza com molho de tomate, mussarela e manjericão", required = true)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres")
    private String descricao;
    
    @Schema(description = "Preço do produto em reais", example = "25.50", minimum = "0.01", required = true)
    @NotNull(message = "Preço é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    @DecimalMax(value = "500.00", message = "Preço não pode exceder R$ 500,00")
    private BigDecimal preco;
    
    @Schema(description = "Categoria do produto", example = "Italiana", required = true)
    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;
    
    @Schema(description = "ID do restaurante ao qual o produto pertence", example = "1", required = true)
    @NotNull(message = "Restaurante ID é obrigatório")
    @Positive(message = "Restaurante ID deve ser positivo")
    private Long restauranteId;
    
    @Schema(description = "Disponibilidade do produto", example = "true")
    @AssertTrue(message = "Produto deve estar disponível por padrão")
    private Boolean disponivel = true;
    
    @Schema(description = "URL da imagem do produto", example = "https://meusite.com/pizza.jpg")
    @Pattern(
        regexp = "^(https?://).+\\.(jpg|jpeg|png|gif)$",
        message = "URL da imagem deve ser válida e ter formato JPG, JPEG, PNG ou GIF"
    )
    private String imagemUrl;

    // ==============================================
    // ADICIONE ESTE CAMPO
    // ==============================================
    @Schema(description = "Quantidade em estoque", example = "50", required = true)
    @NotNull(message = "Estoque é obrigatório")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private int estoque;
    // ==============================================


    // =======================
    // GETTERS E SETTERS
    // =======================
    
    // ... (getters/setters existentes) ...

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

    // ==============================================
    // ADICIONE ESTES GETTERS E SETTERS
    // ==============================================
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }
}