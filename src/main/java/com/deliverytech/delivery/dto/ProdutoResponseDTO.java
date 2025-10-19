package com.deliverytech.delivery.dto;

import com.deliverytech.delivery.entity.Produto; 
import java.math.BigDecimal;

public class ProdutoResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Boolean disponivel;
    private Long restauranteId;
    private String categoria;

    // 2. ADICIONADO CONSTRUTOR VAZIO (BOA PRÁTICA)
    public ProdutoResponseDTO() {
    }

    // 3. ADICIONADO CONSTRUTOR QUE RECEBE A ENTIDADE
    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.descricao = produto.getDescricao();
        this.preco = produto.getPreco();
        this.disponivel = produto.getDisponivel();
        this.categoria = produto.getCategoria();

        // Mapeia o ID do restaurante a partir do objeto (assumindo que existe getRestaurante())
        if (produto.getRestaurante() != null) {
            this.restauranteId = produto.getRestaurante().getId();
        }
    }

    // Getters e Setters (os mesmos que você enviou)
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