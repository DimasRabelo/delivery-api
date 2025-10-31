package com.deliverytech.delivery.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import jakarta.validation.Valid; // Importação para validar objetos aninhados (como a lista de itens)
import jakarta.validation.constraints.*; // Importações para Validação (Bean Validation)
import com.deliverytech.delivery.validation.ValidCEP; // Importação da sua validação customizada de CEP
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) para a criação de um novo Pedido.
 * Esta classe define a estrutura de dados (Schema) que a API espera
 * receber no corpo (body) de uma requisição POST para criar um pedido.
 * Inclui validações para garantir que todos os dados necessários sejam fornecidos.
 */
@Schema(description = "Dados para criação de um novo pedido") // Documentação a nível de classe
public class PedidoDTO {

    // --- Identificação (Quem e De Onde) ---

    @Schema(description = "ID do cliente que está realizando o pedido", example = "1", required = true) // Documentação Swagger
    @NotNull(message = "Cliente ID é obrigatório") // Validação: Não pode ser nulo
    @Positive(message = "Cliente ID deve ser positivo") // Validação: Deve ser > 0
    private Long clienteId;

    @Schema(description = "ID do restaurante onde o pedido será realizado", example = "2", required = true) // Documentação Swagger
    @NotNull(message = "Restaurante ID é obrigatório") // Validação
    @Positive(message = "Restaurante ID deve ser positivo") // Validação
    private Long restauranteId;

    // --- Entrega ---

    @Schema(description = "Endereço completo para entrega", example = "Rua das Flores, 123 - Centro", required = true) // Documentação Swagger
    @NotBlank(message = "Endereço de entrega é obrigatório") // Validação: Não pode ser nulo ou vazio
    @Size(max = 200, message = "Endereço não pode exceder 200 caracteres") // Validação: Tamanho máximo
    private String enderecoEntrega;

    @Schema(description = "CEP do endereço de entrega", example = "12345-678", required = true) // Documentação Swagger
    @NotBlank(message = "CEP é obrigatório") // Validação
    @ValidCEP // Validação Customizada: Verifica o formato e a lógica do CEP
    private String cep;

    // --- Detalhes do Pedido ---

    @Schema(description = "Observações adicionais do pedido", example = "Deixar na portaria", required = false) // Documentação Swagger (opcional)
    @Size(max = 500, message = "Observações não podem exceder 500 caracteres") // Validação: Tamanho máximo (permite nulo ou vazio)
    private String observacoes;

    @Schema(description = "Forma de pagamento", example = "PIX", required = true) // Documentação Swagger
    @NotBlank(message = "Forma de pagamento é obrigatória") // Validação
    @Pattern( // Validação: Garante que o texto siga uma Expressão Regular (Regex)
            regexp = "^(DINHEIRO|CARTAO_CREDITO|CARTAO_DEBITO|PIX)$", // Regex: O valor deve ser exatamente uma dessas 4 strings
            message = "Forma de pagamento deve ser: DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO ou PIX"
    )
    private String formaPagamento;

    // --- Itens do Pedido (Lista Aninhada) ---

    @Schema(description = "Lista de itens do pedido", required = true) // Documentação Swagger
    @NotEmpty(message = "Pedido deve ter pelo menos um item") // Validação: A lista não pode estar vazia
    @Valid // Validação: Instrui o Spring a validar CADA objeto ItemPedidoDTO dentro desta lista
    private List<ItemPedidoDTO> itens = new ArrayList<>();

    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}