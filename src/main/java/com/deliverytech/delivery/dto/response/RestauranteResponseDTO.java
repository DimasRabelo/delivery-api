package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) usado para enviar dados do restaurante para o cliente (front-end)
 * sem expor a entidade JPA diretamente.
 * Documentado no Swagger para que quem acessar a API veja claramente os campos retornados.
 */
@Schema(description = "DTO de resposta com dados do restaurante") // Documentação a nível de classe
public class RestauranteResponseDTO {

    // ---------------------------------------------------
    // ID do restaurante
    // Exemplo: 2
    // Identifica unicamente o restaurante na base de dados
    // ---------------------------------------------------
    @Schema(description = "ID do restaurante", example = "2") // Documentação Swagger
    private Long id;

    // ---------------------------------------------------
    // Nome do restaurante
    // Exemplo: "Pizza Express"
    // Campo principal exibido ao usuário
    // ---------------------------------------------------
    @Schema(description = "Nome do restaurante", example = "Pizza Express") // Documentação Swagger
    private String nome;

    // ---------------------------------------------------
    // Categoria do restaurante
    // Exemplo: "Italiana"
    // Para filtros e organização de restaurantes
    // ---------------------------------------------------
    @Schema(description = "Categoria do restaurante", example = "Italiana") // Documentação Swagger
    private String categoria;

    // ---------------------------------------------------
    // Endereço do restaurante
    // Exemplo: "Rua das Flores, 123"
    // Para exibir localização e referência
    // ---------------------------------------------------
    @Schema(description = "Endereço do restaurante", example = "Rua das Flores, 123") // Documentação Swagger
    private String endereco;

    // ---------------------------------------------------
    // Telefone do restaurante
    // Exemplo: "11999999999"
    // Contato direto para o cliente
    // ---------------------------------------------------
    @Schema(description = "Telefone do restaurante", example = "11999999999") // Documentação Swagger
    private String telefone;

    // ---------------------------------------------------
    // Taxa de entrega
    // Exemplo: 5.50
    // Informativo para o cálculo do pedido e exibição ao cliente
    // ---------------------------------------------------
    @Schema(description = "Taxa de entrega", example = "5.50") // Documentação Swagger
    private BigDecimal taxaEntrega;

    // ---------------------------------------------------
    // Status ativo/inativo
    // Exemplo: true
    // Determina se o restaurante está visível e disponível para pedidos
    // ---------------------------------------------------
    @Schema(description = "Indica se o restaurante está ativo", example = "true") // Documentação Swagger
    private Boolean ativo;

    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public BigDecimal getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(BigDecimal taxaEntrega) { this.taxaEntrega = taxaEntrega; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}