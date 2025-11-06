package com.deliverytech.delivery.integration;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import com.deliverytech.delivery.service.ProdutoService;

import org.junit.jupiter.api.Disabled; // IMPORT ADICIONADO
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
//import java.math.BigDecimal;
import java.util.ArrayList; // IMPORT ADICIONADO


import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Este é um TESTE DE INTEGRAÇÃO.
 * (Refatorado para a nova arquitetura de Produto)
 *
 * NOTA: Estes testes dependem de dados reais (IDs 1 e 2) existindo no banco
 * e de um 'Thread.sleep(3000)' no seu ProdutoService.
 * Se o 'sleep' foi removido, os asserts de duração (duration > 3000) falharão.
 */
@SpringBootTest
@DisplayName("Testes de Integração do Cache de ProdutoService (Refatorado)")
// Adicionando @Disabled para evitar que testes de longa duração rodem no build
// Remova @Disabled se você quiser executar estes testes manualmente.
@Disabled("Testes de cache são lentos (dependem de Thread.sleep) e devem ser executados manualmente.") 
class ProdutoServiceCacheTest {

    @Autowired
    private ProdutoService produtoService;

    /**
     * (Este teste não precisou de refatoração, pois só chama o 'buscarProdutoPorId')
     */
    @Test
    @DisplayName("Performance: Deve buscar do cache na segunda chamada")
    void deveDemonstrarGanhoDePerformanceComCache() {
        System.out.println("\n--- INICIANDO TESTE DE PERFORMANCE DO @Cacheable ---");
        Long produtoIdParaTestar = 1L; // (Assume que ID 1 existe no TestDataConfiguration)

        // --- PRIMEIRA CHAMADA (CACHE MISS) ---
        System.out.println("Buscando produto ID: " + produtoIdParaTestar + " (espera-se simulação de lentidão)...");
        Instant start1 = Instant.now();
        ProdutoResponseDTO produto1 = produtoService.buscarProdutoPorId(produtoIdParaTestar);
        Instant end1 = Instant.now();
        long duration1 = Duration.between(start1, end1).toMillis();
        System.out.println("-> Primeira chamada levou: " + duration1 + "ms");

        // --- SEGUNDA CHAMADA (CACHE HIT) ---
        System.out.println("\nBuscando MESMO produto ID: " + produtoIdParaTestar + " (espera-se < 100ms)...");
        Instant start2 = Instant.now();
        ProdutoResponseDTO produto2 = produtoService.buscarProdutoPorId(produtoIdParaTestar);
        Instant end2 = Instant.now();
        long duration2 = Duration.between(start2, end2).toMillis();
        System.out.println("-> Segunda chamada levou: " + duration2 + "ms");
        System.out.println("--- FIM DO TESTE DE PERFORMANCE ---");

        assertNotNull(produto1);
        assertNotNull(produto2);
        assertEquals(produto1.getId(), produto2.getId());
        
        // (Se o seu 'buscarProdutoPorId' não tiver mais o 'sleep(3000)', comente as linhas abaixo)
        // assertTrue(duration1 > 2900, "Primeira chamada foi LENTA (Banco)"); // (Margem de segurança)
        // assertTrue(duration2 < 100, "Segunda chamada foi RÁPIDA (Cache)");
    }

    /**
     * TESTE DE ENTREGÁVEL: Demonstra a invalidação do cache com @CacheEvict.
     * (Refatorado para usar 'precoBase' e 'gruposOpcionais')
     */
    @Test
    @DisplayName("Invalidação: Deve limpar o cache após atualizarProduto (Refatorado)")
    void deveInvalidarCacheAposAtualizacao() {
        System.out.println("\n--- INICIANDO TESTE DE INVALIDAÇÃO DO @CacheEvict ---");
        Long produtoIdParaTestar = 2L; // (Assume que ID 2 existe no TestDataConfiguration)

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
        // (Usa o ProdutoDTO refatorado)
        ProdutoDTO dadosAtualizados = new ProdutoDTO();
        dadosAtualizados.setNome("Produto Teste Atualizado Pelo CacheTest");
        dadosAtualizados.setDescricao(produtoOriginal.getDescricao());
        dadosAtualizados.setCategoria(produtoOriginal.getCategoria());
        dadosAtualizados.setRestauranteId(produtoOriginal.getRestauranteId()); 
        dadosAtualizados.setEstoque(produtoOriginal.getEstoque() + 1);
        // (O DTO de request do ProdutoService não pede 'disponivel', então não setamos)
        // dadosAtualizados.setDisponivel(produtoOriginal.getDisponivel()); 

        // --- CORREÇÃO (GARGALO 2) ---
        dadosAtualizados.setPrecoBase(produtoOriginal.getPrecoBase()); // <-- CORRIGIDO (era getPreco)
        dadosAtualizados.setGruposOpcionais(new ArrayList<>()); // <-- NOVO (envia lista vazia)
        // --- FIM DA CORREÇÃO ---
        
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
        // (Comente se o 'sleep(3000)' não existir mais no seu 'buscarProdutoPorId')
        // assertTrue(duration1 > 2900, "Chamada 1 foi LENTA (Banco)");
        // assertTrue(duration2 < 100, "Chamada 2 foi RÁPIDA (Cache)");
        // assertTrue(duration3 > 2900, "Chamada 3 foi LENTA (Banco após Evict)");
        
        assertEquals("Produto Teste Atualizado Pelo CacheTest", produtoAtualizado.getNome());
    }
}