package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.entity.Produto;
import com.deliverytech.delivery.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // Listar produtos disponíveis de um restaurante específico
    @GetMapping("/restaurante/{restauranteId}")
    public List<Produto> listarPorRestaurante(@PathVariable Long restauranteId) {
        return produtoService.listarPorRestaurante(restauranteId);
    }

    // Buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        Optional<Produto> produto = produtoService.buscarPorId(id);
        return produto.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Buscar produtos por categoria
    @GetMapping("/categoria/{categoria}")
    public List<Produto> buscarPorCategoria(@PathVariable String categoria) {
        return produtoService.buscarPorCategoria(categoria);
    }

    // Buscar produtos por faixa de preço
    @GetMapping("/faixa-preco")
    public List<Produto> buscarPorFaixaPreco(@RequestParam BigDecimal precoMin,
                                             @RequestParam BigDecimal precoMax) {
        return produtoService.buscarPorFaixaPreco(precoMin, precoMax);
    }

    // Criar novo produto associado a um restaurante
    @PostMapping("/restaurante/{restauranteId}")
    public Produto criar(@PathVariable Long restauranteId, @RequestBody Produto produto) {
        return produtoService.cadastrar(produto, restauranteId);
    }

    // Atualizar produto
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produtoDetalhes) {
        try {
            Produto produtoAtualizado = produtoService.atualizar(id, produtoDetalhes);
            return ResponseEntity.ok(produtoAtualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Alterar disponibilidade do produto
    @PatchMapping("/{id}/disponibilidade")
    public ResponseEntity<Void> alterarDisponibilidade(@PathVariable Long id,
                                                       @RequestParam boolean disponivel) {
        try {
            produtoService.alterarDisponibilidade(id, disponivel);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
