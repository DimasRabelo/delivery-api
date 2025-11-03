package com.deliverytech.delivery.integration;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import java.math.BigDecimal;


import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Este é um TESTE DE INTEGRAÇÃO.
 * A anotação @SpringBootTest faz o Spring carregar a aplicação completa,
 * incluindo o CacheManager e as anotações @Cacheable.
 * É por isso que podemos testar o EFEITO do cache aqui.
 */
@SpringBootTest
@DisplayName("Testes de Integração do Cache de ProdutoService")
class ProdutoServiceCacheTest {

    // Injetamos o serviço REAL, gerenciado pelo Spring (não um @Mock)
    @Autowired
    private ProdutoService produtoService;

    /**
     * TESTE DE ENTREGÁVEL: Demonstra o ganho de performance do @Cacheable.
     * 1. A primeira chamada deve ser LENTA (acessa o banco, simulação de 3s).
     * 2. A segunda chamada (para o MESMO ID) deve ser IMEDIATA (acessa o cache).
     */
    @Test
    @DisplayName("Performance: Deve buscar do cache na segunda chamada")
    void deveDemonstrarGanhoDePerformanceComCache() {
        System.out.println("\n--- INICIANDO TESTE DE PERFORMANCE DO @Cacheable ---");
        // ATENÇÃO: Use um ID que você SABE que existe no seu banco (H2, data.sql)
        // Se não souber, use 1L e torça, ou crie um dado antes.
        // Vamos usar 1L para o exemplo.
        Long produtoIdParaTestar = 1L; 

        // --- PRIMEIRA CHAMADA (CACHE MISS) ---
        // Esperamos ver o log "CONSULTANDO BANCO DE DADOS..."
        System.out.println("Buscando produto ID: " + produtoIdParaTestar + " (espera-se 3s de simulação)...");
        Instant start1 = Instant.now();
        ProdutoResponseDTO produto1 = produtoService.buscarProdutoPorId(produtoIdParaTestar);
        Instant end1 = Instant.now();
        long duration1 = Duration.between(start1, end1).toMillis();

        System.out.println("-> Primeira chamada levou: " + duration1 + "ms");

        // --- SEGUNDA CHAMADA (CACHE HIT) ---
        // NÃO esperamos ver o log do banco
        System.out.println("\nBuscando MESMO produto ID: " + produtoIdParaTestar + " (espera-se < 50ms)...");
        Instant start2 = Instant.now();
        ProdutoResponseDTO produto2 = produtoService.buscarProdutoPorId(produtoIdParaTestar);
        Instant end2 = Instant.now();
        long duration2 = Duration.between(start2, end2).toMillis();

        System.out.println("-> Segunda chamada levou: " + duration2 + "ms");
        System.out.println("--- FIM DO TESTE DE PERFORMANCE ---");

        // Asserts para garantir que funcionou
        assertNotNull(produto1);
        assertNotNull(produto2);
        assertEquals(produto1.getId(), produto2.getId()); // Verifica se os produtos são os mesmos
        
        // A prova final (o entregável!)
        assertTrue(duration1 > 3000, "Primeira chamada foi LENTA (Banco)");
        assertTrue(duration2 < 100, "Segunda chamada foi RÁPIDA (Cache)");
    }

    /**
     * TESTE DE ENTREGÁVEL: Demonstra a invalidação do cache com @CacheEvict.
     * 1. Busca LENTA (salva no cache).
     * 2. Busca RÁPIDA (confirma que está no cache).
     * 3. ATUALIZA o produto (limpa o cache).
     * 4. Busca LENTA novamente (prova que o cache foi limpo).
     */
    @Test
    @DisplayName("Invalidação: Deve limpar o cache após atualizarProduto")
    void deveInvalidarCacheAposAtualizacao() {
        System.out.println("\n--- INICIANDO TESTE DE INVALIDAÇÃO DO @CacheEvict ---");
        // Use um ID DIFERENTE do teste anterior para evitar interferência
        Long produtoIdParaTestar = 2L; 

        // --- 1. PRIMEIRA CHAMADA (CACHE MISS) ---
        System.out.println("1. Buscando produto ID: " + produtoIdParaTestar + " (deve ser LENTO)...");
        Instant start1 = Instant.now();
        ProdutoResponseDTO produtoOriginal = produtoService.buscarProdutoPorId(produtoIdParaTestar);
        long duration1 = Duration.between(start1, Instant.now()).toMillis();
        System.out.println("-> Levou: " + duration1 + "ms");

        // --- 2. SEGUNDA CHAMADA (CACHE HIT) ---
        System.out.println("\n2. Buscando MESMO produto ID: " + produtoIdParaTestar + " (deve ser RÁPIDO)...");
        Instant start2 = Instant.now();
        produtoService.buscarProdutoPorId(produtoIdParaTestar);
        long duration2 = Duration.between(start2, Instant.now()).toMillis();
        System.out.println("-> Levou: " + duration2 + "ms");

        // --- 3. ATUALIZAÇÃO (CACHE EVICT) ---
        // Precisamos criar um DTO para simular a atualização
        ProdutoDTO dadosAtualizados = new ProdutoDTO();
        dadosAtualizados.setNome("Produto Teste Atualizado Pelo CacheTest");
        dadosAtualizados.setDescricao(produtoOriginal.getDescricao());
        dadosAtualizados.setPreco(produtoOriginal.getPreco());
        dadosAtualizados.setCategoria(produtoOriginal.getCategoria());
        dadosAtualizados.setRestauranteId(produtoOriginal.getRestauranteId()); // MUITO IMPORTANTE
        dadosAtualizados.setEstoque(produtoOriginal.getEstoque() + 1);
        dadosAtualizados.setDisponivel(produtoOriginal.getDisponivel());

        System.out.println("\n3. Atualizando produto ID: " + produtoIdParaTestar + " (deve invalidar o cache)...");
        produtoService.atualizarProduto(produtoIdParaTestar, dadosAtualizados);
        System.out.println("-> Log de @CacheEvict deve ter aparecido acima.");

        // --- 4. TERCEIRA CHAMADA (CACHE MISS NOVAMENTE) ---
        System.out.println("\n4. Buscando produto ID: " + produtoIdParaTestar + " PÓS-ATUALIZAÇÃO (deve ser LENTO de novo)...");
        Instant start3 = Instant.now();
        ProdutoResponseDTO produtoAtualizado = produtoService.buscarProdutoPorId(produtoIdParaTestar);
        long duration3 = Duration.between(start3, Instant.now()).toMillis();
        System.out.println("-> Levou: " + duration3 + "ms");
        System.out.println("--- FIM DO TESTE DE INVALIDAÇÃO ---");

        // Asserts
        assertTrue(duration1 > 3000, "Chamada 1 foi LENTA (Banco)");
        assertTrue(duration2 < 100, "Chamada 2 foi RÁPIDA (Cache)");
        assertTrue(duration3 > 3000, "Chamada 3 foi LENTA (Banco após Evict)");
        // Verifica se a atualização realmente aconteceu
        assertEquals("Produto Teste Atualizado Pelo CacheTest", produtoAtualizado.getNome());
    }
}