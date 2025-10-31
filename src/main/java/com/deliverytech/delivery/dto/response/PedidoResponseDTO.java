package com.deliverytech.delivery.dto.response;

import com.deliverytech.delivery.dto.ItemPedidoDTO;
import io.swagger.v3.oas.annotations.media.Schema; // Importação para documentação OpenAPI/Swagger
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO (Data Transfer Object) de resposta de um pedido, enviado pelo backend.
 * Esta classe define a estrutura de dados (Schema) que a API retorna
 * ao consultar os detalhes de um pedido.
 */
@Schema(description = "DTO de resposta com os dados de um pedido") // Documentação a nível de classe
public class PedidoResponseDTO {

    // --- Identificação ---

    @Schema(description = "ID do pedido", example = "1") // Documentação Swagger
    private Long id;

    // --- Cliente ---

    @Schema(description = "ID do cliente", example = "1") // Documentação Swagger
    private Long clienteId;

    @Schema(description = "Nome do cliente", example = "João Silva") // Documentação Swagger
    private String clienteNome;

    // --- Restaurante ---

    @Schema(description = "ID do restaurante", example = "2") // Documentação Swagger
    private Long restauranteId;

    @Schema(description = "Nome do restaurante", example = "Pizza Express") // Documentação Swagger
    private String restauranteNome;

    // --- Detalhes da Entrega e Status ---

    @Schema(description = "Endereço de entrega", example = "Rua das Flores, 123") // Documentação Swagger
    private String enderecoEntrega;

    @Schema(description = "Status do pedido", example = "EM_PREPARO") // Documentação Swagger
    private String status; // Usar String é comum para simplificar o DTO (em vez de Enum)

    // --- Valores e Itens ---

    @Schema(description = "Valor total do pedido", example = "55.50") // Documentação Swagger
    private BigDecimal total;

    @Schema(description = "Lista de itens do pedido") // Documentação Swagger
    private List<ItemPedidoDTO> itens; // Lista aninhada de DTOs

    // ===================================================
    // GETTERS E SETTERS
    // (Necessários pois a classe não usa Lombok @Data)
    // ===================================================
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public Long getRestauranteId() { return restauranteId; }
    public void setRestauranteId(Long restauranteId) { this.restauranteId = restauranteId; }

    public String getRestauranteNome() { return restauranteNome; }
    public void setRestauranteNome(String restauranteNome) { this.restauranteNome = restauranteNome; }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<ItemPedidoDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoDTO> itens) { this.itens = itens; }
}