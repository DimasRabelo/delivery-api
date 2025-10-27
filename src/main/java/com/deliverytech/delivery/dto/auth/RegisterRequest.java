package com.deliverytech.delivery.dto.auth;

import com.deliverytech.delivery.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) que representa o corpo (body) da requisição de registro.
 *
 * Esta classe é usada pelo Spring (via Jackson) para desserializar o JSON enviado
 * pelo cliente no endpoint de criação de novos usuários (ex: POST /api/auth/register).
 *
 * Contém as validações (Bean Validation) para garantir que os dados de
 * registro sejam coesos e corretos antes de serem processados pelo serviço.
 */
public class RegisterRequest {

    /**
     * Nome do usuário.
     * Não pode ser nulo/vazio e deve ter entre 2 e 100 caracteres.
     */
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    /**
     * Email do usuário. Será usado para login.
     * Não pode ser nulo/vazio e deve ter um formato de email válido.
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    /**
     * Senha (plana) do usuário.
     * Não pode ser nula/vazia e deve ter no mínimo 6 caracteres.
     * O {@link com.deliverytech.delivery.service.auth.AuthService} será 
     * responsável por criptografar (hash) esta senha antes de salvar.
     */
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    /**
     * O nível de permissão (role) a ser atribuído ao novo usuário.
     * Não pode ser nulo (ex: "CLIENTE", "RESTAURANTE").
     */
    @NotNull(message = "Role é obrigatória")
    private Role role;

    /**
     * O ID do restaurante ao qual este usuário será associado.
     * Este campo é opcional e usado apenas se a {@link #role} for RESTAURANTE.
     */
    private Long restauranteId;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /**
     * Construtor padrão.
     * Necessário para a desserialização do JSON pelo Jackson.
     */
    public RegisterRequest() {
    }

    /**
     * Construtor completo (sem restauranteId).
     * Útil para a criação de instâncias, especialmente em testes.
     *
     * @param nome  Nome do usuário.
     * @param email Email do usuário.
     * @param senha Senha do usuário.
     * @param role  Role do usuário.
     */
    public RegisterRequest(String nome, String email, String senha, Role role) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }
}