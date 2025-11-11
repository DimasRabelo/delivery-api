package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para *retornar* dados de endereço para o front-end.
 * (Diferente do EnderecoDTO, este inclui o ID).
 */
@Schema(description = "DTO para exibir dados de endereço")
public class EnderecoResponseDTO {

    @Schema(description = "ID único do endereço", example = "1")
    private Long id; 

    @Schema(description = "Nome da rua/logradouro", example = "Rua das Flores")
    private String rua;

    @Schema(description = "Número", example = "123")
    private String numero;

    @Schema(description = "Complemento (apto, bloco, etc.)", example = "Apto 101")
    private String complemento;

    @Schema(description = "Bairro", example = "Centro")
    private String bairro;

    @Schema(description = "Cidade", example = "São Paulo")
    private String cidade;

    @Schema(description = "Sigla do estado (UF)", example = "SP")
    private String estado;

    @Schema(description = "Apelido do endereço", example = "Casa")
    private String apelido;

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }
}