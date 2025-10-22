package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.*;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.PagedResponseWrapper;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.service.RestauranteService;
import com.deliverytech.delivery.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/restaurantes")
@CrossOrigin(origins = "*")
@Tag(name = "Restaurantes", description = "Operações relacionadas aos restaurantes")
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;

    @Autowired
    private ProdutoService produtoService;

    // Método para cadastrar um novo restaurante
    @PostMapping
    @Operation(summary = "Cadastrar restaurante", description = "Cria um novo restaurante no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurante criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Restaurante já existe")
    })
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> cadastrar(
            @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do restaurante a ser criado"
            ) RestauranteDTO dto) {

        RestauranteResponseDTO restaurante = restauranteService.cadastrarRestaurante(dto);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Restaurante criado com sucesso");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Método para listar restaurantes com filtros e paginação
    @GetMapping
    @Operation(summary = "Listar restaurantes", description = "Lista restaurantes com filtros opcionais e paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    })
    public ResponseEntity<PagedResponseWrapper<RestauranteResponseDTO>> listar(
            @Parameter(description = "Categoria do restaurante") @RequestParam(required = false) String categoria,
            @Parameter(description = "Status ativo do restaurante") @RequestParam(required = false) Boolean ativo,
            @Parameter(description = "Parâmetros de paginação") Pageable pageable) {

        Page<RestauranteResponseDTO> restaurantes = restauranteService.listarRestaurantes(categoria, ativo, pageable);
        PagedResponseWrapper<RestauranteResponseDTO> response = new PagedResponseWrapper<>(restaurantes);
        return ResponseEntity.ok(response);
    }

    // Método para buscar um restaurante pelo ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar restaurante por ID", description = "Recupera um restaurante específico pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> buscarPorId(
            @Parameter(description = "ID do restaurante") @PathVariable @Positive(message = "O ID deve ser positivo") Long id) {

        RestauranteResponseDTO restaurante = restauranteService.buscarRestaurantePorId(id);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Restaurante encontrado");
        return ResponseEntity.ok(response);
    }

    // Método para atualizar os dados de um restaurante existente
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar restaurante", description = "Atualiza os dados de um restaurante existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> atualizar(
            @Parameter(description = "ID do restaurante") @PathVariable @Positive(message = "O ID deve ser positivo") Long id,
            @Valid @RequestBody RestauranteDTO dto) {

        RestauranteResponseDTO restaurante = restauranteService.atualizarRestaurante(id, dto);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Restaurante atualizado com sucesso");

        return ResponseEntity.ok(response);
    }

    // Método para ativar ou desativar um restaurante
    @PatchMapping("/{id}/status")
    @Operation(summary = "Ativar/Desativar restaurante", description = "Alterna o status ativo/inativo do restaurante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> alterarStatus(
            @Parameter(description = "ID do restaurante") @PathVariable @Positive(message = "O ID deve ser positivo") Long id) {

        RestauranteResponseDTO restaurante = restauranteService.alterarStatusRestaurante(id);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Status alterado com sucesso");

        return ResponseEntity.ok(response);
    }

    // Método para buscar restaurantes por categoria
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar por categoria", description = "Lista restaurantes de uma categoria específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurantes encontrados")
    })
    public ResponseEntity<ApiResponseWrapper<List<RestauranteResponseDTO>>> buscarPorCategoria(
            @Parameter(description = "Categoria do restaurante") @PathVariable @NotBlank(message = "A categoria não pode ser vazia") String categoria) {

        List<RestauranteResponseDTO> restaurantes = restauranteService.buscarRestaurantesPorCategoria(categoria);
        ApiResponseWrapper<List<RestauranteResponseDTO>> response =
                new ApiResponseWrapper<>(true, restaurantes, "Restaurantes encontrados");

        return ResponseEntity.ok(response);
    }

    // Método para calcular a taxa de entrega de um restaurante para um CEP específico
    @GetMapping("/{id}/taxa-entrega/{cep}")
    @Operation(summary = "Calcular taxa de entrega", description = "Calcula a taxa de entrega para um CEP específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Taxa calculada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<BigDecimal>> calcularTaxaEntrega(
            @Parameter(description = "ID do restaurante") @PathVariable @Positive(message = "O ID deve ser positivo") Long id,
            @Parameter(description = "CEP de destino") @PathVariable @NotBlank(message = "O CEP é obrigatório") String cep) {

        BigDecimal taxa = restauranteService.calcularTaxaEntrega(id, cep);
        ApiResponseWrapper<BigDecimal> response =
                new ApiResponseWrapper<>(true, taxa, "Taxa calculada com sucesso");

        return ResponseEntity.ok(response);
    }

    // Método para buscar restaurantes próximos a um CEP
    @GetMapping("/proximos/{cep}")
    @Operation(summary = "Restaurantes próximos", description = "Lista restaurantes próximos a um CEP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurantes próximos encontrados")
    })
    public ResponseEntity<ApiResponseWrapper<List<RestauranteResponseDTO>>> buscarProximos(
            @Parameter(description = "CEP de referência") @PathVariable @NotBlank(message = "O CEP é obrigatório") String cep,
            @Parameter(description = "Raio em km") @RequestParam(defaultValue = "10") @Positive(message = "O raio deve ser positivo") Integer raio) {

        List<RestauranteResponseDTO> restaurantes = restauranteService.buscarRestaurantesProximos(cep, raio);
        ApiResponseWrapper<List<RestauranteResponseDTO>> response =
                new ApiResponseWrapper<>(true, restaurantes, "Restaurantes próximos encontrados");

        return ResponseEntity.ok(response);
    }

    // Método para listar todos os produtos de um restaurante
    @GetMapping("/{restauranteId}/produtos")
    @Operation(summary = "Produtos do restaurante", description = "Lista todos os produtos de um restaurante")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos encontrados"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarProdutosDoRestaurante(
            @Parameter(description = "ID do restaurante") @PathVariable @Positive(message = "O ID deve ser positivo") Long restauranteId,
            @Parameter(description = "Filtrar apenas disponíveis") @RequestParam(required = false) Boolean disponivel) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorRestaurante(restauranteId, disponivel);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Produtos encontrados");

        return ResponseEntity.ok(response);
    }
}
