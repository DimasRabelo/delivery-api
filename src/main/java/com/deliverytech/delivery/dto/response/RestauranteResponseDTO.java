package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

import com.deliverytech.delivery.dto.request.EnderecoDTO;

@Schema(description = "DTO de resposta com dados do restaurante")
public class RestauranteResponseDTO {

    @Schema(description = "ID do restaurante", example = "2")
    private Long id;

    @Schema(description = "Nome do restaurante", example = "Pizza Express")
    private String nome;

    @Schema(description = "Categoria do restaurante", example = "Italiana")
    private String categoria;

    // ---------------------------------------------------
    // CORREÇÃO DO BUG "toString()"
    // ---------------------------------------------------
    @Schema(description = "Objeto JSON com o endereço do restaurante")
    private EnderecoDTO endereco; // <-- 2. TIPO MUDADO DE String PARA EnderecoDTO

    @Schema(description = "Telefone do restaurante", example = "11999999999")
    private String telefone;

    @Schema(description = "Taxa de entrega", example = "5.50")
    private BigDecimal taxaEntrega;

    @Schema(description = "Indica se o restaurante está ativo", example = "true")
    private Boolean ativo;

    // ===================================================
    // GETTERS E SETTERS (CORRIGIDOS)
    // ===================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    // 3. GETTER E SETTER ATUALIZADOS
    public EnderecoDTO getEndereco() { return endereco; }
    public void setEndereco(EnderecoDTO endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}