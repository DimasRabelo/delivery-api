package com.deliverytech.delivery.dto.auth;

public class LoginResponse {


private String token;
private String tipo = "Bearer"; private Long expiracao; private UserResponse usuario;

// Construtores
public LoginResponse() {}


public LoginResponse(String token, Long expiracao, UserResponse usuario) { this.token = token;
this.expiracao = expiracao; this.usuario = usuario;
}


// Getters e Setters
public String getToken() { return token; }
public void setToken(String token) { this.token = token; }


public String getTipo() { return tipo; }
public void setTipo(String tipo) { this.tipo = tipo; }


public Long getExpiracao() { return expiracao; }
public void setExpiracao(Long expiracao) { this.expiracao = expiracao; }


public UserResponse getUsuario() { return usuario; }
public void setUsuario(UserResponse usuario) { this.usuario = usuario; }
}
