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
 * Representa a entidade Restaurante no banco de dados.
 * Contém informações cadastrais, de contato e operacionais do restaurante.
 */
@Entity
@Data
@ToString(exclude = {"produtos", "pedidos", "endereco"}) // Adicionado 'endereco' ao exclude
@Schema(description = "Entidade que representa um restaurante no sistema")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do restaurante", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100)
    @Schema(description = "Nome do restaurante", example = "Pizza Palace", required = true)
    private String nome;

    @NotBlank(message = "Categoria é obrigatória")
    @Schema(description = "Categoria/tipo de culinária", example = "Italiana", required = true)
    private String categoria;

    
    // --- CAMPO 'String endereco' REMOVIDO DAQUI ---
    // @NotBlank(message = "Endereço é obrigatório")
    // @Schema(description = "Endereço completo do restaurante", ...)
    // private String endereco;


    // --- MUDANÇA: SUBSTITUÍDO PELA ENTIDADE 'Endereco' ---
    /**
     * Endereço físico estruturado do restaurante.
     * Ligado à entidade Endereco.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // Salva/Exclui o Endereço junto com o Restaurante
    @JoinColumn(name = "endereco_id", referencedColumnName = "id") // Chave Estrangeira na tabela 'restaurante'
    @Schema(description = "Endereço físico estruturado do restaurante")
    private Endereco endereco;


    @NotBlank(message = "Telefone é obrigatório")
    @Schema(description = "Telefone de contato", example = "(11) 1234-5678", required = true)
    private String telefone;

    @Column(name = "taxa_entrega")
    @Schema(description = "Valor da taxa de entrega", example = "5.00")
    private BigDecimal taxaEntrega;

    @Schema(description = "Indica se o restaurante está ativo", example = "true", defaultValue = "true")
    private Boolean ativo;

    // Relacionamento JPA: Um Restaurante pode ter Muitos Produtos
    @OneToMany(mappedBy = "restaurante")
    @Schema(description = "Lista de produtos oferecidos pelo restaurante")
    private List<Produto> produtos;

    // Relacionamento JPA: Um Restaurante pode ter Muitos Pedidos
    @OneToMany(mappedBy = "restaurante")
    @Schema(description = "Lista de pedidos recebidos pelo restaurante")
    private List<Pedido> pedidos;

    @Column(precision = 3, scale = 2)
    @Schema(description = "Avaliação média (0 a 5 estrelas)", example = "4.5", minimum = "0", maximum = "5")
    private BigDecimal avaliacao;
    
    @Column(name = "tempo_entrega")
    @Schema(description = "Tempo médio de entrega estimado em minutos", example = "45")
    private Integer tempoEntrega;

    @Column(name = "horario_funcionamento")
    @Schema(description = "Horário de funcionamento", example = "18:00 - 23:30")
    private String horarioFuncionamento;

    
    // Construtores, Getters e Setters são gerenciados pelo @Data do Lombok
    // Talvez seja necessário um método utilitário para setar o endereço:
    
    public void setEndereco(Endereco endereco) {
        // Se a entidade Endereco não tiver um link de volta para Restaurante, 
        // esta atribuição simples é suficiente.
        this.endereco = endereco;
    }
}