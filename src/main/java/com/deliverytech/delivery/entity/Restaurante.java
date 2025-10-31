package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.persistence.*; // Importações para JPA (Persistência de Dados)
import jakarta.validation.constraints.NotBlank; // Importações para Validação de dados
import jakarta.validation.constraints.Size;
import lombok.Data; // Lombok para reduzir boilerplate (getters, setters, etc.)
import lombok.ToString; // Lombok para personalizar o método toString

import java.math.BigDecimal;
import java.util.List;

/**
 * Representa a entidade Restaurante no banco de dados.
 * Contém informações cadastrais, de contato e operacionais do restaurante.
 */
@Entity // Marca esta classe como uma entidade gerenciada pela JPA
@Data   // Anotação do Lombok que gera Getters, Setters, toString, equals, etc.
@ToString(exclude = {"produtos", "pedidos"}) // Evita recursão infinita (LazyInitializationException) ao chamar o toString()
@Schema(description = "Entidade que representa um restaurante no sistema")
public class Restaurante {

    @Id // Define este campo como a chave primária da tabela
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração automática do ID (auto-incremento)
    @Schema(description = "Identificador único do restaurante", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório") // Validação: Não pode ser nulo ou conter apenas espaços em branco
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") // Validação: Define o tamanho
    @Schema(description = "Nome do restaurante", example = "Pizza Palace", required = true)
    private String nome;

    @NotBlank(message = "Categoria é obrigatória") // Validação
    @Schema(description = "Categoria/tipo de culinária do restaurante", example = "Italiana", required = true)
    private String categoria;

    @NotBlank(message = "Endereço é obrigatório") // Validação
    @Schema(description = "Endereço completo do restaurante", example = "Rua das Pizzas, 123 - Centro, São Paulo - SP", required = true)
    private String endereco;

    @NotBlank(message = "Telefone é obrigatório") // Validação
    @Schema(description = "Telefone de contato do restaurante", example = "(11) 1234-5678", required = true)
    private String telefone;

    @Column(name = "taxa_entrega") // JPA: Mapeia este campo para a coluna 'taxa_entrega' no banco
    @Schema(description = "Valor da taxa de entrega do restaurante", example = "5.00")
    private BigDecimal taxaEntrega;

    @Schema(description = "Indica se o restaurante está ativo no sistema", example = "true", defaultValue = "true")
    private Boolean ativo;

    // Relacionamento JPA: Um Restaurante pode ter Muitos Produtos
    @OneToMany(mappedBy = "restaurante") // 'mappedBy' indica que a entidade 'Produto' é a dona do relacionamento
    @Schema(description = "Lista de produtos oferecidos pelo restaurante")
    private List<Produto> produtos;

    // Relacionamento JPA: Um Restaurante pode ter Muitos Pedidos
    @OneToMany(mappedBy = "restaurante") // 'mappedBy' indica que a entidade 'Pedido' é a dona do relacionamento
    @Schema(description = "Lista de pedidos recebidos pelo restaurante")
    private List<Pedido> pedidos;

    @Column(precision = 3, scale = 2) // JPA: Define precisão (total de dígitos = 3) e escala (dígitos após a vírgula = 2). Ex: 4.50
    @Schema(description = "Avaliação média do restaurante (0 a 5 estrelas)", example = "4.5", minimum = "0", maximum = "5")
    private BigDecimal avaliacao;

    
    @Column(name = "tempo_entrega") // JPA: Mapeia para a coluna 'tempo_entrega'
    @Schema(description = "Tempo médio de entrega estimado em minutos", example = "45")
    private Integer tempoEntrega;

    @Column(name = "horario_funcionamento") // JPA: Mapeia para a coluna 'horario_funcionamento'
    @Schema(description = "Horário de funcionamento do restaurante", example = "18:00 - 23:30")
    private String horarioFuncionamento;
}