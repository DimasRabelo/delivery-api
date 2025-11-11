package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Entidade responsável pela autenticação e autorização do sistema.
 * Implementa UserDetails para integração com o Spring Security.
 */
@Entity
@Table(name = "usuario")
@Schema(description = "Entidade de autenticação e autorização (usuário do sistema)")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do usuário", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 100)
    @Schema(description = "Email do usuário (usado como login)", example = "usuario@email.com")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha criptografada do usuário", readOnly = true)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Role é obrigatória")
    @Schema(description = "Nível de permissão do usuário", example = "CLIENTE")
    private Role role;

    @Column(nullable = false)
    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Schema(description = "Data e hora de criação do usuário", example = "2024-06-05T10:30:00")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "restaurante_id")
    @Schema(description = "ID do restaurante vinculado (caso role seja RESTAURANTE)", example = "5")
    private Long restauranteId;

    /** Dados do cliente (usado apenas se a role for CLIENTE) */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Schema(description = "Perfil de cliente associado a este usuário")
    private Cliente cliente;

    /** Endereços de entrega cadastrados pelo usuário */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Schema(description = "Lista de endereços do usuário")
    private List<Endereco> enderecos = new ArrayList<>();

    public Usuario() {}

    public Usuario(String email, String senha, Role role) {
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
    }

    // --- Métodos exigidos pelo Spring Security ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return ativo; }

    // --- Getters e Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<Endereco> getEnderecos() { return enderecos; }
    public void setEnderecos(List<Endereco> enderecos) { this.enderecos = enderecos; }
}
