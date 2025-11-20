package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.deliverytech.delivery.validation.ValidTelefone;
import com.deliverytech.delivery.validation.ValidCategoria;
import com.deliverytech.delivery.validation.ValidHorarioFuncionamento;
import jakarta.validation.Valid;
import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para o cadastro ou atualização
 * de um Restaurante.
 *
 * @implNote O campo 'endereco' é um DTO aninhado (EnderecoDTO) e não
 * mais uma String simples. O campo 'email' foi omitido deste DTO
 * por simplicidade.
 */
@Schema(description = "Dados para cadastro ou atualização de um Restaurante")
public class RestauranteDTO {

    @Schema(description = "Nome do restaurante", example = "Pizza Frango", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @Schema(description = "Categoria do restaurante", example = "Italiana")
    @NotNull(message = "Categoria é obrigatória")
    @ValidCategoria
    private String categoria;

    @Schema(description = "Endereço estruturado do restaurante", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Endereço é obrigatório")
    @Valid // <-- Adicionado: Valida o EnderecoDTO aninhado
    private EnderecoDTO endereco;

    @Schema(description = "Telefone para contato", example = "11999999999")
    @NotBlank(message = "Telefone é obrigatório")
    @ValidTelefone
    private String telefone;

    @Schema(description = "Taxa de entrega em reais", example = "5.50")
    @NotNull(message = "Taxa de entrega é obrigatória")
    @DecimalMin(value = "0.00", inclusive = true) // Permite taxa zero (frete grátis)
    @DecimalMax(value = "50.0", message = "Taxa de entrega não pode exceder R$ 50,00")
    private BigDecimal taxaEntrega;

    @Schema(description = "Tempo de entrega em minutos", example = "45")
    @NotNull(message = "Tempo de entrega é obrigatório")
    @Min(value = 10) @Max(value = 120)
    private Integer tempoEntrega;

    @Schema(description = "Horário de funcionamento", example = "08:00-22:00")
    @NotBlank(message = "Horário de funcionamento é obrigatório")
    @ValidHorarioFuncionamento
    private String horarioFuncionamento;

    @Schema(description = "Indica se o restaurante está ativo", example = "true")
    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

    @Schema(description = "Avaliação do restaurante", example = "4.5")
    @NotNull(message = "Avaliação é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal avaliacao;
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public EnderecoDTO getEndereco() { return endereco; }
    public void setEndereco(EnderecoDTO endereco) { this.endereco = endereco; }

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