package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.dto.RestauranteResponseDTO;
import java.math.BigDecimal;
import java.util.List;

public interface RestauranteService {

    RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto);

    RestauranteResponseDTO buscarRestaurantePorId(Long id);

    List<RestauranteResponseDTO> buscarRestaurantesPorCategoria(String categoria);

    List<RestauranteResponseDTO> buscarRestaurantesDisponiveis();

    RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto);

    void ativarDesativarRestaurante(Long id);

    BigDecimal calcularTaxaEntrega(Long restauranteId, String cep);
}
