package com.deliverytech.delivery.entity;

import com.deliverytech.delivery.enums.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@ToString(exclude = {"cliente", "restaurante", "itens", "enderecoEntrega", "entregador"}) // Excluir novos campos
@Schema(description = "Entidade que representa um pedido completo no sistema")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do pedido", example = "1001")
    private Long id;

    @PastOrPresent(message = "A data do pedido não pode ser no futuro")
    @Schema(description = "Data e hora em que o pedido foi confirmado", example = "2024-06-05T19:30:00")
    private LocalDateTime dataPedido;


    // --- MUDANÇA 1: ENDEREÇO DE ENTREGA (Gargalo 1) ---
    // O campo 'String enderecoEntrega' foi removido.
    /**
     * Aponta para o Endereco (da lista do usuário) que foi selecionado para esta entrega.
     */
    @NotNull(message = "Endereço de entrega é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_entrega_id")
    @Schema(description = "O endereço selecionado pelo cliente para esta entrega")
    private Endereco enderecoEntrega;


    @PositiveOrZero
    @Schema(description = "Valor somado de todos os itens", example = "85.00")
    private BigDecimal subtotal;

    @PositiveOrZero
    @Schema(description = "Valor da taxa de entrega", example = "5.00")
    private BigDecimal taxaEntrega;

    @PositiveOrZero
    @Schema(description = "Valor total (Subtotal + Taxa de Entrega)", example = "90.00")
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Status atual do pedido", example = "PENDENTE")
    private StatusPedido status;


    // --- MUDANÇA 2: PAGAMENTO (Gargalo 3) ---
    @NotBlank(message = "Método de pagamento é obrigatório")
    @Schema(description = "Método de pagamento escolhido", example = "DINHEIRO", required = true)
    private String metodoPagamento; // Ex: "PIX", "CREDIT_CARD", "DINHEIRO"

    @PositiveOrZero
    @Schema(description = "Se o método for DINHEIRO, valor para o qual o cliente precisa de troco", example = "100.00", nullable = true)
    private BigDecimal trocoPara; // Nulo se não for DINHEIRO


    // --- Relacionamentos (Cliente e Restaurante) ---
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @Schema(description = "Cliente que realizou o pedido")
    private Cliente cliente;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id")
    @Schema(description = "Restaurante que receberá o pedido")
    private Restaurante restaurante;


    // --- MUDANÇA 3: ENTREGADOR (Gargalo 3) ---
    /**
     * O Entregador (Usuário com Role.ENTREGADOR) atribuído a esta entrega.
     * É nulo quando o pedido é criado e preenchido pela API.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id")
    @Schema(description = "Entregador (Usuário) atribuído a esta entrega", nullable = true)
    private Usuario entregador; // Nulo no início


    // --- Lista de Itens (Seu código original) ---
    @OneToMany(
            mappedBy = "pedido",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Schema(description = "Lista dos itens que compõem o pedido")
    private List<ItemPedido> itens = new ArrayList<>();


    // --- Campos Finais (Seu código original) ---
    @Column(unique = true)
    @NotBlank
    @Schema(description = "Número de identificação único para acompanhamento", example = "PED-20240605-1001", required = true)
    private String numeroPedido;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Observações ou instruções adicionais do cliente", example = "Tirar a cebola.")
    private String observacoes;

    
    // --- MÉTODOS DE LÓGICA DE NEGÓCIO (Seu código original - Perfeito!) ---
    
    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
        recalcularSubtotal();
        this.valorTotal = calcularValorTotal();
    }

    public void recalcularSubtotal() {
        this.itens.forEach(ItemPedido::calcularSubtotal);
        this.subtotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal taxa = (taxaEntrega != null) ? taxaEntrega : BigDecimal.ZERO;
        BigDecimal sub = (subtotal != null) ? subtotal : BigDecimal.ZERO;
        return sub.add(taxa);
    }

    public void confirmar() {
        this.status = StatusPedido.CONFIRMADO;
        this.dataPedido = LocalDateTime.now();
        this.valorTotal = calcularValorTotal();
    }
}