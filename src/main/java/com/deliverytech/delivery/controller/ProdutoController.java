package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.*;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@Tag(name = "Produtos", description = "Operações relacionadas aos produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // Método para cadastrar um novo produto
    @PostMapping
    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto no sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Produto criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Restaurante não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> cadastrar(
            @Valid @RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do produto a ser criado") ProdutoDTO dto) {

        // Chama o serviço para cadastrar o produto
        ProdutoResponseDTO produto = produtoService.cadastrarProduto(dto);
        // Envolve a resposta em um wrapper com mensagem de sucesso
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Método para buscar um produto pelo ID
    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Recupera um produto específico pelo ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        // Chama o serviço para buscar o produto pelo ID
        ProdutoResponseDTO produto = produtoService.buscarProdutoPorId(id);
        // Envolve a resposta em um wrapper com mensagem de sucesso
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto encontrado");
        return ResponseEntity.ok(response);
    }

    // Método para atualizar os dados de um produto existente
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

        // Chama o serviço para atualizar o produto
        ProdutoResponseDTO produto = produtoService.atualizarProduto(id, dto);
        // Envolve a resposta em um wrapper com mensagem de sucesso
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto atualizado com sucesso");
        return ResponseEntity.ok(response);
    }

    // Método para remover um produto
    @DeleteMapping("/{id}")
    @Operation(summary = "Remover produto", description = "Remove um produto do sistema")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Produto removido com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado"),
        @ApiResponse(responseCode = "409", description = "Produto possui pedidos associados")
    })
    public ResponseEntity<Void> remover(@Parameter(description = "ID do produto") @PathVariable Long id) {
        // Chama o serviço para remover o produto
        produtoService.removerProduto(id);
        // Retorna HTTP 204 (No Content) indicando remoção bem-sucedida
        return ResponseEntity.noContent().build();
    }

    // Método para alterar a disponibilidade de um produto
    @PatchMapping("/{id}/disponibilidade")
    @Operation(summary = "Alterar disponibilidade", description = "Alterna a disponibilidade do produto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Disponibilidade alterada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> alterarDisponibilidade(
            @Parameter(description = "ID do produto") @PathVariable Long id) {

        // Chama o serviço para alternar a disponibilidade do produto
        ProdutoResponseDTO produto = produtoService.alterarDisponibilidade(id);
        // Envolve a resposta em um wrapper com mensagem de sucesso
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Disponibilidade alterada com sucesso");
        return ResponseEntity.ok(response);
    }

    // Método para buscar produtos por categoria
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar por categoria", description = "Lista produtos disponíveis de uma categoria específica")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Produtos encontrados")
    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorCategoria(
            @Parameter(description = "Categoria do produto") @PathVariable String categoria) {

        // Chama o serviço para buscar produtos da categoria informada
        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorCategoria(categoria);
        // Envolve a lista em um wrapper com mensagem de sucesso
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Produtos encontrados");
        return ResponseEntity.ok(response);
    }

    // Método para buscar produtos pelo nome
    @GetMapping("/buscar")
    @Operation(summary = "Buscar por nome", description = "Busca produtos disponíveis pelo nome")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    })
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorNome(
            @Parameter(description = "Nome do produto") @RequestParam String nome) {

        // Chama o serviço para buscar produtos pelo nome informado
        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorNome(nome);
        // Envolve a lista de produtos em um wrapper com mensagem de sucesso
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Busca realizada com sucesso");
        return ResponseEntity.ok(response);
    }
}
