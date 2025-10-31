package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.persistence.*; // Importações para JPA (Persistência de Dados)
import jakarta.validation.constraints.Email; // Importações para Validação de dados
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Entity // Marca esta classe como uma entidade gerenciada pela JPA
@Table(name = "usuario") // Define o nome da tabela
@Schema(description = "Entidade que representa um Usuário no sistema, usada para autenticação e autorização") // Documentação Swagger
public class Usuario implements UserDetails {

    /**
     * Identificador único (Chave Primária).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do usuário", example = "1") // Documentação Swagger
    private Long id;

    /**
     * Email do usuário. Usado como 'username' para o login no Spring Security.
     * Deve ser único no sistema.
     */
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email é obrigatório") // Validação: Não pode ser nulo ou vazio
    @Email(message = "Email deve ter um formato válido") // Validação: Formato de email
    @Size(max = 100) // Validação: Tamanho máximo
    @Schema(description = "Email do usuário (usado como 'username' para login)", example = "usuario@email.com", required = true) // Documentação Swagger
    private String email;

   /**
 * Senha criptografada (hash) do usuário.
 * Usada como 'password' para o login no Spring Security.
 */
@Column(nullable = false)
@NotBlank(message = "Senha é obrigatória")
// A anotação @Schema é desnecessária aqui, pois a senha NUNCA deve ser exposta.
// Mas se quiser mantê-la, deve ser simples:
@Schema(description = "Senha criptografada do usuário (hash)", readOnly = true)
private String senha;

    /**
     * Nome completo do usuário.
     */
    @Column(nullable = false)
    @NotBlank(message = "Nome é obrigatório") // Validação
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") // Validação: Tamanho
    @Schema(description = "Nome completo do usuário", example = "Nome Sobrenome", required = true) // Documentação Swagger
    private String nome;

    /**
     * Define o nível de permissão do usuário (ex: CLIENTE, RESTAURANTE, ADMIN).
     * Mapeado como String no banco de dados.
     */
    @Enumerated(EnumType.STRING) // JPA: Salva o Enum como String ("CLIENTE") ao invés de número (0)
    @Column(nullable = false)
    @NotNull(message = "Role é obrigatória") // Validação: @NotNull para Enums/Objetos
    @Schema(description = "Nível de permissão do usuário", example = "CLIENTE", required = true) // Documentação Swagger
    private Role role;

    /**
     * Controle de status do usuário.
     * Se 'false', o usuário não pode logar (mapeado para {@link #isEnabled()}).
     */
    @Column(nullable = false)
    @Schema(description = "Indica se o usuário está ativo e pode logar", example = "true", defaultValue = "true") // Documentação Swagger
    private Boolean ativo = true; // Valor padrão

    /**
     * Data e hora em que o registro do usuário foi criado.
     * Preenchido automaticamente na criação.
     */
    @Column(name = "data_criacao", nullable = false)
    @Schema(description = "Data e hora de criação do registro", example = "2024-06-05T10:30:00", readOnly = true) // Documentação Swagger: 'readOnly' indica que não deve ser enviado em requests
    private LocalDateTime dataCriacao = LocalDateTime.now(); // Valor padrão

    /**
     * ID do restaurante ao qual este usuário está associado (se aplicável).
     * Usado principalmente para usuários com {@link Role#RESTAURANTE}.
     */
    @Column(name = "restaurante_id")
    @Schema(description = "ID do restaurante associado (se a role for 'RESTAURANTE')", example = "5", nullable = true) // Documentação Swagger: 'nullable' indica que é opcional
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
    // (Estes métodos não são parte do Schema da API, são para lógica interna do Security)
    // -------------------------------------------------------------------------

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

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

    // 'getEmail()' é sobrescrito por 'getUsername()', mas é bom ter o getter padrão
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // 'getSenha()' é sobrescrito por 'getPassword()', mas é bom ter o getter padrão
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