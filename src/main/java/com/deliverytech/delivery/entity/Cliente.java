package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.persistence.*; // Importações para JPA (Persistência de Dados)
import jakarta.validation.constraints.Email; // Importações para Validação de dados
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode; // Imports do Lombok
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a entidade Cliente no banco de dados.
 * Armazena informações pessoais e de contato do usuário que realiza os pedidos.
 */
@Entity // Marca esta classe como uma entidade gerenciada pela JPA
@Getter // Lombok: Gera getters para todos os campos
@Setter // Lombok: Gera setters para todos os campos
@ToString // Lombok: Gera o método toString()
@EqualsAndHashCode(of = "id") // Lombok: Gera equals() e hashCode() baseados APENAS no 'id'
@Schema(description = "Entidade que representa um cliente do serviço de delivery")
public class Cliente {

    @Id // Define este campo como a chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração automática do ID (auto-incremento)
    @Schema(description = "Identificador único do cliente", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório") // Validação: Não pode ser nulo ou vazio
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") // Validação: Tamanho
    @Schema(description = "Nome completo do cliente", example = "João da Silva", required = true)
    private String nome;

    @Column(unique = true, nullable = false) // JPA: Garante que o email seja único e não nulo no banco
    @NotBlank(message = "Email é obrigatório") // Validação
    @Email(message = "Email deve ter um formato válido") // Validação: Verifica o formato de email
    @Size(max = 100) // Validação: Tamanho máximo
    @Schema(description = "Email do cliente (usado para login e comunicação)", example = "joao.silva@email.com", required = true)
    private String email;

    @Size(min = 10, max = 15, message = "Telefone deve ter entre 10 e 15 caracteres") // Validação: Permite formatos (11)91234-5678
    @Schema(description = "Telefone de contato do cliente (com DDD)", example = "11912345678")
    private String telefone;

    @Size(max = 255) // Validação: Tamanho máximo
    @Schema(description = "Endereço principal do cliente", example = "Rua das Flores, 123, Apto 45, Bairro, Cidade - UF")
    private String endereco;

    @Column(nullable = false) // JPA: Garante que não seja nulo no banco
    @Schema(description = "Indica se o cliente está ativo no sistema", example = "true", defaultValue = "true")
    private boolean ativo = true; // Valor padrão para novos clientes

    @Column(name = "data_cadastro", nullable = false, updatable = false) // JPA: Não nulo e não pode ser atualizado após a criação
    @Schema(description = "Data e hora do cadastro do cliente", example = "2024-10-30T10:00:00", readOnly = true)
    private LocalDateTime dataCadastro = LocalDateTime.now(); // Valor padrão

    @Column(unique = true, nullable = false, length = 11) // JPA: Único, não nulo e com tamanho fixo de 11
    @NotBlank(message = "CPF é obrigatório") // Validação
    @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos") // Validação: Tamanho exato
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter apenas números") // Validação: Garante que são apenas dígitos
    @Schema(description = "CPF do cliente (apenas números)", example = "12345678901", required = true)
    private String cpf;

    // Relacionamento JPA: Um Cliente pode ter Muitos Pedidos
    @OneToMany(
            mappedBy = "cliente", // 'mappedBy' indica que a entidade 'Pedido' gerencia este relacionamento
            cascade = CascadeType.ALL, // 'Cascade.ALL': Ações no Cliente (salvar, remover) afetam seus Pedidos
            orphanRemoval = true, // 'orphanRemoval': Pedidos removidos da lista são excluídos do banco
            fetch = FetchType.LAZY // 'FetchType.LAZY': Boa prática. Não carrega os pedidos do banco a menos que seja solicitado
    )
    @ToString.Exclude // Lombok: Exclui este campo do 'toString()' para evitar loops infinitos
    @Schema(description = "Lista de pedidos realizados pelo cliente")
    private List<Pedido> pedidos = new ArrayList<>();

    // --- Métodos de Negócio ---

    /**
     * Método utilitário para "desativar" um cliente.
     */
    public void inativar() {
        this.ativo = false;
    }

    // --- Construtores ---

    /**
     * Construtor padrão.
     * Necessário para o funcionamento da JPA.
     */
    public Cliente() {
    }

    /**
     * Construtor de conveniência para criar novos clientes.
     * Os campos 'ativo' e 'dataCadastro' são definidos por padrão.
     */
    public Cliente(String nome, String email, String telefone, String endereco, String cpf) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.cpf = cpf;
    }
}