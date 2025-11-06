package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.ToString; // Import necessário
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList; // Import necessário
import java.util.Collection;
import java.util.Collections;
import java.util.List; // Import necessário

/**
 * Entidade de autenticação e autorização (Spring Security).
 * Armazena credenciais (email/senha) e permissões (role).
 */
@Entity
@Table(name = "usuario")
@Schema(description = "Entidade de autenticação (login, senha, permissão)")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do usuário", example = "1")
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 100)
    @Schema(description = "Email do usuário (usado como 'username' para login)", example = "usuario@email.com", required = true)
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha criptografada do usuário (hash)", readOnly = true)
    private String senha;

    // --- CAMPO 'nome' REMOVIDO DAQUI ---
    // O nome agora pertence às entidades de perfil (Cliente, Entregador, etc.)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Role é obrigatória")
    @Schema(description = "Nível de permissão do usuário", example = "CLIENTE", required = true)
    private Role role;

    @Column(nullable = false)
    @Schema(description = "Indica se o usuário está ativo e pode logar", example = "true", defaultValue = "true")
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    @Schema(description = "Data e hora de criação do registro", example = "2024-06-05T10:30:00", readOnly = true)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "restaurante_id")
    @Schema(description = "ID do restaurante associado (se a role for 'RESTAURANTE')", example = "5", nullable = true)
    private Long restauranteId;

    // --- RELACIONAMENTO COM O PERFIL DO CLIENTE ---
    /**
     * Link para os dados cadastrais (nome, cpf) do cliente.
     * Será nulo se a 'role' não for CLIENTE.
     */
    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Schema(description = "Dados cadastrais do perfil do cliente (se a role for CLIENTE)")
    private Cliente cliente;

    // --- RELACIONAMENTO COM ENDEREÇOS (GARGALO 1) ---
    /**
     * Lista de endereços cadastrados por este usuário.
     */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Schema(description = "Lista de endereços de entrega cadastrados pelo usuário")
    private List<Endereco> enderecos = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    public Usuario() {
    }

    /**
     * Construtor para novos usuários (sem nome).
     */
    public Usuario(String email, String senha, Role role) {
        this.email = email;
        this.senha = senha;
        this.role = role;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Métodos UserDetails (Spring Security)
    // -------------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    // ... (getPassword, getUsername, isAccountNonExpired, etc. - seu código original)

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------
    // (Incluindo os novos para cliente e enderecos)

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
    
    // Métodos UserDetails (sem getters/setters)
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return ativo; }
}