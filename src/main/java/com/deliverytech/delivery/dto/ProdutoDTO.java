package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.validation.constraints.*; // Importações para Validação (Bean Validation)
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para criar ou atualizar um Produto.
 * Define o "shape" dos dados que a API espera receber no corpo (body)
 * da requisição para endpoints de produto.
 */
@Schema(description = "Dados para cadastro ou atualização de produto") // Documentação a nível de classe
public class ProdutoDTO {

    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true) // Documentação Swagger
    @NotBlank(message = "Nome é obrigatório") // Validação: Não pode ser nulo ou vazio
    @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres") // Validação: Tamanho
    private String nome;

    @Schema(description = "Descrição do produto", example = "Pizza com molho de tomate, mussarela e manjericão", required = true) // Documentação Swagger
    @NotBlank(message = "Descrição é obrigatória") // Validação
    @Size(min = 10, max = 500, message = "Descrição deve ter entre 10 e 500 caracteres") // Validação: Tamanho
    private String descricao;
    
    @Schema(description = "Preço do produto em reais", example = "25.50", minimum = "0.01", required = true) // Documentação Swagger
    @NotNull(message = "Preço é obrigatório") // Validação: Não pode ser nulo
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero") // Validação: Valor mínimo
    @DecimalMax(value = "500.00", message = "Preço não pode exceder R$ 500,00") // Validação: Valor máximo
    private BigDecimal preco;
    
    @Schema(description = "Categoria do produto", example = "Italiana", required = true) // Documentação Swagger
    @NotBlank(message = "Categoria é obrigatória") // Validação
    private String categoria;
    
    @Schema(description = "ID do restaurante ao qual o produto pertence", example = "1", required = true) // Documentação Swagger
    @NotNull(message = "Restaurante ID é obrigatório") // Validação
    @Positive(message = "Restaurante ID deve ser positivo") // Validação: Deve ser um número maior que zero
    private Long restauranteId;
    
    @Schema(description = "Disponibilidade do produto", example = "true") // Documentação Swagger
    @AssertTrue(message = "Produto deve estar disponível por padrão") // Validação: Garante que o valor (quando enviado) seja 'true'
    private Boolean disponivel = true; // Define 'true' como valor padrão
    
    @Schema(description = "URL da imagem do produto", example = "https://meusite.com/pizza.jpg") // Documentação Swagger
    @Pattern( // Validação: Garante que o texto siga uma Expressão Regular (Regex)
        regexp = "^(https?://).+\\.(jpg|jpeg|png|gif)$", // Regex: Deve começar com http/https e terminar com .jpg, .jpeg, .png, ou .gif
        message = "URL da imagem deve ser válida e ter formato JPG, JPEG, PNG ou GIF"
    )
    private String imagemUrl;

    // --- CAMPO ADICIONADO ---
    
    @Schema(description = "Quantidade em estoque", example = "50", required = true) // Documentação Swagger
    @NotNull(message = "Estoque é obrigatório") // Validação
    @Min(value = 0, message = "Estoque não pode ser negativo") // Validação: Valor mínimo é zero
    private int estoque;
    
    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    
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

    // --- Getters e Setters do campo adicionado ---
    
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }
}