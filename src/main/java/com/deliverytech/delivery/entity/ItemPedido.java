package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.persistence.*; // Importações para JPA (Persistência de Dados)
import jakarta.validation.constraints.Min; // Importações para Validação de dados
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data; // Lombok: Gera getters, setters, toString, equals, hashCode
import lombok.ToString; // Importação para usar o @ToString.Exclude

import java.math.BigDecimal;

/**
 * Representa a entidade ItemPedido (tabela associativa).
 * Esta é a "linha" de um pedido, conectando um Pedido a um Produto
 * e armazenando a quantidade e o preço daquele momento.
 */
@Entity
@Data // Lombok: Gera getters, setters, toString, equals, hashCode.
@Table(name = "itens_pedido") // JPA: Define o nome da tabela no banco
@Schema(description = "Entidade que representa um item dentro de um pedido (produto, quantidade, preço)")
public class ItemPedido {

    @Id // Define como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento
    @Schema(description = "Identificador único do item no pedido", example = "501")
    private Long id;

    @NotNull(message = "Quantidade é obrigatória") // Validação: Não pode ser nula
    @Min(value = 1, message = "A quantidade deve ser de pelo menos 1") // Validação: Deve ser no mínimo 1
    @Schema(description = "Quantidade deste produto no pedido", example = "2", required = true, minimum = "1")
    private Integer quantidade;

    @NotNull(message = "Preço unitário é obrigatório") // Validação
    @PositiveOrZero(message = "Preço unitário não pode ser negativo") // Validação
    @Schema(description = "Preço do produto no momento da compra (snapshot)", example = "35.50", required = true, minimum = "0")
    private BigDecimal precoUnitario; // Este é o preço "congelado" no momento da compra

    @NotNull(message = "Subtotal é obrigatório") // Validação
    @PositiveOrZero(message = "Subtotal não pode ser negativo") // Validação
    @Schema(description = "Subtotal (quantidade * precoUnitario) deste item", example = "71.00", required = true, minimum = "0")
    private BigDecimal subtotal; // (quantidade * precoUnitario)

    // --- Relacionamentos ---

    // Relacionamento JPA: Muitos Itens pertencem a Um Pedido
    @ManyToOne(fetch = FetchType.LAZY) // 'FetchType.LAZY' é uma boa prática para performance
    @JoinColumn(name = "pedido_id") // Define o nome da coluna de chave estrangeira (FK)
    @NotNull(message = "O item deve estar associado a um pedido") // Validação: Um item não existe sem um pedido
    @ToString.Exclude // IMPORTANTE: Evita loop infinito no toString() gerado pelo Lombok
    @Schema(description = "O Pedido ao qual este item pertence")
    private Pedido pedido;

    // Relacionamento JPA: Muitos Itens podem se referir ao Mesmo Produto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id") // Define o nome da coluna de chave estrangeira (FK)
    @NotNull(message = "O item deve estar associado a um produto") // Validação: Um item não existe sem um produto
    @Schema(description = "O Produto que está sendo comprado")
    private Produto produto;

    // --- Construtores ---

    /**
     * Construtor padrão (vazio).
     * Necessário para o funcionamento da JPA.
     */
    public ItemPedido() {}

    /**
     * Construtor de conveniência para criar um novo item.
     * Ele "congela" o preço do produto (precoUnitario) no momento da criação.
     *
     * @param produto O produto a ser adicionado
     * @param quantidade A quantidade desejada
     */
    public ItemPedido(Produto produto, int quantidade) {
        this.produto = produto;
        this.quantidade = quantidade;
        
        // Lógica de negócio: "Congela" o preço do produto no item
        if (produto != null && produto.getPreco() != null) {
            this.precoUnitario = produto.getPreco(); 
        } else {
            this.precoUnitario = BigDecimal.ZERO;
        }
        
        // Calcula o subtotal inicial
        calcularSubtotal();
    }

    // --- Lógica de Negócio ---

    /**
     * Lógica de Negócio: Calcula (ou recalcula) o subtotal deste item.
     * Garante que o subtotal seja (precoUnitario * quantidade).
     */
    public void calcularSubtotal() {
        if (precoUnitario != null && quantidade != null && quantidade > 0) {
            this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
        } else {
            this.subtotal = BigDecimal.ZERO;
        }
    }
}