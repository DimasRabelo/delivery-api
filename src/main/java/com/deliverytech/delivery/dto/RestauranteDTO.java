package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.validation.constraints.*; // Importações para Validação (Bean Validation)
import com.deliverytech.delivery.validation.ValidTelefone; // Importações das suas validações customizadas
import com.deliverytech.delivery.validation.ValidCategoria;
import com.deliverytech.delivery.validation.ValidHorarioFuncionamento;

import java.math.BigDecimal;

/**
 * DTO (Data Transfer Object) para criar ou atualizar um Restaurante.
 * Esta classe define a "forma" dos dados (o Schema) que a API espera
 * receber no corpo (body) de uma requisição.
 * Ela inclui validações (Bean Validation) para garantir a integridade dos dados.
 */
@Schema(description = "Dados para cadastro ou atualização de um Restaurante") // Documentação a nível de classe
public class RestauranteDTO {

    // --- Informações Básicas ---

    @Schema(description = "Nome do restaurante", example = "Pizza Frango", required = true) // Documentação Swagger: Campo, exemplo e obrigatoriedade
    @NotBlank(message = "Nome é obrigatório") // Validação: Não pode ser nulo ou conter apenas espaços em branco
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") // Validação: Define o tamanho
    private String nome;

    @Schema(description = "Categoria do restaurante", example = "Italiana") // Documentação Swagger
    @NotNull(message = "Categoria é obrigatória") // Validação: Não pode ser nulo
    @ValidCategoria // Validação Customizada: Verifica se a categoria é uma das permitidas
    private String categoria;

    @Schema(description = "Endereço completo do restaurante", example = "Rua das Flores, 123 - Centro") // Documentação Swagger
    @NotBlank(message = "Endereço é obrigatório") // Validação
    @Size(max = 200, message = "Endereço não pode exceder 200 caracteres") // Validação: Tamanho máximo
    private String endereco;

    // --- Informações de Contato ---

    @Schema(description = "Telefone para contato", example = "11999999999") // Documentação Swagger
    @NotBlank(message = "Telefone é obrigatório") // Validação
    @ValidTelefone // Validação Customizada: Verifica o formato do telefone
    private String telefone;

    @Schema(description = "Email para contato do restaurante", example = "contato@restaurante.com") // Documentação Swagger
    @Email(message = "Email deve ter formato válido") // Validação: Verifica se segue o padrão de e-mail
    private String email;

    // --- Informações Operacionais ---

    @Schema(description = "Taxa de entrega em reais", example = "5.50", minimum = "0", maximum = "50") // Documentação Swagger
    @NotNull(message = "Taxa de entrega é obrigatória") // Validação
    @DecimalMin(value = "0.01", inclusive = true, message = "Taxa de entrega deve ser positiva") // Validação: Valor mínimo
    @DecimalMax(value = "50.0", message = "Taxa de entrega não pode exceder R$ 50,00") // Validação: Valor máximo
    private BigDecimal taxaEntrega;

    @Schema(description = "Tempo de entrega em minutos", example = "45") // Documentação Swagger
    @NotNull(message = "Tempo de entrega é obrigatório") // Validação
    @Min(value = 10, message = "Tempo mínimo de entrega é 10 minutos") // Validação: Mínimo
    @Max(value = 120, message = "Tempo máximo de entrega é 120 minutos") // Validação: Máximo
    private Integer tempoEntrega;

    @Schema(description = "Horário de funcionamento do restaurante", example = "08:00-22:00") // Documentação Swagger
    @NotBlank(message = "Horário de funcionamento é obrigatório") // Validação
    @ValidHorarioFuncionamento // Validação Customizada: Verifica o formato "HH:mm-HH:mm"
    private String horarioFuncionamento;

    @Schema(description = "Indica se o restaurante está ativo", example = "true") // Documentação Swagger
    @NotNull(message = "Status ativo é obrigatório") // Validação
    private Boolean ativo;

    // --- Informações Adicionais ---
    
    @Schema(description = "Avaliação do restaurante", example = "4.5", minimum = "0") // Documentação Swagger
    @NotNull(message = "Avaliação é obrigatória") // Validação
    @DecimalMin(value = "0.0", inclusive = true, message = "Avaliação não pode ser negativa") // Validação: Mínimo
    private BigDecimal avaliacao;

    
    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
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