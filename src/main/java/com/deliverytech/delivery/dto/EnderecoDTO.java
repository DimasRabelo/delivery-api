package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para dados de endereço estruturado")
public class EnderecoDTO {

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos (apenas números)")
    @Schema(description = "CEP (apenas números)", example = "01001000", required = true)
    private String cep;

    @NotBlank(message = "Rua é obrigatória")
    @Schema(description = "Nome da rua/logradouro", example = "Rua das Flores", required = true)
    private String rua;

    @NotBlank(message = "Número é obrigatório")
    @Schema(description = "Número", example = "123", required = true)
    private String numero;

    @Schema(description = "Complemento (apto, bloco, etc.)", example = "Apto 101")
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Schema(description = "Bairro", example = "Centro", required = true)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Schema(description = "Cidade", example = "São Paulo", required = true)
    private String cidade;

    @NotBlank(message = "Estado (UF) é obrigatório")
    @Size(min = 2, max = 2, message = "UF deve ter 2 caracteres")
    @Schema(description = "Sigla do estado (UF)", example = "SP", required = true)
    private String estado;
    
    // (Opcional: Latitude e Longitude se o front-end for enviar)
    // private BigDecimal latitude;
    // private BigDecimal longitude;

    // Getters e Setters
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getRua() { return rua; }
    public void setRua(String rua) { this.rua = rua; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}