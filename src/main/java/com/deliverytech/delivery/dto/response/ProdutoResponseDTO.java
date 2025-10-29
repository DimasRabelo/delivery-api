package com.deliverytech.delivery.dto.response;

import com.deliverytech.delivery.entity.Produto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO usado para enviar informações de produtos para o cliente (resposta da API).
 * Documentado no Swagger para facilitar a visualização e entendimento dos dados.
 */
@Schema(description = "DTO de resposta com dados do produto")
public class ProdutoResponseDTO {

    @Schema(description = "ID do produto", example = "1")
    private Long id;

    @Schema(description = "Nome do produto", example = "Pizza Margherita")
    private String nome;

    @Schema(description = "Descrição do produto", example = "Pizza de massa fina com queijo e tomate")
    private String descricao;

    @Schema(description = "Preço do produto", example = "35.50")
    private BigDecimal preco;

    @Schema(description = "Indica se o produto está disponível", example = "true")
    private Boolean disponivel;

    @Schema(description = "ID do restaurante do produto", example = "2")
    private Long restauranteId;

    @Schema(description = "Categoria do produto", example = "Italiana")
    private String categoria;

    // ==============================================
    // ADICIONE ESTE CAMPO
    // ==============================================
    @Schema(description = "Quantidade em estoque", example = "50")
    private int estoque;
    // ==============================================


    // =======================
    // CONSTRUTOR VAZIO
    // =======================
    public ProdutoResponseDTO() {}

    // =======================
    // CONSTRUTOR COM PRODUTO
    // =======================
    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.descricao = produto.getDescricao();
        this.preco = produto.getPreco();
        this.disponivel = produto.getDisponivel();
        this.restauranteId = produto.getRestaurante() != null ? produto.getRestaurante().getId() : null;
        this.categoria = produto.getCategoria();
        this.estoque = produto.getEstoque();
    }

    // =======================
    // GETTERS E SETTERS
    // =======================
    
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