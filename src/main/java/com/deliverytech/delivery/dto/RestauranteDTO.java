package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO usado para enviar ou receber informações de um restaurante
 * quando for criar ou atualizar registros.
 * Documentado no Swagger para que a API mostre claramente os campos.
 */
@Schema(description = "Dados para cadastro de restaurante")
public class RestauranteDTO {

    // ---------------------------------------------------
    // Nome do restaurante
    // Exemplo: "Pizza Frango"
    // Campo obrigatório para identificar o restaurante
    // ---------------------------------------------------
    @Schema(description = "Nome do restaurante", example = "Pizza Frango", required = true)
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    // ---------------------------------------------------
    // Categoria do restaurante
    // Exemplo: "Italiana", pode ter valores permitidos
    // Útil para filtros e organização de restaurantes
    // ---------------------------------------------------
    @Schema(description = "Categoria do restaurante", example = "Italiana", allowableValues = {"Italiana", "Brasileira", "Japonesa", "Mexicana", "Árabe"})
    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;

    // ---------------------------------------------------
    // Endereço completo
    // Exemplo: "Rua das Flores, 123 - Centro"
    // Campo obrigatório para entrega e localização
    // ---------------------------------------------------
    @Schema(description = "Endereço completo do restaurante", example = "Rua das Flores, 123 - Centro")
    @NotBlank(message = "Endereço é obrigatório")
    private String endereco;

    // ---------------------------------------------------
    // Telefone de contato
    // Exemplo: "11999999999"
    // Necessário para comunicação com clientes
    // ---------------------------------------------------
    @Schema(description = "Telefone para contato", example = "11999999999")
    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    // ---------------------------------------------------
    // Taxa de entrega
    // Exemplo: 5.50
    // Não pode ser negativa
    // ---------------------------------------------------
    @Schema(description = "Taxa de entrega em reais", example = "5.50", minimum = "0")
    @NotNull(message = "Taxa de entrega é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Taxa de entrega não pode ser negativa")
    private BigDecimal taxaEntrega;

    // ---------------------------------------------------
    // Avaliação do restaurante
    // Exemplo: 4.5
    // Representa nota média de clientes
    // ---------------------------------------------------
    @Schema(description = "Avaliação do restaurante", example = "4.5", minimum = "0")
    @NotNull(message = "Avaliação é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Avaliação não pode ser negativa")
    private BigDecimal avaliacao;

    // ---------------------------------------------------
    // Status ativo/inativo
    // Exemplo: true
    // Controla se o restaurante aparece para pedidos
    // ---------------------------------------------------
    @Schema(description = "Indica se o restaurante está ativo", example = "true")
    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    // ---------------------------------------------------
    // Tempo de entrega estimado
    // Exemplo: 45 (minutos)
    // Útil para informar o cliente sobre previsão de entrega
    // ---------------------------------------------------
    @Schema(description = "Tempo de entrega em minutos", example = "45")
    @NotNull(message = "Tempo de entrega é obrigatório")
    private Integer tempoEntrega;

    // ---------------------------------------------------
    // Horário de funcionamento
    // Exemplo: "08:00-22:00"
    // Informativo para saber quando o restaurante está aberto
    // ---------------------------------------------------
    @Schema(description = "Horário de funcionamento do restaurante", example = "08:00-22:00")
    @NotBlank(message = "Horário de funcionamento é obrigatório")
    private String horarioFuncionamento;

    // =======================
    // GETTERS E SETTERS
    // =======================
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

    public BigDecimal getAvaliacao() { return avaliacao; }
    public void setAvaliacao(BigDecimal avaliacao) { this.avaliacao = avaliacao; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Integer getTempoEntrega() { return tempoEntrega; }
    public void setTempoEntrega(Integer tempoEntrega) { this.tempoEntrega = tempoEntrega; }

    public String getHorarioFuncionamento() { return horarioFuncionamento; }
    public void setHorarioFuncionamento(String horarioFuncionamento) { this.horarioFuncionamento = horarioFuncionamento; }
}
