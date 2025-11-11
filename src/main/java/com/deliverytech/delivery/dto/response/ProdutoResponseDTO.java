package com.deliverytech.delivery.dto.response;

import com.deliverytech.delivery.dto.request.GrupoOpcionalDTO;
import com.deliverytech.delivery.dto.request.ItemOpcionalDTO;
import com.deliverytech.delivery.entity.Produto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * DTO (Data Transfer Object) de resposta que encapsula
 * todos os dados de um Produto, incluindo seu preço base
 * e a lista de grupos de opcionais.
 */
@Schema(description = "DTO de resposta com dados do produto (incluindo opcionais)")
public class ProdutoResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do produto", example = "1")
    private Long id;

    @Schema(description = "Nome do produto", example = "Pizza Margherita")
    private String nome;

    @Schema(description = "Descrição do produto", example = "Pizza de massa fina...")
    private String descricao;

    @Schema(description = "Preço BASE do produto (sem opcionais)", example = "35.50")
    private BigDecimal precoBase;

    @Schema(description = "Indica se o produto está disponível", example = "true")
    private Boolean disponivel;

    @Schema(description = "ID do restaurante do produto", example = "2")
    private Long restauranteId;

    @Schema(description = "Categoria do produto", example = "Italiana")
    private String categoria;
    
    @Schema(description = "Quantidade em estoque", example = "50")
    private int estoque;
    
    @Schema(description = "Lista de grupos de opcionais para este produto (Tamanho, Adicionais, etc.)")
    private List<GrupoOpcionalDTO> gruposOpcionais = new ArrayList<>();


    public ProdutoResponseDTO() {}

    /**
     * Construtor de Mapeamento.
     * Converte a Entidade 'Produto' (com 'precoBase' e 'gruposOpcionais') 
     * para este DTO de resposta.
     */
    public ProdutoResponseDTO(Produto produto) {
        this.id = produto.getId();
        this.nome = produto.getNome();
        this.descricao = produto.getDescricao();
        this.disponivel = produto.getDisponivel();
        this.restauranteId = produto.getRestaurante() != null ? produto.getRestaurante().getId() : null;
        this.categoria = produto.getCategoria();
        this.estoque = produto.getEstoque();

        this.precoBase = produto.getPrecoBase();

        // Mapeia as entidades de opcionais para DTOs de opcionais
        if (produto.getGruposOpcionais() != null) {
            this.gruposOpcionais = produto.getGruposOpcionais().stream()
                .map(grupo -> {
                    GrupoOpcionalDTO grupoDTO = new GrupoOpcionalDTO();
                    grupoDTO.setId(grupo.getId());
                    grupoDTO.setNome(grupo.getNome());
                    grupoDTO.setMinSelecao(grupo.getMinSelecao());
                    grupoDTO.setMaxSelecao(grupo.getMaxSelecao());
                    
                    // Mapeia os itens aninhados
                    if (grupo.getItensOpcionais() != null) {
                        grupoDTO.setItensOpcionais(grupo.getItensOpcionais().stream()
                            .map(item -> {
                                ItemOpcionalDTO itemDTO = new ItemOpcionalDTO();
                                itemDTO.setId(item.getId());
                                itemDTO.setNome(item.getNome());
                                itemDTO.setPrecoAdicional(item.getPrecoAdicional());
                                return itemDTO;
                            }).collect(Collectors.toList()));
                    }
                    return grupoDTO;
                }).collect(Collectors.toList());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }

    public Boolean getDisponivel() { return disponivel; }
    public void setDisponivel(Boolean disponivel) { this.disponivel = disponivel; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public List<GrupoOpcionalDTO> getGruposOpcionais() { return gruposOpcionais; }
    public void setGruposOpcionais(List<GrupoOpcionalDTO> gruposOpcionais) { this.gruposOpcionais = gruposOpcionais; }
}