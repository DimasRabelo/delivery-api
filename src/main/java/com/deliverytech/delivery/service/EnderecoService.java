package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Endereco;

import java.util.List;

/**
 * Interface de serviço para gerenciamento de Endereços.
 * Contém operações de busca, criação e exclusão de endereços de usuários.
 */
public interface EnderecoService {

    // ==========================================================
    // --- BUSCAS ---
    // ==========================================================
    /**
     * Busca todos os endereços associados ao usuário logado.
     * @return Lista de Endereco
     */
    List<Endereco> buscarPorUsuarioLogado();

    /**
     * Busca todos os endereços de um usuário específico pelo ID.
     * @param usuarioId ID do usuário
     * @return Lista de Endereco
     */
    List<Endereco> buscarPorUsuarioId(Long usuarioId);

    // ==========================================================
    // --- CRIAÇÃO / SALVAMENTO ---
    // ==========================================================
    /**
     * Salva um novo endereço para o usuário logado.
     * @param enderecoDTO DTO contendo os dados do endereço
     * @return Entidade Endereco que foi persistida
     */
    Endereco salvarNovoEndereco(EnderecoDTO enderecoDTO);

    // ==========================================================
    // --- EXCLUSÃO ---
    // ==========================================================
    /**
     * Deleta um endereço específico de um usuário.
     * @param enderecoId ID do endereço a ser removido
     * @param usuarioId ID do usuário dono do endereço
     */
    void deletarEndereco(Long enderecoId, Long usuarioId);
}
