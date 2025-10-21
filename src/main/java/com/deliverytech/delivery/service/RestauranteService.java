package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RestauranteService {

    RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto);

    RestauranteResponseDTO buscarRestaurantePorId(Long id);

    List<RestauranteResponseDTO> buscarRestaurantesPorCategoria(String categoria);

    List<RestauranteResponseDTO> buscarRestaurantesDisponiveis();

    RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto);

    RestauranteResponseDTO alterarStatusRestaurante(Long id);

    BigDecimal calcularTaxaEntrega(Long restauranteId, String cep);

    List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raio);

    Page<RestauranteResponseDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable);
}
