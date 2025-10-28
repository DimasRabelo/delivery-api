package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para cadastro ou atualização de cliente")
public class ClienteDTO {

    // ... (campos nome, email, telefone, endereco - permanecem iguais) ...

    @Schema(description = "Nome do cliente", example = "João Silva", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "Email do cliente", example = "joao.silva@email.com", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @Schema(description = "Telefone do cliente", example = "11999999999", required = true)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(
        regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-?\\d{4}|\\d{10,11})$",
        message = "Telefone inválido. Ex.: 11999999999 ou (11) 99999-9999"
    )
    private String telefone;

    @Schema(description = "Endereço completo do cliente", example = "Rua das Flores, 123 - Centro", required = true)
    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    private String endereco;

    // ==============================================
    // ADICIONAR ESTE CAMPO
    // ==============================================
    @Schema(description = "CPF do cliente (apenas números)", example = "12345678901", required = true)
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(
        regexp = "^\\d{11}$", // Valida que são exatamente 11 dígitos numéricos
        message = "CPF deve conter exatamente 11 dígitos numéricos"
    )
    private String cpf;
    // ==============================================


    // =======================
    // GETTERS E SETTERS
    // =======================
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    // ==============================================
    // GETTER E SETTER PARA CPF
    // ==============================================
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
}