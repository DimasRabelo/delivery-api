package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO (Data Transfer Object) para o cadastro ou atualização
 * de um Produto, incluindo seus grupos de opcionais.
 *
 * @implNote O campo 'disponivel' não está neste DTO;
 * o serviço definirá a disponibilidade (ex: 'true')
 * como padrão ao criar um novo produto.
 */
@Schema(description = "Dados para cadastro ou atualização de produto")
public class ProdutoDTO {

    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 50)
    private String nome;

    @Schema(description = "Descrição do produto", example = "Pizza com molho de tomate...", required = true)
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 500)
    private String descricao;
    
    @Schema(description = "Preço base do produto", example = "25.50", minimum = "0.00", required = true)
    @NotNull(message = "Preço base é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço base deve ser zero ou positivo") // Pode ser 0.00 se o tamanho definir o preço
    @DecimalMax(value = "500.00")
    private BigDecimal precoBase;
    
    @Schema(description = "Categoria do produto", example = "Italiana", required = true)
    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;
    
    @Schema(description = "ID do restaurante", example = "1", required = true)
    @NotNull(message = "Restaurante ID é obrigatório")
    @Positive(message = "Restaurante ID deve ser positivo")
    private Long restauranteId;
    
    @Schema(description = "Quantidade em estoque", example = "50", required = true)
    @NotNull(message = "Estoque é obrigatório")
    @Min(value = 0, message = "Estoque não pode ser negativo")
    private int estoque;
    
    @Valid // Valida os grupos aninhados
    @Schema(description = "Lista de grupos de opcionais para este produto (Tamanho, Adicionais, etc.)")
    private List<GrupoOpcionalDTO> gruposOpcionais = new ArrayList<>();

    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }
    
    public List<GrupoOpcionalDTO> getGruposOpcionais() { return gruposOpcionais; }
    public void setGruposOpcionais(List<GrupoOpcionalDTO> gruposOpcionais) { this.gruposOpcionais = gruposOpcionais; }
}