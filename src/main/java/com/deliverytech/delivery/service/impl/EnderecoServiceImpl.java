package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.request.EnderecoDTO;
import com.deliverytech.delivery.entity.Endereco;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.EnderecoRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.EnderecoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper; // Verifique se este import está presente
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper; // Injetado com 'M'

    /**
     * Busca todos os endereços do usuário logado.
     */
    @Override
    public List<Endereco> buscarPorUsuarioLogado() {
        Long usuarioId = SecurityUtils.getCurrentUserId(); 
        return enderecoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Salva um novo endereço para o usuário logado.
     * @param enderecoDTO O DTO com os dados do novo endereço
     * @return A entidade Endereco que foi salva
     */
    @Override
    public Endereco salvarNovoEndereco(EnderecoDTO enderecoDTO) {
        Long usuarioId = SecurityUtils.getCurrentUserId();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com ID " + usuarioId + " não encontrado."));

        // Mapeia o DTO para a Entidade
        // CORREÇÃO: Usando modelMapper (com 'M')
        Endereco novoEndereco = modelMapper.map(enderecoDTO, Endereco.class); 
        
        // Associa o endereço ao usuário dono dele
        novoEndereco.setUsuario(usuario);

        return enderecoRepository.save(novoEndereco);
    }
}