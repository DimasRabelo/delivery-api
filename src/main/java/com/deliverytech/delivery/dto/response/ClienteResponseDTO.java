package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime; // <-- ADICIONAR IMPORTAÇÃO

// ==================================================
// DTO de resposta para enviar dados do cliente
// Usado quando retornamos informações do cliente na API
// ==================================================
@Schema(description = "DTO de resposta com dados do cliente")
public class ClienteResponseDTO {

    // ... (campos id, nome, email, telefone, endereco, ativo - permanecem iguais) ...

    @Schema(description = "ID do cliente", example = "1")
    private Long id;

    @Schema(description = "Nome do cliente", example = "João Silva")
    private String nome;

    @Schema(description = "Email do cliente", example = "joao@email.com")
    private String email;

    @Schema(description = "Telefone do cliente", example = "11999999999")
    private String telefone;

    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123")
    private String endereco;

    @Schema(description = "Indica se o cliente está ativo", example = "true")
    private boolean ativo;

    // ==============================================
    // ADICIONAR ESTES CAMPOS
    // ==============================================
    @Schema(description = "CPF do cliente (apenas números)", example = "12345678901")
    private String cpf;

    @Schema(description = "Data e hora do cadastro do cliente")
    private LocalDateTime dataCadastro;
    // ==============================================

    
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

    // ==============================================
    // GETTERS E SETTERS PARA OS NOVOS CAMPOS
    // ==============================================
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}