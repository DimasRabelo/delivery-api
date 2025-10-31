package com.deliverytech.delivery.dto.response;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import java.time.LocalDateTime; // Importação para o campo 'dataCadastro'

/**
 * DTO (Data Transfer Object) de resposta para enviar dados do Cliente.
 * Define a estrutura de dados que a API retorna ao consultar um cliente,
 * omitindo informações sensíveis (como senha, se houvesse) e
 * formatando dados para o front-end.
 */
@Schema(description = "DTO de resposta com dados do cliente") // Documentação a nível de classe
public class ClienteResponseDTO {

    @Schema(description = "ID do cliente", example = "1") // Documentação Swagger
    private Long id;

    @Schema(description = "Nome do cliente", example = "João Silva") // Documentação Swagger
    private String nome;

    @Schema(description = "Email do cliente", example = "joao@email.com") // Documentação Swagger
    private String email;

    @Schema(description = "Telefone do cliente", example = "11999999999") // Documentação Swagger
    private String telefone;

    @Schema(description = "Endereço do cliente", example = "Rua das Flores, 123") // Documentação Swagger
    private String endereco;

    @Schema(description = "Indica se o cliente está ativo", example = "true") // Documentação Swagger
    private boolean ativo;

    // --- CAMPOS ADICIONADOS ---

    @Schema(description = "CPF do cliente (apenas números)", example = "12345678901") // Documentação Swagger
    private String cpf;

    @Schema(description = "Data e hora do cadastro do cliente", example = "2024-10-30T10:00:00") // Documentação Swagger
    private LocalDateTime dataCadastro;
    
    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    
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

    public boolean isAtivo() { return ativo; } // 'isAtivo' é o padrão para boolean
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    // --- Getters e Setters dos novos campos ---
    
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}