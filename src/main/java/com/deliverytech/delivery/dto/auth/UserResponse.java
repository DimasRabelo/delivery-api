package com.deliverytech.delivery.dto.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para representar os dados de um Usuário
 * de forma segura em respostas de API.
 *
 * Esta classe é uma "visão" pública da entidade {@link Usuario},
 * omitindo intencionalmente campos sensíveis como a senha (`senha`).
 *
 * É usado como parte da {@link LoginResponse} e em endpoints
 * que retornam informações do usuário (ex: GET /api/auth/me).
 */
public class UserResponse {

    /**
     * O ID único do usuário.
     */
    private Long id;

    /**
     * O nome completo do usuário.
     */
    private String nome;

    /**
     * O email (username) do usuário.
     */
    private String email;

    /**
     * O nível de permissão (role) do usuário (ex: CLIENTE, RESTAURANTE).
     */
    private Role role;

    /**
     * Indica se a conta do usuário está ativa ('true') ou desativada ('false').
     */
    private Boolean ativo;

    /**
     * A data e hora em que a conta do usuário foi criada.
     */
    private LocalDateTime dataCriacao;

    /**
     * O ID do restaurante associado a este usuário (se aplicável).
     * Será 'null' para usuários que não são do tipo RESTAURANTE.
     */
    private Long restauranteId;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    /**
     * Construtor padrão.
     * Necessário para a desserialização/serialização do JSON pelo Jackson.
     */
    public UserResponse() {
    }

    /**
     * Construtor de "Mapeamento" (Mapping Constructor).
     *
     * Cria uma instância de {@link UserResponse} a partir de uma entidade
     * {@link Usuario}. Este é o local onde os dados são filtrados,
     * copiando apenas os campos seguros para o DTO.
     *
     * @param usuario A entidade {@link Usuario} vinda do banco de dados.
     */
    public UserResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
        this.ativo = usuario.getAtivo();
        this.dataCriacao = usuario.getDataCriacao();
        this.restauranteId = usuario.getRestauranteId();
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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