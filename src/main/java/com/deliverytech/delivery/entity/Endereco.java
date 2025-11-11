package com.deliverytech.delivery.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.math.BigDecimal;

/**
 * Entidade que representa um endereço de entrega cadastrado por um usuário.
 */
@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(description = "Endereço de entrega do usuário")
public class Endereco {

    /** Identificador único do endereço */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do endereço", example = "10")
    private Long id;

    /** Usuário proprietário do endereço */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @Schema(description = "Usuário dono deste endereço")
    private Usuario usuario;

    /** Apelido do endereço (ex: Casa, Trabalho) */
    @NotBlank(message = "Apelido é obrigatório")
    @Size(max = 50)
    @Schema(description = "Apelido do endereço", example = "Casa", required = true)
    private String apelido;

    /** CEP (apenas números) */
    @NotBlank(message = "CEP é obrigatório")
    @Size(min = 8, max = 8, message = "CEP deve ter 8 dígitos")
    @Schema(description = "CEP (apenas números)", example = "01001000", required = true)
    private String cep;

    /** Rua ou logradouro */
    @NotBlank(message = "Rua é obrigatória")
    @Schema(description = "Nome da rua ou logradouro", example = "Praça da Sé", required = true)
    private String rua;

    /** Número do endereço */
    @NotBlank(message = "Número é obrigatório")
    @Size(max = 20)
    @Schema(description = "Número do endereço", example = "100", required = true)
    private String numero;

    /** Complemento (apto, bloco, etc.) */
    @Schema(description = "Complemento do endereço", example = "Apto 50")
    private String complemento;

    /** Bairro */
    @NotBlank(message = "Bairro é obrigatório")
    @Schema(description = "Bairro", example = "Sé", required = true)
    private String bairro;

    /** Cidade */
    @NotBlank(message = "Cidade é obrigatória")
    @Schema(description = "Cidade", example = "São Paulo", required = true)
    private String cidade;

    /** Estado (UF) */
    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "UF deve conter 2 caracteres")
    @Schema(description = "Sigla do estado (UF)", example = "SP", required = true)
    private String estado;

    /** Latitude para geolocalização (opcional) */
    @Schema(description = "Latitude para geolocalização", example = "-23.5505")
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    /** Longitude para geolocalização (opcional) */
    @Schema(description = "Longitude para geolocalização", example = "-46.6333")
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    /** Indica se o endereço está ativo (soft delete) */
    @Schema(description = "Indica se o endereço está ativo", example = "true", hidden = true)
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    /** Construtor prático (campo ativo é true por padrão) */
    public Endereco(Usuario usuario, String apelido, String cep, String rua, String numero, String complemento,
                    String bairro, String cidade, String estado) {
        this.usuario = usuario;
        this.apelido = apelido;
        this.cep = cep;
        this.rua = rua;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
    }
}
