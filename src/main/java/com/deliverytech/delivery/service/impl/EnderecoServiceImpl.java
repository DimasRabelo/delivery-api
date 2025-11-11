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
// --- 1. IMPORTAR A EXCEÇÃO DE ACESSO NEGADO ---
import org.springframework.security.access.AccessDeniedException; 

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnderecoServiceImpl implements EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    /**
     * Busca todos os endereços do usuário logado.
     */
    @Override
    public List<Endereco> buscarPorUsuarioLogado() {
        Long usuarioId = SecurityUtils.getCurrentUserId(); 
        
        // --- MUDANÇA AQUI ---
        // Chamamos o novo método do repositório que filtra por "ativo = true"
        return enderecoRepository.findByUsuarioIdAndAtivoIsTrue(usuarioId);
    }

    /**
     * "Deleta" um endereço (Soft Delete)
     * Em vez de DELETAR, ele define "ativo = false" e SALVA.
     */
    /**
     * Busca todos os endereços de um usuário específico pelo ID.
     */
    @Override
    public List<Endereco> buscarPorUsuarioId(Long usuarioId) {
        return enderecoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Salva um novo endereço para o usuário logado.
     */
    @Override
    public Endereco salvarNovoEndereco(EnderecoDTO enderecoDTO) {
        Long usuarioId = SecurityUtils.getCurrentUserId();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário com ID " + usuarioId + " não encontrado."));

        Endereco novoEndereco = modelMapper.map(enderecoDTO, Endereco.class); 
        
        novoEndereco.setUsuario(usuario);

        return enderecoRepository.save(novoEndereco);
    }

    @Override
    @Transactional 
    public void deletarEndereco(Long enderecoId, Long usuarioId) {
        
        // 1. Busca o endereço (continua igual)
        Endereco endereco = enderecoRepository.findById(enderecoId)
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com id: " + enderecoId));

        // 2. Valida o dono (continua igual)
        if (endereco.getUsuario() == null) {
            throw new IllegalStateException("Endereço " + enderecoId + " não possui um usuário associado.");
        }
        if (!endereco.getUsuario().getId().equals(usuarioId)) {
            throw new AccessDeniedException("Usuário não autorizado a deletar este endereço");
        }

        // --- MUDANÇA DE LÓGICA AQUI ---
        // 3. Em vez de deletar, nós "inativamos"
        endereco.setAtivo(false);
        
        // 4. E salvamos a mudança
        enderecoRepository.save(endereco);
        
        // (A linha 'enderecoRepository.delete(endereco);' é removida)
}   
};