package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.ProdutoDTO;
import com.deliverytech.delivery.dto.response.ProdutoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface de serviço para gerenciamento de Produtos.
 * Contém operações de criação, consulta, atualização, remoção e validação de propriedade.
 */
public interface ProdutoService {

    // ==========================================================
    // --- CRIAÇÃO DE PRODUTO ---
    // ==========================================================
    /**
     * Cadastra um novo produto no sistema.
     * @param dto DTO com informações do produto
     * @return ProdutoResponseDTO com os dados cadastrados
     */
    ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto);

    // ==========================================================
    // --- CONSULTAS ---
    // ==========================================================
    /**
     * Busca um produto pelo seu ID.
     * @param id ID do produto
     * @return ProdutoResponseDTO
     */
    ProdutoResponseDTO buscarProdutoPorId(Long id);

    /**
     * Busca produtos pelo nome (parcial, case-insensitive).
     * @param nome Nome ou parte do nome
     * @return Lista de ProdutoResponseDTO
     */
    List<ProdutoResponseDTO> buscarProdutosPorNome(String nome);

    /**
     * Busca produtos por categoria.
     * @param categoria Categoria do produto
     * @return Lista de ProdutoResponseDTO
     */
    List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria);

    /**
     * Busca produtos de um restaurante filtrando por disponibilidade.
     * @param restauranteId ID do restaurante
     * @param disponivel Se o produto deve estar disponível
     * @return Lista de ProdutoResponseDTO
     */
    List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId, Boolean disponivel);

    /**
     * Lista produtos com paginação e filtros opcionais.
     * @param pageable Configuração de paginação
     * @param restauranteId ID do restaurante (opcional)
     * @param categoria Categoria do produto (opcional)
     * @param disponivel Filtrar apenas produtos disponíveis (opcional)
     * @return Página de ProdutoResponseDTO
     */
    Page<ProdutoResponseDTO> listarProdutos(Pageable pageable, Long restauranteId, String categoria, Boolean disponivel);

    // ==========================================================
    // --- ATUALIZAÇÃO / ALTERAÇÃO ---
    // ==========================================================
    /**
     * Atualiza os dados de um produto existente.
     * @param id ID do produto
     * @param dto DTO com novos dados
     * @return ProdutoResponseDTO atualizado
     */
    ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto);

    /**
     * Altera a disponibilidade de um produto (ativo/inativo).
     * @param id ID do produto
     * @return ProdutoResponseDTO atualizado
     */
    ProdutoResponseDTO alterarDisponibilidade(Long id);

    // ==========================================================
    // --- REMOÇÃO ---
    // ==========================================================
    /**
     * Remove um produto do sistema.
     * @param id ID do produto
     */
    void removerProduto(Long id);

    // ==========================================================
    // --- AUTORIZAÇÃO / PROPRIEDADE ---
    // ==========================================================
    /**
     * Verifica se o usuário logado é proprietário do produto.
     * @param produtoId ID do produto
     * @return true se é dono, false caso contrário
     */
    boolean isOwner(Long produtoId);
}
