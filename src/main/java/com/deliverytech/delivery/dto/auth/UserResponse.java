package com.deliverytech.delivery.dto.auth;

import com.deliverytech.delivery.entity.Usuario;
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

    @Schema(description = "Nome completo", example = "João Silva")
    private String nome;

    @Schema(description = "Email (login) do usuário", example = "joao.silva@email.com")
    private String email;

    // MUDANÇA IMPORTANTE: String para evitar conflito de tipos no Service
    @Schema(description = "Nível de permissão", example = "CLIENTE")
    private String role; 

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
     * Ajustado para buscar dados da nova estrutura da entidade Usuario.
     *
     * @param usuario A entidade 'Usuario' vinda do banco de dados.
     */
    public UserResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.ativo = usuario.getAtivo();
        this.dataCriacao = usuario.getDataCriacao();
        
        // Converte o Enum Role para String
        if (usuario.getRole() != null) {
            this.role = usuario.getRole().name();
        }

        // Lógica inteligente para pegar o NOME
        if (usuario.getNome() != null) {
            // 1. Tenta pegar direto do Usuário (se foi preenchido no update)
            this.nome = usuario.getNome();
        } else if (usuario.getCliente() != null) {
            // 2. Se não, tenta pegar do perfil Cliente
            this.nome = usuario.getCliente().getNome();
        } else if (usuario.getRestaurante() != null) {
            // 3. Se não, tenta pegar do perfil Restaurante
            this.nome = usuario.getRestaurante().getNome();
        }

        // Lógica para pegar o ID do Restaurante (Correção do erro getRestaurante)
        if (usuario.getRestaurante() != null) {
            this.restauranteId = usuario.getRestaurante().getId();
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

    // Getter e Setter agora lidam com String
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }
}