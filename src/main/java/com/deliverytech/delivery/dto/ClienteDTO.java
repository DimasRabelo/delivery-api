package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.validation.constraints.*; // Importações para Validação (Bean Validation)
import org.hibernate.validator.constraints.br.CPF; // Importação para validação de CPF específica do Brasil

/**
 * DTO (Data Transfer Object) para criar ou atualizar um Cliente.
 * Define o "shape" dos dados que a API espera receber no corpo (body)
 * da requisição para endpoints de cliente.
 */
@Schema(description = "Dados para cadastro ou atualização de cliente") // Documentação a nível de classe
public class ClienteDTO {

    @Schema(description = "Nome do cliente", example = "João Silva", required = true) // Documentação Swagger
    @NotBlank(message = "Nome é obrigatório") // Validação: Não pode ser nulo ou vazio
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") // Validação: Tamanho
    private String nome;

    @Schema(description = "Email do cliente", example = "joao.silva@email.com", required = true) // Documentação Swagger
    @NotBlank(message = "Email é obrigatório") // Validação
    @Email(message = "Email deve ter formato válido") // Validação: Verifica o formato de email
    private String email;

    @Schema(description = "Telefone do cliente", example = "11999999999", required = true) // Documentação Swagger
    @NotBlank(message = "Telefone é obrigatório") // Validação
    @Pattern( // Validação: Garante que o texto siga uma Expressão Regular (Regex)
        regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-?\\d{4}|\\d{10,11})$", // Regex: Aceita formatos com ou sem parênteses, hífen e com 10 ou 11 dígitos
        message = "Telefone inválido. Ex.: 11999999999 ou (11) 99999-9999"
    )
    private String telefone;

    @Schema(description = "Endereço completo do cliente", example = "Rua das Flores, 123 - Centro", required = true) // Documentação Swagger
    @NotBlank(message = "Endereço é obrigatório") // Validação
    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres") // Validação: Tamanho máximo
    private String endereco;

    @Schema(description = "CPF do cliente (pode conter pontuação ou apenas números)", example = "39053344705", required = true) // Documentação Swagger
    @NotBlank(message = "CPF é obrigatório") // Validação
    @CPF(message = "CPF inválido") // Validação: Usa a lógica de validação de CPF (dígitos verificadores)
    private String cpf;

    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
}