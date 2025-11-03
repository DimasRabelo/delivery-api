package com.deliverytech.delivery.dto.response;

import com.deliverytech.delivery.entity.Produto;
import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import java.math.BigDecimal;
import java.io.Serializable;

/**
 * DTO (Data Transfer Object) usado para enviar informações de produtos para o cliente (resposta da API).
 * Documentado no Swagger para facilitar a visualização e entendimento dos dados.
 */
@Schema(description = "DTO de resposta com dados do produto") // Documentação a nível de classe
public class ProdutoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "1") // Documentação Swagger
    private Long id;

    @Schema(description = "Nome do produto", example = "Pizza Margherita") // Documentação Swagger
    private String nome;

    @Schema(description = "Descrição do produto", example = "Pizza de massa fina com queijo e tomate") // Documentação Swagger
    private String descricao;

    @Schema(description = "Preço do produto", example = "35.50") // Documentação Swagger
    private BigDecimal preco;

    @Schema(description = "Indica se o produto está disponível", example = "true") // Documentação Swagger
    private Boolean disponivel;

    @Schema(description = "ID do restaurante do produto", example = "2") // Documentação Swagger
    private Long restauranteId;

    @Schema(description = "Categoria do produto", example = "Italiana") // Documentação Swagger
    private String categoria;

    // --- CAMPO ADICIONADO ---
    
    @Schema(description = "Quantidade em estoque", example = "50") // Documentação Swagger
    private int estoque;
    
    // =======================
    // CONSTRUTOR VAZIO
    // (Necessário para frameworks como Jackson/JPA)
    // =======================
    public ProdutoResponseDTO() {}

    // =======================
    // CONSTRUTOR MAPPER
    // (Boa prática para converter a Entidade em DTO)
    // =======================
    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.descricao = produto.getDescricao();
        this.preco = produto.getPreco();
        this.disponivel = produto.getDisponivel();
        // Tratamento seguro para evitar NullPointerException se o restaurante for nulo
        this.restauranteId = produto.getRestaurante() != null ? produto.getRestaurante().getId() : null;
        this.categoria = produto.getCategoria();
        this.estoque = produto.getEstoque();
    }

    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }
}