package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.RestauranteDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface de serviço para gerenciamento de restaurantes.
 * Contém métodos de cadastro, atualização, busca e cálculos relacionados.
 */
public interface RestauranteService {

    // ==========================================================
    // --- CADASTRO E ATUALIZAÇÃO ---
    // ==========================================================
    /**
     * Cadastra um novo restaurante no sistema.
     * @param dto DTO com dados do restaurante
     * @return RestauranteResponseDTO com informações do restaurante cadastrado
     */
    RestauranteResponseDTO cadastrarRestaurante(RestauranteDTO dto);

    /**
     * Atualiza os dados de um restaurante existente.
     * @param id ID do restaurante
     * @param dto DTO com dados atualizados
     * @return RestauranteResponseDTO atualizado
     */
    RestauranteResponseDTO atualizarRestaurante(Long id, RestauranteDTO dto);

    // ==========================================================
    // --- BUSCAS ---
    // ==========================================================
    /**
     * Busca um restaurante pelo ID.
     * @param id ID do restaurante
     * @return RestauranteResponseDTO com os dados encontrados
     */
    RestauranteResponseDTO buscarRestaurantePorId(Long id);

    /**
     * Busca restaurantes ativos por categoria.
     * @param categoria Categoria de culinária
     * @return Lista de Restaurantes encontrados
     */
    List<RestauranteResponseDTO> buscarRestaurantesPorCategoria(String categoria);

    /**
     * Busca todos os restaurantes disponíveis (ativos).
     * @return Lista de restaurantes disponíveis
     */
    List<RestauranteResponseDTO> buscarRestaurantesDisponiveis();

    // ==========================================================
    // --- STATUS E DISPONIBILIDADE ---
    // ==========================================================
    /**
     * Altera o status de ativo/inativo do restaurante.
     * @param id ID do restaurante
     * @return RestauranteResponseDTO atualizado
     */
    RestauranteResponseDTO alterarStatusRestaurante(Long id);

    // ==========================================================
    // --- CÁLCULOS E LOCALIZAÇÃO ---
    // ==========================================================
    /**
     * Calcula a taxa de entrega para um restaurante e um CEP específico.
     * @param restauranteId ID do restaurante
     * @param cep CEP do cliente
     * @return Valor da taxa de entrega
     */
    BigDecimal calcularTaxaEntrega(Long restauranteId, String cep);

    /**
     * Busca restaurantes próximos de um CEP dentro de um raio determinado.
     * @param cep CEP de referência
     * @param raio Raio em quilômetros
     * @return Lista de restaurantes próximos
     */
    List<RestauranteResponseDTO> buscarRestaurantesProximos(String cep, Integer raio);

    // ==========================================================
    // --- LISTAGEM PAGINADA ---
    // ==========================================================
    /**
     * Lista restaurantes com filtros opcionais de categoria e status ativo/inativo.
     * @param categoria Categoria de culinária (opcional)
     * @param ativo Status do restaurante (opcional)
     * @param pageable Informações de paginação
     * @return Página de restaurantes
     */
    Page<RestauranteResponseDTO> listarRestaurantes(String categoria, Boolean ativo, Pageable pageable);
}
