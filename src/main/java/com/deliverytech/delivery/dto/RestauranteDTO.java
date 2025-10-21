package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import com.deliverytech.delivery.validation.ValidTelefone;
import com.deliverytech.delivery.validation.ValidCategoria;
import java.math.BigDecimal;

/**
 * DTO usado para enviar ou receber informações de um restaurante
 * quando for criar ou atualizar registros.
 * Documentado no Swagger para que a API mostre claramente os campos.
 */
@Schema(description = "Dados para cadastro de restaurante")
public class RestauranteDTO {

    @Schema(description = "Nome do restaurante", example = "Pizza Frango", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "Categoria do restaurante", example = "Italiana")
    @NotNull(message = "Categoria é obrigatória")
    @ValidCategoria
    private String categoria;

    @Schema(description = "Telefone para contato", example = "11999999999")
    @NotBlank(message = "Telefone é obrigatório")
    @ValidTelefone
    private String telefone;

    @Schema(description = "Taxa de entrega em reais", example = "5.50", minimum = "0", maximum = "50")
    @NotNull(message = "Taxa de entrega é obrigatória")
    @DecimalMin(value = "0.01", inclusive = true, message = "Taxa de entrega deve ser positiva")
    @DecimalMax(value = "50.0", message = "Taxa de entrega não pode exceder R$ 50,00")
    private BigDecimal taxaEntrega;

    @Schema(description = "Tempo de entrega em minutos", example = "45")
    @NotNull(message = "Tempo de entrega é obrigatório")
    @Min(value = 10, message = "Tempo mínimo de entrega é 10 minutos")
    @Max(value = 120, message = "Tempo máximo de entrega é 120 minutos")
    private Integer tempoEntrega;

    @Schema(description = "Endereço completo do restaurante", example = "Rua das Flores, 123 - Centro")
    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 200, message = "Endereço não pode exceder 200 caracteres")
    private String endereco;

    @Schema(description = "Email para contato do restaurante", example = "contato@restaurante.com")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @Schema(description = "Avaliação do restaurante", example = "4.5", minimum = "0")
    @NotNull(message = "Avaliação é obrigatória")
    @DecimalMin(value = "0.0", inclusive = true, message = "Avaliação não pode ser negativa")
    private BigDecimal avaliacao;

    @Schema(description = "Indica se o restaurante está ativo", example = "true")
    @NotNull(message = "Status ativo é obrigatório")
    private Boolean ativo;

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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
