package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Endereco;
import java.util.List;

public interface EnderecoService {

    /**
     * Busca todos os endereços do usuário logado.
     */
    List<Endereco> buscarPorUsuarioLogado();

    // ==========================================================
    // <-- AQUI ESTÁ A CORREÇÃO (MÉTODO ADICIONADO)
    // ==========================================================
    /**
     * Busca todos os endereços de um usuário específico pelo ID.
     * @param usuarioId O ID do usuário
     * @return A lista de endereços
     */
    List<Endereco> buscarPorUsuarioId(Long usuarioId);
    // ==========================================================


    /**
     * Salva um novo endereço para o usuário logado.
     * @param enderecoDTO O DTO com os dados do novo endereço
     * @return A entidade Endereco que foi salva
     */
    Endereco salvarNovoEndereco(EnderecoDTO enderecoDTO);

    public void deletarEndereco(Long enderecoId, Long usuarioId);
}