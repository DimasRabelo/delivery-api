package com.deliverytech.delivery.dto.auth;

import java.util.Date;

/**
 * DTO (Data Transfer Object) que representa a resposta bem-sucedida do login.
 */
public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private Date expiracao;
    private UserResponse usuario;
    private long expiracaoSegundos;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    public LoginResponse() {
    }

    /**
     * Construtor simplificado (Usado pelo AuthService atualmente).
     * @param token O token JWT
     * @param usuario Os dados do usuário
     */
    public LoginResponse(String token, UserResponse usuario) {
        this.token = token;
        this.usuario = usuario;
        // Se quiser, pode definir valores padrão para expiração aqui ou deixar null
    }

    /**
     * Construtor completo.
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