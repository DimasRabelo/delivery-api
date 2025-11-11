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
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

/**
 * IMPLEMENTAÇÃO DO SERVIÇO DE ENDEREÇOS
 * ----------------------------------------------------------
 * Contém todas as operações relacionadas a Endereço:
 * - Busca por usuário logado ou ID
 * - Salvamento de novo endereço
 * - "Soft delete" (inativação)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EnderecoServiceImpl implements EnderecoService {

    // ==========================================================
    // DEPENDÊNCIAS
    // ==========================================================
    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    // ==========================================================
    // MÉTODOS DE BUSCA
    // ==========================================================
    
    /**
     * Busca todos os endereços do usuário logado (ativos apenas)
     */
    @Override
    public List<Endereco> buscarPorUsuarioLogado() {
        Long usuarioId = SecurityUtils.getCurrentUserId();
        return enderecoRepository.findByUsuarioIdAndAtivoIsTrue(usuarioId);
    }

    /**
     * Busca todos os endereços de um usuário específico pelo ID
     */
    @Override
    public List<Endereco> buscarPorUsuarioId(Long usuarioId) {
        return enderecoRepository.findByUsuarioId(usuarioId);
    }

    // ==========================================================
    // MÉTODOS DE SALVAMENTO
    // ==========================================================
    
    /**
     * Salva um novo endereço para o usuário logado
     */
    @Override
    public Endereco salvarNovoEndereco(EnderecoDTO enderecoDTO) {
        Long usuarioId = SecurityUtils.getCurrentUserId();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário com ID " + usuarioId + " não encontrado."));

        Endereco novoEndereco = modelMapper.map(enderecoDTO, Endereco.class);
        novoEndereco.setUsuario(usuario);

        return enderecoRepository.save(novoEndereco);
    }

    // ==========================================================
    // MÉTODOS DE DELEÇÃO (SOFT DELETE)
    // ==========================================================
    
    /**
     * "Deleta" um endereço (Soft Delete)
     * Em vez de remover do banco, define 'ativo = false'.
     *
     * @param enderecoId ID do endereço a ser deletado
     * @param usuarioId  ID do usuário dono do endereço (validação)
     */
    @Override
    public void deletarEndereco(Long enderecoId, Long usuarioId) {
        // 1. Busca o endereço
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Endereço não encontrado com id: " + enderecoId));

        // 2. Valida o dono do endereço
        if (endereco.getUsuario() == null) {
            throw new IllegalStateException(
                    "Endereço " + enderecoId + " não possui um usuário associado.");
        }
        if (!endereco.getUsuario().getId().equals(usuarioId)) {
            throw new AccessDeniedException("Usuário não autorizado a deletar este endereço");
        }

        // 3. Soft delete: inativa o endereço
        endereco.setAtivo(false);

        // 4. Salva a alteração
        enderecoRepository.save(endereco);
    }
}
