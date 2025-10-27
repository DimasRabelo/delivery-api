package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Entidade que representa um Usuário no banco de dados.
 *
 * Esta classe possui uma dupla responsabilidade:
 * 1. Mapeamento JPA para a tabela "usuario".
 * 2. Implementação da interface {@link UserDetails} do Spring Security,
 * permitindo que esta entidade seja usada diretamente para autenticação e autorização.
 */
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    /**
     * Identificador único (Chave Primária).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Email do usuário. Usado como 'username' para o login no Spring Security.
     * Deve ser único no sistema.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Senha criptografada (hash) do usuário.
     * Usada como 'password' para o login no Spring Security.
     */
    @Column(nullable = false)
    private String senha;

    /**
     * Nome completo do usuário.
     */
    @Column(nullable = false)
    private String nome;

    /**
     * Define o nível de permissão do usuário (ex: CLIENTE, RESTAURANTE, ADMIN).
     * Mapeado como String no banco de dados.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Controle de status do usuário.
     * Se 'false', o usuário não pode logar (mapeado para {@link #isEnabled()}).
     */
    @Column(nullable = false)
    private Boolean ativo = true;

    /**
     * Data e hora em que o registro do usuário foi criado.
     * Preenchido automaticamente na criação.
     */
    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    /**
     * ID do restaurante ao qual este usuário está associado (se aplicável).
     * Usado principalmente para usuários com {@link Role#RESTAURANTE}.
     */
    @Column(name = "restaurante_id")
    private Long restauranteId;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public Usuario() {
    }

    /**
     * Construtor para facilitar a criação de novos usuários.
     *
     * @param email Email de login.
     * @param senha Senha (deve ser criptografada antes de persistir).
     * @param nome  Nome do usuário.
     * @param role  Nível de permissão.
     */
    public Usuario(String email, String senha, String nome, Role role) {
        this.email = email;
        this.senha = senha;
        this.nome = nome;
        this.role = role;
        this.ativo = true;
        this.dataCriacao = LocalDateTime.now();
    }

    // -------------------------------------------------------------------------
    // Implementação dos métodos UserDetails (Spring Security)
    // -------------------------------------------------------------------------

    /**
     * Retorna as permissões (roles) concedidas ao usuário.
     * O Spring Security exige que as roles tenham o prefixo "ROLE_".
     *
     * @return Uma coleção contendo a role do usuário (ex: "ROLE_CLIENTE").
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Retorna a senha (hash) usada para autenticar o usuário.
     *
     * @return A senha criptografada.
     */
    @Override
    public String getPassword() {
        return senha;
    }

    /**
     * Retorna o nome de usuário (username) usado para autenticar.
     * No nosso sistema, usamos o email como username.
     *
     * @return O email do usuário.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indica se a conta do usuário expirou.
     * (Não implementado, sempre retorna 'true').
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica se o usuário está bloqueado ou desbloqueado.
     * (Não implementado, sempre retorna 'true'. O controle é feito por {@link #ativo}).
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indica se as credenciais do usuário (senha) expiraram.
     * (Não implementado, sempre retorna 'true').
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica se o usuário está habilitado ou desabilitado.
     * Mapeado diretamente para o nosso campo {@link #ativo}.
     *
     * @return 'true' se o usuário estiver ativo, 'false' caso contrário.
     */
    @Override
    public boolean isEnabled() {
        return ativo;
    }

    // -------------------------------------------------------------------------
    // Getters e Setters Padrão
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Long getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Long restauranteId) {
        this.restauranteId = restauranteId;
    }
}