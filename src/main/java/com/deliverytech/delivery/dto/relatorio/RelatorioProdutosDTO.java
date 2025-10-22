package com.deliverytech.delivery.dto.relatorio;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Relatório dos produtos mais vendidos")
public class RelatorioProdutosDTO {

    @Schema(description = "Nome do produto")
    private String produtoNome;

    @Schema(description = "Categoria do produto")
    private String categoria;

    @Schema(description = "Quantidade total vendida")
    private Integer totalVendido;

    @Schema(description = "Total de faturamento com o produto")
    private BigDecimal receitaTotal;

    public RelatorioProdutosDTO() {}

    public RelatorioProdutosDTO(String produtoNome, String categoria, Integer totalVendido, BigDecimal receitaTotal) {
        this.produtoNome = produtoNome;
        this.categoria = categoria;
        this.totalVendido = totalVendido;
        this.receitaTotal = receitaTotal;
    }

    public String getProdutoNome() { return produtoNome; }
    public void setProdutoNome(String produtoNome) { this.produtoNome = produtoNome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public Integer getTotalVendido() { return totalVendido; } // usado no Comparator
    public void setTotalVendido(Integer totalVendido) { this.totalVendido = totalVendido; }

    public BigDecimal getReceitaTotal() { return receitaTotal; }
    public void setReceitaTotal(BigDecimal receitaTotal) { this.receitaTotal = receitaTotal; }
}
