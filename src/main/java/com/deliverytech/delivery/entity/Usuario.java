package com.deliverytech.delivery.entity;


import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.deliverytech.delivery.enums.Role;

import jakarta.persistence.*;
import java.time.LocalDateTime; 
import java.util.Collection; 
import java.util.Collections;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

@Column(nullable = false, unique = true) private String email;

@Column(nullable = false) private String senha;

@Column(nullable = false) private String nome;

@Enumerated(EnumType.STRING) @Column(nullable = false)
private Role role;


@Column(nullable = false) private Boolean ativo = true;

@Column(name = "data_criacao", nullable = false)
private LocalDateTime dataCriacao = LocalDateTime.now();


@Column(name = "restaurante_id") private Long restauranteId;

  // Construtores
public Usuario() {}

public Usuario(String email, String senha, String nome, Role role) {
this.email = email; this.senha = senha; this.nome = nome; this.role = role; this.ativo = true;
this.dataCriacao = LocalDateTime.now();
}


// Implementação UserDetails @Override
public Collection<? extends GrantedAuthority> getAuthorities() {
return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
}


@Override
public String getPassword() { return senha;
}


@Override
public String getUsername() { return email;
}


@Override
public boolean isAccountNonExpired() { return true;
}


@Override
public boolean isAccountNonLocked() {
return true;
}


@Override
public boolean isCredentialsNonExpired() { return true;
}


@Override
public boolean isEnabled() { return ativo;
}


// Getters e Setters
public Long getId() { return id; }
public void setId(Long id) { this.id = id; }


public String getEmail() { return email; }
public void setEmail(String email) { this.email = email; }


public String getSenha() { return senha; }
public void setSenha(String senha) { this.senha = senha; }


public String getNome() { return nome; }
public void setNome(String nome) { this.nome = nome; }


public Role getRole() { return role; }
public void setRole(Role role) { this.role = role; }


public Boolean getAtivo() { return ativo; }
public void setAtivo(Boolean ativo) { this.ativo = ativo; }
public LocalDateTime getDataCriacao() { return dataCriacao; }
public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }


public Long getRestauranteId() { return restauranteId; }
public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
}


