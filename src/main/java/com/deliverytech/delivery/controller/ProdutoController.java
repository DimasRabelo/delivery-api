package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para operações de CRUD e consulta relacionadas a Produtos.
 *
 * Expõe endpoints públicos para consulta de cardápios e endpoints protegidos
 * para o gerenciamento de produtos por (ADMINs ou donos de restaurantes).
 */
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "4. Produtos", description = "Operações de consulta e gerenciamento de produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    /**
     * Cadastra um novo produto no sistema.
     * Acesso permitido para 'ADMIN' ou 'RESTAURANTE'.
     *
     * @param dto DTO contendo os dados do produto.
     * @return ResponseEntity 201 (Created) com os dados do produto criado.
     *
     * @implNote **Atenção de Segurança:** A regra `@PreAuthorize("hasRole('RESTAURANTE')")`
     * apenas verifica se o usuário é um restaurante, mas não se ele é o dono
     * do 'restauranteId' informado no DTO. É esperado que o
     * {@link ProdutoService#cadastrarProduto(ProdutoDTO)} contenha a lógica para
     * extrair o usuário logado e forçar o 'restauranteId' correto,
     * ignorando o que vier no DTO para evitar que um restaurante crie
     * produtos para outro.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANTE')")
    @Operation(summary = "Cadastrar produto (ADMIN ou Dono)",
               description = "Cria um novo produto. Requer permissão de ADMIN ou RESTAURANTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                         content = @Content(schema = @Schema(implementation = ProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> cadastrar(
            @Parameter(description = "Dados do produto a ser cadastrado", required = true)
            @Valid @RequestBody ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.cadastrarProduto(dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um produto específico pelo seu ID. Endpoint público.
     *
     * @param id ID do produto a ser consultado.
     * @return ResponseEntity 200 (OK) com os dados do produto.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID (Público)",
               description = "Retorna um produto específico pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                         content = @Content(schema = @Schema(implementation = ProdutoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do produto a ser consultado", required = true, example = "1")
            @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.buscarProdutoPorId(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto encontrado");
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um produto existente.
     * Acesso restrito a 'ADMIN' ou ao dono do restaurante ao qual o produto pertence.
     *
     * @param id  ID do produto a ser atualizado.
     * @param dto DTO com os novos dados do produto.
     * @return ResponseEntity 200 (OK) com os dados do produto atualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @produtoService.isOwner(#id)")
    @Operation(summary = "Atualizar produto (ADMIN ou Dono)",
               description = "Atualiza os dados de um produto. Requer permissão de ADMIN ou ser o dono do produto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou dono)"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> atualizar(
            @Parameter(description = "ID do produto a ser atualizado", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dados atualizados do produto", required = true)
            @Valid @RequestBody ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.atualizarProduto(id, dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto atualizado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Remove um produto do sistema (deleção lógica ou física).
     * Acesso restrito a 'ADMIN' ou ao dono do restaurante ao qual o produto pertence.
     *
     * @param id ID do produto a ser removido.
     * @return ResponseEntity 204 (No Content).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @produtoService.isOwner(#id)")
    @Operation(summary = "Remover produto (ADMIN ou Dono)",
               description = "Remove um produto. Requer permissão de ADMIN ou ser o dono do produto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou dono)"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> remover(
            @Parameter(description = "ID do produto a ser removido", required = true, example = "1")
            @PathVariable Long id) {

        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Altera a disponibilidade (ativo/inativo) de um produto.
     * Acesso restrito a 'ADMIN' ou ao dono do restaurante ao qual o produto pertence.
     *
     * @param id ID do produto a ter sua disponibilidade alterada.
     * @return ResponseEntity 200 (OK) com os dados do produto atualizado.
     */
    @PatchMapping("/{id}/disponibilidade")
    @PreAuthorize("hasRole('ADMIN') or @produtoService.isOwner(#id)")
    @Operation(summary = "Alterar disponibilidade (ADMIN ou Dono)",
               description = "Altera a disponibilidade (ativo/inativo) de um produto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilidade alterada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou dono)"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> alterarDisponibilidade(
            @Parameter(description = "ID do produto", required = true, example = "1")
            @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.alterarDisponibilidade(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Disponibilidade alterada com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Busca produtos por uma categoria específica. Endpoint público.
     *
     * @param categoria Categoria a ser buscada (ex: "Bebidas").
     * @return ResponseEntity 200 (OK) com a lista de produtos encontrados.
     */
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar produtos por categoria (Público)",
               description = "Retorna todos os produtos de uma determinada categoria.")
    @ApiResponse(responseCode = "200", description = "Produtos encontrados",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProdutoResponseDTO.class))))
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorCategoria(
            @Parameter(description = "Categoria dos produtos", required = true, example = "Bebidas")
            @PathVariable String categoria) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorCategoria(categoria);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Produtos encontrados");
        return ResponseEntity.ok(response);
    }

    /**
     * Busca produtos por nome (correspondência parcial). Endpoint público.
     *
     * @param nome Termo de busca para o nome do produto.
     * @return ResponseEntity 200 (OK) com a lista de produtos encontrados.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar produtos por nome (Público)",
               description = "Retorna produtos que correspondem ao nome informado (busca parcial).")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso",
                 content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProdutoResponseDTO.class))))
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorNome(
            @Parameter(description = "Nome do produto a ser buscado", required = true, example = "Coca-Cola")
            @RequestParam String nome) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorNome(nome);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Busca realizada com sucesso");
        return ResponseEntity.ok(response);
    }
}