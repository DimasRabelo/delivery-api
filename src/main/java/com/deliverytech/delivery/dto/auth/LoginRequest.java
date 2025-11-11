package com.deliverytech.delivery.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO (Data Transfer Object) que representa o corpo (body) da requisição de login.
 *
 * Esta classe é usada pelo Spring (via Jackson) para desserializar o JSON enviado
 * pelo cliente no endpoint de autenticação (ex: POST /api/auth/login).
 *
 * Inclui validações (Bean Validation) que são verificadas automaticamente
 * pelo Spring quando a anotação {@link jakarta.validation.Valid} é usada
 * no parâmetro do método no Controller.
 */
public class LoginRequest {

    /**
     * O email do usuário que está tentando autenticar.
     * Não pode ser nulo/vazio e deve ser um email bem formatado.
     */
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    /**
     * A senha (plana, sem criptografia) fornecida pelo usuário para autenticação.
     * Não pode ser nula/vazia.
     */
    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /**
     * Construtor padrão.
     * Necessário para a desserialização do JSON pelo Jackson.
     */
    public LoginRequest() {
    }

    /**
     * Construtor completo para facilitar a criação de instâncias
     * (útil principalmente para testes).
     *
     * @param email O email do usuário.
     * @param senha A senha do usuário.
     */
    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

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
}