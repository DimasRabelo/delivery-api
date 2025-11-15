package com.deliverytech.delivery.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

/**
 * DTO (Data Transfer Object) para a criação ou atualização
 * dos dados de PERFIL de um Cliente. (Inclui campos essenciais de Usuario para validação)
 */
@Schema(description = "Dados para ATUALIZAÇÃO do perfil de um cliente")
public class ClienteDTO {

    // --- CAMPOS DE CLIENTE ---
    @Schema(description = "Nome do cliente", example = "João Silva", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    private String nome;

    @Schema(description = "Telefone do cliente", example = "11999999999", required = true)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-?\\d{4}|\\d{10,11})$", message = "Telefone inválido")
    private String telefone;

    @Schema(description = "CPF do cliente", example = "39053344705", required = true)
    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;
    
    // --- CAMPOS DE USUÁRIO (NECESSÁRIOS PARA O PUT PASSAR NA VALIDAÇÃO) ---
    
    @Schema(description = "Email do usuário (obrigatório)", example = "joao.teste@email.com")
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Schema(description = "Senha do usuário (obrigatório na validação do PUT)", example = "123456")
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String senha;


    // --- GETTERS & SETTERS (Corrigidos e Completos) ---
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    
    public String getEmail() { return email; } 
    public void setEmail(String email) { this.email = email; } 
    public String getSenha() { return senha; } 
    public void setSenha(String senha) { this.senha = senha; } 
}