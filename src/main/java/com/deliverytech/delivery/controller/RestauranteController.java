package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.RestauranteDTO;
import com.deliverytech.delivery.dto.RestauranteResponseDTO;
import com.deliverytech.delivery.service.RestauranteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/restaurantes")
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;

    // ✅ POST /api/restaurantes - Cadastrar restaurante
    @PostMapping
    public ResponseEntity<RestauranteResponseDTO> cadastrar(@Valid @RequestBody RestauranteDTO dto) {
        RestauranteResponseDTO response = restauranteService.cadastrarRestaurante(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ GET /api/restaurantes/{id} - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> buscarPorId(@PathVariable Long id) {
        RestauranteResponseDTO response = restauranteService.buscarRestaurantePorId(id);
        return ResponseEntity.ok(response);
    }

    // ✅ GET /api/restaurantes - Listar restaurantes disponíveis (ativos)
    @GetMapping
    public ResponseEntity<List<RestauranteResponseDTO>> listarDisponiveis() {
        List<RestauranteResponseDTO> response = restauranteService.buscarRestaurantesDisponiveis();
        return ResponseEntity.ok(response);
    }

    // ✅ GET /api/restaurantes/categoria/{categoria} - Filtrar por categoria
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<RestauranteResponseDTO>> buscarPorCategoria(@PathVariable String categoria) {
        List<RestauranteResponseDTO> response = restauranteService.buscarRestaurantesPorCategoria(categoria);
        return ResponseEntity.ok(response);
    }

    // ✅ PUT /api/restaurantes/{id} - Atualizar restaurante
    @PutMapping("/{id}")
    public ResponseEntity<RestauranteResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody RestauranteDTO dto) {
        RestauranteResponseDTO response = restauranteService.atualizarRestaurante(id, dto);
        return ResponseEntity.ok(response);
    }

    // ✅ PATCH /api/restaurantes/{id}/status - Ativar/Desativar restaurante
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> ativarDesativar(@PathVariable Long id) {
        restauranteService.ativarDesativarRestaurante(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ GET /api/restaurantes/{id}/taxa-entrega/{cep} - Calcular taxa de entrega
    @GetMapping("/{id}/taxa-entrega/{cep}")
    public ResponseEntity<BigDecimal> calcularTaxaEntrega(@PathVariable Long id, @PathVariable String cep) {
        BigDecimal taxa = restauranteService.calcularTaxaEntrega(id, cep);
        return ResponseEntity.ok(taxa);
    }

    
}
