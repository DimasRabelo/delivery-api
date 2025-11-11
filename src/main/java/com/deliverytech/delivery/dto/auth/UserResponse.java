package com.deliverytech.delivery.dto.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para representar os dados de um Usuário
 * de forma segura em respostas de API.
 */
@Schema(description = "Dados de resposta segura do usuário")
public class UserResponse {

    @Schema(description = "ID único do usuário", example = "1")
    private Long id;

    @Schema(description = "Nome completo do usuário (buscado do perfil Cliente, se existir)", example = "João Silva")
    private String nome;

    @Schema(description = "Email (login) do usuário", example = "joao.silva@email.com")
    private String email;

    @Schema(description = "Nível de permissão", example = "CLIENTE")
    private Role role;

    @Schema(description = "Conta está ativa?", example = "true")
    private Boolean ativo;

    @Schema(description = "Data de criação da conta")
    private LocalDateTime dataCriacao;

    @Schema(description = "ID do restaurante associado (se role=RESTAURANTE)", example = "5")
    private Long restauranteId;

    // -------------------------------------------------------------------------
    // Construtores
    // -------------------------------------------------------------------------

    public UserResponse() {
    }

    /**
     * Construtor de "Mapeamento" (Mapping Constructor)
     *
     * @param usuario A entidade 'Usuario' vinda do banco de dados.
     */
    public UserResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.role = usuario.getRole();
        this.ativo = usuario.getAtivo();
        this.dataCriacao = usuario.getDataCriacao();
        this.restauranteId = usuario.getRestauranteId();

        // O 'nome' agora é buscado do perfil 'Cliente' associado
        Cliente cliente = usuario.getCliente();
        if (cliente != null) {
            this.nome = cliente.getNome();
        } else {
            // Se for um Admin ou Restaurante (sem perfil Cliente), o nome será nulo
            this.nome = null; 
        }
    }

    // -------------------------------------------------------------------------
    // Getters e Setters
    // -------------------------------------------------------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
}