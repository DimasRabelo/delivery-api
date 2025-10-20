package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// ==================================================
// DTO de resposta para enviar dados do cliente
// Usado quando retornamos informações do cliente na API
// ==================================================
@Schema(description = "DTO de resposta com dados do cliente")
public class ClienteResponseDTO {

    // --------------------------------------------------
    // ID do cliente
    // @Schema → documenta o campo no Swagger, com exemplo
    // --------------------------------------------------
    @Schema(description = "ID do cliente", example = "1")
    private Long id;

    // --------------------------------------------------
    // Nome do cliente
    // --------------------------------------------------
    @Schema(description = "Nome do cliente", example = "João Silva")
    private String nome;

    // --------------------------------------------------
    // Email do cliente
    // --------------------------------------------------
    @Schema(description = "Email do cliente", example = "joao@email.com")
    private String email;

    // --------------------------------------------------
    // Telefone do cliente
    // --------------------------------------------------
    @Schema(description = "Telefone do cliente", example = "11999999999")
    private String telefone;

    // --------------------------------------------------
    // Endereço do cliente
    // --------------------------------------------------
    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123")
    private String endereco;

    // --------------------------------------------------
    // Indica se o cliente está ativo ou não
    // --------------------------------------------------
    @Schema(description = "Indica se o cliente está ativo", example = "true")
    private boolean ativo;

    // =======================
    // GETTERS E SETTERS
    // =======================
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
