package com.deliverytech.delivery.dto.auth;

// --- IMPORTS ADICIONADOS ---
import com.deliverytech.delivery.dto.EnderecoDTO; 
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.br.CPF;
import jakarta.validation.constraints.Pattern;
// --- FIM DOS IMPORTS ---

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.NotNull; // (Não precisamos mais de NotNull para 'role')
import jakarta.validation.constraints.Size;
// import com.deliverytech.delivery.enums.Role; // (Não precisamos mais de 'Role' no DTO)

/**
 * DTO (Data Transfer Object) para o registro de um novo CLIENTE.
 * (Refatorado para incluir dados de Perfil e Endereço)
 */
@Schema(description = "DTO completo para registro de um novo Cliente")
public class RegisterRequest {

    // --- Dados do Perfil (vão para a entidade Cliente) ---
    @Schema(description = "Nome completo do cliente", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @Schema(description = "CPF do cliente", required = true)
    @NotBlank(message = "CPF é obrigatório")
    @CPF(message = "CPF inválido") // Valida o formato e os dígitos do CPF
    private String cpf;

    @Schema(description = "Telefone do cliente", example = "11999999999", required = true)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-?\\d{4}|\\d{10,11})$", message = "Telefone inválido")
    private String telefone;

    // --- Dados de Autenticação (vão para a entidade Usuario) ---
    @Schema(description = "Email (será o login)", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @Schema(description = "Senha (mínimo 6 caracteres)", required = true)
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    // --- Dados de Endereço (vão para a entidade Endereco - Gargalo 1) ---
    @Schema(description = "Endereço principal (obrigatório no cadastro)", required = true)
    @NotNull(message = "Endereço principal é obrigatório")
    @Valid // Instrui o Spring a validar os campos dentro do EnderecoDTO
    private EnderecoDTO endereco;

    // --- CAMPOS ANTIGOS REMOVIDOS ---
    // private Role role; (Será 'CLIENTE' por padrão no serviço)
    // private Long restauranteId; (Não se aplica ao cadastro de cliente)

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------
    
    public RegisterRequest() {
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public EnderecoDTO getEndereco() { return endereco; }
    public void setEndereco(EnderecoDTO endereco) { this.endereco = endereco; }
}