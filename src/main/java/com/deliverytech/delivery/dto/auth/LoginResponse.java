package com.deliverytech.delivery.dto.auth;

import java.util.Date;

/**
 * DTO (Data Transfer Object) que representa a resposta bem-sucedida do login.
 *
 * Esta classe é serializada para JSON e retornada ao cliente
 * após uma autenticação bem-sucedida no endpoint (ex: POST /api/auth/login).
 *
 * Contém o token de acesso, informações sobre sua expiração e
 * os dados básicos do usuário autenticado.
 */
public class LoginResponse {

    /**
     * O token de acesso JWT (JSON Web Token) gerado.
     * Deve ser enviado pelo cliente em requisições futuras no header "Authorization".
     */
    private String token;

    /**
     * O tipo do token. Padrão "Bearer", indicando o esquema de autenticação.
     */
    private String tipo = "Bearer";

    /**
     * A data e hora exata em que o token irá expirar (timestamp).
     * Formatado como UTC (ISO 8601) no JSON final.
     */
    private Date expiracao;

    /**
     * Objeto contendo os detalhes do usuário que foi autenticado.
     */
    private UserResponse usuario;

    /**
     * A duração total do token em segundos (ex: 60 para 1 minuto).
     * Campo útil para o cliente saber por quanto tempo o token é válido,
     * sem precisar calcular a partir da data de expiração.
     */
    private long expiracaoSegundos;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /**
     * Construtor padrão.
     * Necessário para a serialização (JSON para Objeto) pelo Jackson.
     */
    public LoginResponse() {
    }

    /**
     * Construtor completo para criar a resposta de login.
     *
     * @param token             O token JWT gerado.
     * @param expiracao         A data exata de expiração do token.
     * @param usuario           O DTO com os dados do usuário.
     * @param expiracaoSegundos A duração do token em segundos.
     */
    public LoginResponse(String token, Date expiracao, UserResponse usuario, long expiracaoSegundos) {
        this.token = token;
        this.expiracao = expiracao;
        this.usuario = usuario;
        this.expiracaoSegundos = expiracaoSegundos;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Date getExpiracao() {
        return expiracao;
    }

    public void setExpiracao(Date expiracao) {
        this.expiracao = expiracao;
    }

    public UserResponse getUsuario() {
        return usuario;
    }

    public void setUsuario(UserResponse usuario) {
        this.usuario = usuario;
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }

    public void setExpiracaoSegundos(long expiracaoSegundos) {
        this.expiracaoSegundos = expiracaoSegundos;
    }
}