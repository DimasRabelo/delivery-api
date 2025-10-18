package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.ProdutoDTO;
import com.deliverytech.delivery.dto.ProdutoResponseDTO;

import java.util.List;

public interface ProdutoService {

    ProdutoResponseDTO cadastrarProduto(ProdutoDTO dto);

    ProdutoResponseDTO buscarProdutoPorId(Long id);

    List<ProdutoResponseDTO> buscarProdutosPorNome(String nome);


    List<ProdutoResponseDTO> buscarProdutosPorRestaurante(Long restauranteId);

    ProdutoResponseDTO atualizarProduto(Long id, ProdutoDTO dto);

    ProdutoResponseDTO alterarDisponibilidade(Long id);

    List<ProdutoResponseDTO> buscarProdutosPorCategoria(String categoria);

    void removerProduto(Long id);
}
