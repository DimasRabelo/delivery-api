package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.RestauranteDTO;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.PagedResponseWrapper;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.dto.response.RestauranteResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;
import com.deliverytech.delivery.service.RestauranteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller para operações de CRUD e consulta relacionadas a Restaurantes.
 *
 * Expõe endpoints públicos (para clientes) e protegidos (para administração
 * e donos de restaurantes).
 */
@Validated // Habilita a validação de parâmetros de método (ex: @Positive nos @PathVariables)
@RestController
@RequestMapping("/api/restaurantes")
@Tag(name = "3. Restaurantes", description = "Operações públicas e privadas relacionadas aos restaurantes")
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;

    @Autowired
    private ProdutoService produtoService;

    /**
     * Cadastra um novo restaurante no sistema.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param dto DTO contendo os dados do novo restaurante.
     * @return ResponseEntity 201 (Created) com os dados do restaurante criado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar restaurante (ADMIN)",
               description = "Cria um novo restaurante no sistema. Requer permissão de ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurante criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)"),
            @ApiResponse(responseCode = "409", description = "Restaurante já existe (ex: CNPJ duplicado)")
    })
    @SecurityRequirement(name = "bearerAuth") // Indica ao Swagger que este endpoint é protegido
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> cadastrar(
            @Valid @RequestBody RestauranteDTO dto) {

        RestauranteResponseDTO restaurante = restauranteService.cadastrarRestaurante(dto);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Restaurante criado com sucesso");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos os restaurantes de forma paginada.
     * Permite filtrar por categoria e status (ativo). Endpoint público.
     *
     * @param categoria (Opcional) Filtra restaurantes pela categoria.
     * @param ativo     (Opcional) Filtra restaurantes pelo status (true/false).
     * @param pageable  Objeto de paginação (tamanho, página, ordenação).
     * @return ResponseEntity 200 (OK) com a página de restaurantes.
     */
    @GetMapping
    @Operation(summary = "Listar restaurantes (Público)",
               description = "Lista restaurantes com filtros opcionais e paginação.")
    public ResponseEntity<PagedResponseWrapper<RestauranteResponseDTO>> listar(
            @Parameter(description = "Categoria do restaurante") @RequestParam(required = false) String categoria,
            @Parameter(description = "Status ativo do restaurante") @RequestParam(required = false) Boolean ativo,
            @Parameter(description = "Parâmetros de paginação") Pageable pageable) {

        Page<RestauranteResponseDTO> restaurantes = restauranteService.listarRestaurantes(categoria, ativo, pageable);
        PagedResponseWrapper<RestauranteResponseDTO> response = new PagedResponseWrapper<>(restaurantes);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um restaurante específico pelo seu ID. Endpoint público.
     *
     * @param id O ID (Long) do restaurante.
     * @return ResponseEntity 200 (OK) com os dados do restaurante.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar restaurante por ID (Público)",
               description = "Recupera um restaurante específico pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> buscarPorId(
            @PathVariable @Positive(message = "O ID deve ser positivo") Long id) {

        RestauranteResponseDTO restaurante = restauranteService.buscarRestaurantePorId(id);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Restaurante encontrado");
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um restaurante existente.
     * Acesso restrito a 'ADMIN' ou ao dono do restaurante.
     *
     * @param id  O ID do restaurante a ser atualizado.
     * @param dto DTO com os novos dados.
     * @return ResponseEntity 200 (OK) com os dados atualizados do restaurante.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @restauranteService.isOwner(#id)")
    @Operation(summary = "Atualizar restaurante (ADMIN ou Dono)",
               description = "Atualiza os dados de um restaurante. Requer ADMIN ou ser o dono do restaurante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou dono)"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> atualizar(
            @PathVariable @Positive(message = "O ID deve ser positivo") Long id,
            @Valid @RequestBody RestauranteDTO dto) {

        RestauranteResponseDTO restaurante = restauranteService.atualizarRestaurante(id, dto);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Restaurante atualizado com sucesso");

        return ResponseEntity.ok(response);
    }

    /**
     * Altera o status (ativo/inativo) de um restaurante.
     * Acesso restrito a usuários com a role 'ADMIN'.
     *
     * @param id O ID do restaurante a ter o status alterado.
     * @return ResponseEntity 200 (OK) com os dados do restaurante e o novo status.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ativar/Desativar restaurante (ADMIN)",
               description = "Alterna o status ativo/inativo do restaurante. Requer permissão de ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<RestauranteResponseDTO>> alterarStatus(
            @PathVariable @Positive(message = "O ID deve ser positivo") Long id) {

        RestauranteResponseDTO restaurante = restauranteService.alterarStatusRestaurante(id);
        ApiResponseWrapper<RestauranteResponseDTO> response =
                new ApiResponseWrapper<>(true, restaurante, "Status alterado com sucesso");

        return ResponseEntity.ok(response);
    }

    /**
     * Busca restaurantes por uma categoria específica. Endpoint público.
     *
     * @param categoria O nome da categoria (ex: "Japonesa", "Pizza").
     * @return ResponseEntity 200 (OK) com a lista de restaurantes encontrados.
     */
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar por categoria (Público)",
               description = "Lista restaurantes de uma categoria específica.")
    @ApiResponse(responseCode = "200", description = "Restaurantes encontrados")
    public ResponseEntity<ApiResponseWrapper<List<RestauranteResponseDTO>>> buscarPorCategoria(
            @PathVariable @NotBlank(message = "A categoria não pode ser vazia") String categoria) {

        List<RestauranteResponseDTO> restaurantes = restauranteService.buscarRestaurantesPorCategoria(categoria);
        ApiResponseWrapper<List<RestauranteResponseDTO>> response =
                new ApiResponseWrapper<>(true, restaurantes, "Restaurantes encontrados");

        return ResponseEntity.ok(response);
    }

    /**
     * Calcula a taxa de entrega de um restaurante para um CEP de destino. Endpoint público.
     *
     * @param id  O ID do restaurante.
     * @param cep O CEP de destino para o cálculo.
     * @return ResponseEntity 200 (OK) com o valor da taxa.
     */
    @GetMapping("/{id}/taxa-entrega/{cep}")
    @Operation(summary = "Calcular taxa de entrega (Público)",
               description = "Calcula a taxa de entrega para um CEP específico a partir de um restaurante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Taxa calculada com sucesso"),
            @ApiResponse(responseCode = "400", description = "CEP inválido"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<BigDecimal>> calcularTaxaEntrega(
            @PathVariable @Positive(message = "O ID deve ser positivo") Long id,
            @PathVariable @NotBlank(message = "O CEP é obrigatório") String cep) {

        BigDecimal taxa = restauranteService.calcularTaxaEntrega(id, cep);
        ApiResponseWrapper<BigDecimal> response =
                new ApiResponseWrapper<>(true, taxa, "Taxa calculada com sucesso");

        return ResponseEntity.ok(response);
    }

    /**
     * Lista restaurantes próximos a um CEP, dentro de um raio (em km). Endpoint público.
     *
     * @param cep  O CEP de referência.
     * @param raio O raio de busca em quilômetros (padrão 10km).
     * @return ResponseEntity 200 (OK) com a lista de restaurantes próximos.
     */
    @GetMapping("/proximos/{cep}")
    @Operation(summary = "Restaurantes próximos (Público)",
               description = "Lista restaurantes próximos a um CEP, baseado em um raio (km).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurantes próximos encontrados"),
            @ApiResponse(responseCode = "400", description = "CEP inválido")
    })
    public ResponseEntity<ApiResponseWrapper<List<RestauranteResponseDTO>>> buscarProximos(
            @PathVariable @NotBlank(message = "O CEP é obrigatório") String cep,
            @RequestParam(defaultValue = "10") @Positive(message = "O raio deve ser positivo") Integer raio) {

        List<RestauranteResponseDTO> restaurantes = restauranteService.buscarRestaurantesProximos(cep, raio);
        ApiResponseWrapper<List<RestauranteResponseDTO>> response =
                new ApiResponseWrapper<>(true, restaurantes, "Restaurantes próximos encontrados");

        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os produtos (cardápio) de um restaurante específico. Endpoint público.
     *
     * @param restauranteId O ID do restaurante.
     * @param disponivel    (Opcional) Filtra produtos pelo status (true/false).
     * @return ResponseEntity 200 (OK) com a lista de produtos.
     */
    @GetMapping("/{restauranteId}/produtos")
    @Operation(summary = "Produtos do restaurante (Público)",
               description = "Lista todos os produtos (cardápio) de um restaurante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos encontrados"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarProdutosDoRestaurante(
            @PathVariable @Positive(message = "O ID deve ser positivo") Long restauranteId,
            @RequestParam(required = false) Boolean disponivel) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorRestaurante(restauranteId, disponivel);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Produtos encontrados");

        return ResponseEntity.ok(response);
    }
}