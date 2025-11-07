package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Endereco;
import java.util.List;

public interface EnderecoService {

    /**
     * Busca todos os endereços do usuário logado.
     */
    List<Endereco> buscarPorUsuarioLogado();

    /**
     * Salva um novo endereço para o usuário logado.
     * @param enderecoDTO O DTO com os dados do novo endereço
     * @return A entidade Endereco que foi salva
     */
    Endereco salvarNovoEndereco(EnderecoDTO enderecoDTO);
}