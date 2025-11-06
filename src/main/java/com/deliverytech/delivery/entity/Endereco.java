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
 * Representa um endereço de entrega cadastrado por um Usuário.
 * Substitui o antigo campo 'String endereco'.
 */
@Entity
@Table(name = "endereco")
@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(description = "Representa um endereço de entrega estruturado")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único do endereço", example = "10")
    private Long id;

    /**
     * O usuário (cliente) que é dono deste endereço.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @Schema(description = "Usuário dono deste endereço")
    private Usuario usuario;

    @NotBlank(message = "Apelido é obrigatório")
    @Size(max = 50)
    @Schema(description = "Apelido do endereço (ex: Casa, Trabalho)", example = "Casa", required = true)
    private String apelido;

    @NotBlank(message = "CEP é obrigatório")
    @Size(min = 8, max = 8, message = "CEP deve ter 8 dígitos")
    @Schema(description = "CEP (apenas números)", example = "01001000", required = true)
    private String cep;

    @NotBlank(message = "Rua é obrigatória")
    @Schema(description = "Nome da rua/logradouro", example = "Praça da Sé", required = true)
    private String rua;

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 20)
    @Schema(description = "Número", example = "100", required = true)
    private String numero;

    @Schema(description = "Complemento (apto, bloco, etc.)", example = "Apto 50")
    private String complemento;

    @NotBlank(message = "Bairro é obrigatório")
    @Schema(description = "Bairro", example = "Sé", required = true)
    private String bairro;

    @NotBlank(message = "Cidade é obrigatória")
    @Schema(description = "Cidade", example = "São Paulo", required = true)
    private String cidade;

    @NotBlank(message = "Estado é obrigatório")
    @Size(min = 2, max = 2, message = "Estado (UF)")
    @Schema(description = "Sigla do estado (UF)", example = "SP", required = true)
    private String estado;

    @Schema(description = "Latitude para geolocalização (opcional)", example = "-23.5505")
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Schema(description = "Longitude para geolocalização (opcional)", example = "-46.6333")
    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    
    public Endereco(Usuario usuario, String apelido, String cep, String rua, String numero, String complemento, String bairro, String cidade, String estado) {
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