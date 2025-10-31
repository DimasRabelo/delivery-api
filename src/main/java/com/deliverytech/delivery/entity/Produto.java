package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.persistence.*; // Importações para JPA (Persistência de Dados)
import jakarta.validation.constraints.NotBlank; // Importações para Validação de dados
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data; // Lombok para reduzir boilerplate

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

/**
 * Representa a entidade Produto no banco de dados.
 * Também conhecido como item de cardápio, está sempre associado a um Restaurante.
 */
@Entity // Marca esta classe como uma entidade gerenciada pela JPA
@Data   // Anotação do Lombok que gera Getters, Setters, etc.
@Schema(description = "Entidade que representa um produto (item de cardápio) de um restaurante")
public class Produto {

    @Id // Define este campo como a chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração automática do ID (auto-incremento)
    @Schema(description = "Identificador único do produto", example = "101")
    private Long id;

    @NotBlank(message = "Nome é obrigatório") // Validação: Não pode ser nulo ou vazio
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres") // Validação: Tamanho
    @Schema(description = "Nome do produto", example = "Pizza Margherita", required = true)
    private String nome;

    @Size(max = 255, message = "Descrição não pode exceder 255 caracteres") // Validação: Tamanho máximo
    @Schema(description = "Descrição detalhada do produto", example = "Molho de tomate fresco, mozzarella e manjericão")
    private String descricao;

    @NotNull(message = "Preço é obrigatório") // Validação: Não pode ser nulo
    @PositiveOrZero(message = "Preço deve ser zero ou positivo") // Validação: Não pode ser negativo
    @Schema(description = "Preço unitário do produto", example = "45.00", required = true, minimum = "0")
    private BigDecimal preco;

    @Schema(description = "Categoria do produto dentro do restaurante", example = "Pizzas Tradicionais")
    private String categoria;

    @Schema(description = "Indica se o produto está disponível para venda", example = "true", defaultValue = "true")
    private Boolean disponivel;

    @PositiveOrZero(message = "Estoque deve ser zero ou positivo") // Validação
    @Schema(description = "Quantidade do produto em estoque (se aplicável)", example = "50", minimum = "0")
    private int estoque;

    // Relacionamento JPA: Muitos Produtos pertencem a Um Restaurante
    @ManyToOne
    @JoinColumn(name = "restaurante_id") // Define a coluna de chave estrangeira (FK) nesta tabela ('produto')
    @Schema(description = "Restaurante ao qual este produto pertence")
    private Restaurante restaurante;

    // Relacionamento JPA: Um Produto pode estar em Muitos ItensPedido
    @OneToMany(mappedBy = "produto") // 'mappedBy' indica que 'ItemPedido' gerencia o relacionamento
    @Schema(description = "Lista de itens de pedido associados a este produto")
    private List<ItemPedido> itensPedido = new ArrayList<>();
    
}