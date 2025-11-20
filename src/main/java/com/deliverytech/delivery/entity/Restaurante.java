package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entidade que representa um restaurante.
 * Armazena informações cadastrais, endereço e dados operacionais.
 */
@Entity
@Data
@ToString(exclude = {"produtos", "pedidos", "endereco"})
@Schema(description = "Entidade que representa um restaurante no sistema")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do restaurante", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    @Schema(description = "Nome do restaurante", example = "Pizza Palace", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @NotBlank(message = "Categoria é obrigatória")
    @Schema(description = "Tipo de culinária ou categoria do restaurante", example = "Italiana")
    private String categoria;

    /** Endereço físico do restaurante */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", referencedColumnName = "id")
    private Endereco endereco;

    @NotBlank(message = "Telefone é obrigatório")
    @Schema(description = "Telefone de contato", example = "(11) 1234-5678")
    private String telefone;

    @Column(name = "taxa_entrega")
    @Schema(description = "Valor da taxa de entrega", example = "5.00")
    private BigDecimal taxaEntrega;

    @Schema(description = "Indica se o restaurante está ativo", example = "true", defaultValue = "true")
    private Boolean ativo;

    /** Lista de produtos oferecidos pelo restaurante */
    @OneToMany(mappedBy = "restaurante")
    @Schema(description = "Lista de produtos do restaurante")
    private List<Produto> produtos;

    /** Lista de pedidos realizados neste restaurante */
    @OneToMany(mappedBy = "restaurante")
    @Schema(description = "Lista de pedidos do restaurante")
    private List<Pedido> pedidos;

    @Column(precision = 3, scale = 2)
    @Schema(description = "Avaliação média (0 a 5 estrelas)", example = "4.5")
    private BigDecimal avaliacao;

    @Column(name = "tempo_entrega")
    @Schema(description = "Tempo médio de entrega em minutos", example = "45")
    private Integer tempoEntrega;

    @Column(name = "horario_funcionamento")
    @Schema(description = "Horário de funcionamento", example = "18:00 - 23:30")
    private String horarioFuncionamento;

    /** Define o endereço do restaurante */
    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }
}
