package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

@Schema(description = "Dados para ATUALIZAÇÃO do perfil de um cliente")
public class ClienteDTO {

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

    // --- CAMPOS REMOVIDOS ---
    // 'email' agora é atualizado pelo UsuarioService
    // 'endereco' (String) agora é gerenciado pelo EnderecoService
    
    // --- Getters e Setters ---
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
}