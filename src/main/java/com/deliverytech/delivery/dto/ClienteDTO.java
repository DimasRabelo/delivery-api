package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

// ==================================================
// DTO usado para cadastro ou atualização de clientes
// Serve para receber dados da requisição HTTP e
// validar antes de processar no service.
// ==================================================
@Schema(description = "Dados para cadastro ou atualização de cliente")
public class ClienteDTO {

    // --------------------------------------------------
    // Nome do cliente
    // @Schema → documenta o campo no Swagger
    // @NotBlank → valida que não pode ser vazio
    // @Size → valida tamanho mínimo e máximo
    // --------------------------------------------------
    @Schema(description = "Nome do cliente", example = "João Silva", required = true)
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    // --------------------------------------------------
    // Email do cliente
    // @Email → valida formato correto de email
    // --------------------------------------------------
    @Schema(description = "Email do cliente", example = "joao.silva@email.com", required = true)
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    // --------------------------------------------------
    // Telefone do cliente
    // @Pattern → valida formato correto de número
    // Suporta 11999999999 ou (11) 99999-9999
    // --------------------------------------------------
    @Schema(description = "Telefone do cliente", example = "11999999999", required = true)
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(
        regexp = "^(\\(\\d{2}\\)\\s?\\d{4,5}-?\\d{4}|\\d{10,11})$",
        message = "Telefone inválido. Ex.: 11999999999 ou (11) 99999-9999"
    )
    private String telefone;

    // --------------------------------------------------
    // Endereço completo do cliente
    // @Size → valida comprimento máximo do texto
    // --------------------------------------------------
    @Schema(description = "Endereço completo do cliente", example = "Rua das Flores, 123 - Centro", required = true)
    @NotBlank(message = "Endereço é obrigatório")
    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    private String endereco;

    // =======================
    // GETTERS E SETTERS
    // =======================
    
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
}
