package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador responsável pelas operações relacionadas aos produtos.
 */
@RestController
@RequestMapping("/api/produtos")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Produtos", description = "Operações relacionadas aos produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // --------------------------------------------------------------------------
    // CRIAR PRODUTO - ADMIN OU RESTAURANTE
    // --------------------------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANTE')")
    @Operation(summary = "Cadastrar produto", description = "Cria um novo produto no sistema")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> cadastrar(
            @Parameter(description = "Dados do produto a ser cadastrado", required = true)
            @Valid @RequestBody ProdutoDTO dto) {

        ProdutoResponseDTO produto = produtoService.cadastrarProduto(dto);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --------------------------------------------------------------------------
    // BUSCAR PRODUTO POR ID - PÚBLICO
    // --------------------------------------------------------------------------
    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Retorna um produto específico pelo seu ID")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> buscarPorId(
            @Parameter(description = "ID do produto a ser consultado", required = true, example = "1")
            @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.buscarProdutoPorId(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Produto encontrado");
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // ATUALIZAR PRODUTO - ADMIN OU DONO
    // --------------------------------------------------------------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @produtoService.isOwner(#id)")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
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

    // --------------------------------------------------------------------------
    // REMOVER PRODUTO - ADMIN OU DONO
    // --------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @produtoService.isOwner(#id)")
    @Operation(summary = "Remover produto", description = "Remove um produto do sistema")
    public ResponseEntity<Void> remover(
            @Parameter(description = "ID do produto a ser removido", required = true, example = "1")
            @PathVariable Long id) {

        produtoService.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------------------
    // ALTERAR DISPONIBILIDADE - ADMIN OU DONO
    // --------------------------------------------------------------------------
    @PatchMapping("/{id}/disponibilidade")
    @PreAuthorize("hasRole('ADMIN') or @produtoService.isOwner(#id)")
    @Operation(summary = "Alterar disponibilidade", description = "Altera a disponibilidade de um produto")
    public ResponseEntity<ApiResponseWrapper<ProdutoResponseDTO>> alterarDisponibilidade(
            @Parameter(description = "ID do produto cuja disponibilidade será alterada", required = true, example = "1")
            @PathVariable Long id) {

        ProdutoResponseDTO produto = produtoService.alterarDisponibilidade(id);
        ApiResponseWrapper<ProdutoResponseDTO> response =
                new ApiResponseWrapper<>(true, produto, "Disponibilidade alterada com sucesso");
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // BUSCAR PRODUTOS POR CATEGORIA - PÚBLICO
    // --------------------------------------------------------------------------
    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Buscar produtos por categoria", description = "Retorna todos os produtos de uma determinada categoria")
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorCategoria(
            @Parameter(description = "Categoria dos produtos a serem buscados", required = true, example = "Bebidas")
            @PathVariable String categoria) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorCategoria(categoria);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Produtos encontrados");
        return ResponseEntity.ok(response);
    }

    // --------------------------------------------------------------------------
    // BUSCAR PRODUTOS POR NOME - PÚBLICO
    // --------------------------------------------------------------------------
    @GetMapping("/buscar")
    @Operation(summary = "Buscar produtos por nome", description = "Retorna produtos que correspondem ao nome informado")
    public ResponseEntity<ApiResponseWrapper<List<ProdutoResponseDTO>>> buscarPorNome(
            @Parameter(description = "Nome do produto a ser buscado", required = true, example = "Coca-Cola")
            @RequestParam String nome) {

        List<ProdutoResponseDTO> produtos = produtoService.buscarProdutosPorNome(nome);
        ApiResponseWrapper<List<ProdutoResponseDTO>> response =
                new ApiResponseWrapper<>(true, produtos, "Busca realizada com sucesso");
        return ResponseEntity.ok(response);
    }
}
