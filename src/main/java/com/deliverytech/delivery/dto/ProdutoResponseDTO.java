package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * DTO usado para enviar informações de produtos para o cliente (resposta da API).
 * Documentado no Swagger para facilitar a visualização e entendimento dos dados.
 */
@Schema(description = "DTO de resposta com dados do produto")
public class ProdutoResponseDTO {

    // ---------------------------------------------------
    // ID do produto
    // Exemplo: 1
    // Usado para identificar unicamente o produto
    // ---------------------------------------------------
    @Schema(description = "ID do produto", example = "1")
    private Long id;

    // ---------------------------------------------------
    // Nome do produto
    // Exemplo: "Pizza Margherita"
    // Informativo para o usuário/cliente
    // ---------------------------------------------------
    @Schema(description = "Nome do produto", example = "Pizza Margherita")
    private String nome;

    // ---------------------------------------------------
    // Descrição do produto
    // Exemplo: "Pizza de massa fina com queijo e tomate"
    // Pode ser usado para exibir detalhes do produto no front-end
    // ---------------------------------------------------
    @Schema(description = "Descrição do produto", example = "Pizza de massa fina com queijo e tomate")
    private String descricao;

    // ---------------------------------------------------
    // Preço do produto
    // Exemplo: 35.50
    // Mostra o valor que será cobrado
    // ---------------------------------------------------
    @Schema(description = "Preço do produto", example = "35.50")
    private BigDecimal preco;

    // ---------------------------------------------------
    // Disponibilidade do produto
    // Exemplo: true
    // Indica se o produto pode ser comprado
    // ---------------------------------------------------
    @Schema(description = "Indica se o produto está disponível", example = "true")
    private Boolean disponivel;

    // ---------------------------------------------------
    // ID do restaurante
    // Exemplo: 2
    // Informa de qual restaurante o produto pertence
    // ---------------------------------------------------
    @Schema(description = "ID do restaurante do produto", example = "2")
    private Long restauranteId;

    // ---------------------------------------------------
    // Categoria do produto
    // Exemplo: "Italiana"
    // Pode ser usado para filtros e exibição no front-end
    // ---------------------------------------------------
    @Schema(description = "Categoria do produto", example = "Italiana")
    private String categoria;

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
}
