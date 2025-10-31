package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.persistence.*; // Importações para JPA (Persistência de Dados)
import jakarta.validation.constraints.NotBlank; // Importações para Validação de dados
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data; // Lombok para reduzir boilerplate (getters, setters, etc.)

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa a entidade Pedido no banco de dados.
 * Esta classe armazena todas as informações sobre um pedido,
 * desde os itens e valores até o cliente e o status da entrega.
 */
@Entity // Marca esta classe como uma entidade gerenciada pela JPA
@Data   // Anotação do Lombok que gera Getters, Setters, toString, equals, etc.
@Schema(description = "Entidade que representa um pedido realizado por um cliente a um restaurante")
public class Pedido {

    @Id // Define este campo como a chave primária da tabela
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração automática do ID pelo banco (auto-incremento)
    @Schema(description = "Identificador único do pedido", example = "1001")
    private Long id;

    @PastOrPresent(message = "A data do pedido não pode ser no futuro") // Validação: Garante que a data não seja futura
    @Schema(description = "Data e hora em que o pedido foi confirmado", example = "2024-06-05T19:30:00")
    private LocalDateTime dataPedido;

    @Schema(description = "Endereço completo para a entrega do pedido", example = "Rua dos Clientes, 789 - Apto 101, Bairro Feliz")
    private String enderecoEntrega;

    @PositiveOrZero // Validação: O valor não pode ser negativo
    @Schema(description = "Valor somado de todos os itens do pedido (calculado)", example = "85.00", minimum = "0")
    private BigDecimal subtotal;

    @PositiveOrZero // Validação: O valor não pode ser negativo
    @Schema(description = "Valor da taxa de entrega cobrada pelo restaurante", example = "5.00", minimum = "0")
    private BigDecimal taxaEntrega;

    @PositiveOrZero // Validação: O valor não pode ser negativo
    @Schema(description = "Valor total do pedido (Subtotal + Taxa de Entrega)", example = "90.00", minimum = "0")
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING) // Diz à JPA para salvar o Enum como String ("PENDENTE", "CONFIRMADO") em vez de número (0, 1)
    @Schema(description = "Status atual do pedido", example = "PENDENTE", defaultValue = "PENDENTE")
    private StatusPedido status;

    @NotNull // Validação: Garante que o pedido esteja associado a um cliente
    @ManyToOne // Relacionamento JPA: Muitos Pedidos podem pertencer a Um Cliente
    @JoinColumn(name = "cliente_id") // Define o nome da coluna de chave estrangeira no banco
    @Schema(description = "Cliente que realizou o pedido")
    private Cliente cliente;

    @NotNull // Validação: Garante que o pedido esteja associado a um restaurante
    @ManyToOne // Relacionamento JPA: Muitos Pedidos podem pertencer a Um Restaurante
    @JoinColumn(name = "restaurante_id") // Define o nome da coluna de chave estrangeira no banco
    @Schema(description = "Restaurante que receberá o pedido")
    private Restaurante restaurante;

    // Relacionamento JPA: Um Pedido possui Muitos ItensPedido
    @OneToMany(
            mappedBy = "pedido", // 'mappedBy' indica que a entidade 'ItemPedido' é a dona do relacionamento (ela possui o @ManyToOne)
            cascade = CascadeType.ALL, // 'Cascade.ALL': Se um Pedido for salvo/atualizado/removido, seus ItensPedido também serão.
            orphanRemoval = true // 'orphanRemoval': Se um ItemPedido for removido da 'List<Itens>', ele deve ser excluído do banco.
    )
    @Schema(description = "Lista dos itens que compõem o pedido")
    private List<ItemPedido> itens = new ArrayList<>();

    @Column(unique = true) // Garante que não haja números de pedido duplicados no banco de dados
    @NotBlank // Validação: Não pode ser nulo ou vazio
    @Schema(description = "Número de identificação único para acompanhamento (gerado pelo sistema)", example = "PED-20240605-1001", required = true)
    private String numeroPedido;

    @Column(columnDefinition = "TEXT") // Define o tipo da coluna como TEXT para permitir strings mais longas
    @Schema(description = "Observações ou instruções adicionais do cliente", example = "Tirar a cebola da pizza, por favor. Entregar no bloco B.")
    private String observacoes;

    // =================================================================
    // MÉTODOS DE LÓGICA DE NEGÓCIO
    // (Gerenciam o estado interno da entidade Pedido)
    // =================================================================

    /**
     * Adiciona um item à lista de itens do pedido e atualiza os totais.
     * Garante a consistência do relacionamento bidirecional.
     *
     * @param item O ItemPedido a ser adicionado.
     */
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);            // Define a referência bidirecional (ItemPedido -> Pedido)
        this.itens.add(item);            // Adiciona o item à lista
        recalcularSubtotal();            // Atualiza o subtotal do pedido
        this.valorTotal = calcularValorTotal(); // Atualiza o valor total
    }

    /**
     * Recalcula o subtotal do pedido somando os subtotais de todos os itens.
     * Primeiro, garante que cada item tenha seu próprio subtotal calculado.
     */
    public void recalcularSubtotal() {
        // Garante que o subtotal de cada item esteja correto
        this.itens.forEach(ItemPedido::calcularSubtotal);
        
        // Soma o subtotal de todos os itens para obter o subtotal do pedido
        this.subtotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Soma segura (começa do zero)
    }

    /**
     * Calcula o valor total do pedido (subtotal + taxa de entrega).
     * Trata casos onde a taxa ou subtotal podem ser nulos.
     *
     * @return O valor total.
     */
    public BigDecimal calcularValorTotal() {
        // Garante que valores nulos sejam tratados como zero
        BigDecimal taxa = (taxaEntrega != null) ? taxaEntrega : BigDecimal.ZERO;
        BigDecimal sub = (subtotal != null) ? subtotal : BigDecimal.ZERO;
        
        return sub.add(taxa);
    }

    /**
     * Método utilitário para "confirmar" um pedido.
     * Define o status, a data/hora atual e recalcula o valor total final.
     */
    public void confirmar() {
        this.status = StatusPedido.CONFIRMADO;
        this.dataPedido = LocalDateTime.now(); // Define a data de confirmação como agora
        this.valorTotal = calcularValorTotal(); // Garante que o valor total está correto no momento da confirmação
    }
}