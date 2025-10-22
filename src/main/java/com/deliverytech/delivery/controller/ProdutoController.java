package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador responsável pelas operações relacionadas aos produtos.
 * Inclui cadastro, atualização, exclusão e consultas com validação de dados.
 */
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@Validated // Habilita as validações do Bean Validation no controlador
@Tag(name = "Produtos", description = "Operações relacionadas aos produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    /**
     * Cadastra um novo produto no sistema.
     *
     * @param dto Dados do produto a ser cadastrado (validados automaticamente).
     * @return Resposta com status HTTP 201 e os dados do produto criado.
     */
    @PostMapping
    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> cadastrar(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do produto a ser criado")
            ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.cadastrarProduto(dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca um produto pelo seu ID.
     *
     * @param id Identificador do produto.
     * @return Produto encontrado ou erro 404 se não existir.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Recupera um produto específico pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.buscarProdutoPorId(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto encontrado");
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um produto existente.
     *
     * @param id  Identificador do produto a ser atualizado.
     * @param dto Dados atualizados do produto (validados automaticamente).
     * @return Produto atualizado e mensagem de sucesso.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> atualizar(
            @Parameter(description = "ID do produto") @PathVariable Long id,
            @Valid @RequestBody ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.atualizarProduto(id, dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto atualizado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Remove um produto do sistema.
     *
     * @param id Identificador do produto a ser removido.
     * @return Resposta HTTP 204 em caso de sucesso.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover produto", description = "Remove um produto do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
            @ApiResponse(responseCode = "409", description = "Produto possui pedidos associados")
    })
    public ResponseEntity<Void> remover(@Parameter(description = "ID do produto") @PathVariable Long id) {
        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Alterna o status de disponibilidade de um produto.
     *
     * @param id Identificador do produto.
     * @return Produto com o novo status de disponibilidade.
     */
    @PatchMapping("/{id}/disponibilidade")
    @Operation(summary = "Alterar disponibilidade", description = "Alterna a disponibilidade do produto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidade alterada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> alterarDisponibilidade(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.alterarDisponibilidade(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Disponibilidade alterada com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Busca produtos disponíveis de uma categoria específica.
     *
     * @param categoria Nome da categoria.
     * @return Lista de produtos encontrados.
     */
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar por categoria", description = "Lista produtos disponíveis de uma categoria específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produtos encontrados")
    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorCategoria(
            @Parameter(description = "Categoria do produto") @PathVariable String categoria) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorCategoria(categoria);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Produtos encontrados");
        return ResponseEntity.ok(response);
    }

    /**
     * Busca produtos disponíveis pelo nome informado.
     *
     * @param nome Parte ou nome completo do produto.
     * @return Lista de produtos que correspondem à pesquisa.
     */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nome", description = "Busca produtos disponíveis pelo nome")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorNome(
            @Parameter(description = "Nome do produto") @RequestParam String nome) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorNome(nome);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Busca realizada com sucesso");
        return ResponseEntity.ok(response);
    }
}
