package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "DTO para um grupo de opcionais (ex: 'Tamanho')")
public class GrupoOpcionalDTO {

    @Schema(description = "ID do grupo (usado apenas para atualização)")
    private Long id; // Útil para o método de 'atualizarProduto'

    @NotBlank
    @Schema(description = "Nome do grupo", example = "Escolha o Tamanho", required = true)
    private String nome;

    @Min(0)
    @Schema(description = "Mínimo de seleções", example = "1")
    private int minSelecao = 1;

    @Min(1)
    @Schema(description = "Máximo de seleções", example = "1")
    private int maxSelecao = 1;

    @Valid // Valida os DTOs aninhados
    @NotEmpty
    @Schema(description = "Lista de itens de opção dentro deste grupo")
    private List<ItemOpcionalDTO> itensOpcionais;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getMinSelecao() { return minSelecao; }
    public void setMinSelecao(int minSelecao) { this.minSelecao = minSelecao; }
    public int getMaxSelecao() { return maxSelecao; }
    public void setMaxSelecao(int maxSelecao) { this.maxSelecao = maxSelecao; }
    public List<ItemOpcionalDTO> getItensOpcionais() { return itensOpcionais; }
    public void setItensOpcionais(List<ItemOpcionalDTO> itensOpcionais) { this.itensOpcionais = itensOpcionais; }
}