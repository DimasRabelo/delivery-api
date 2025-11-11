package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.request.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.deliverytech.delivery.security.jwt.SecurityUtils;
import com.deliverytech.delivery.service.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * IMPLEMENTAÇÃO DO SERVIÇO DE CLIENTES
 * ----------------------------------------------------------
 * Contém todas as operações relacionadas ao Cliente:
 * - Busca por ID ou email
 * - Atualização de perfil
 * - Ativação/Desativação
 * - Listagem de clientes ativos (simples e paginada)
 *
 * Observação:
 * O cadastro de Cliente (com Usuário e Endereço) agora é realizado
 * exclusivamente via AuthService.registrarCliente().
 */
@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    // ==========================================================
    // DEPENDÊNCIAS
    // ==========================================================
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    // ==========================================================
    // MÉTODOS DE CADASTRO (REMOVIDOS)
    // ----------------------------------------------------------
    // O cadastro agora é feito via AuthService.
    // ==========================================================
    @Override
    public ClienteResponseDTO cadastrarCliente(ClienteDTO dto) {
        throw new BusinessException("Método obsoleto. Use o endpoint de registro do AuthService.");
    }

    // ==========================================================
    // MÉTODOS DE BUSCA
    // ==========================================================
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));
        return mapToClienteResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorEmail(String email) {
        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com email: " + email));
        return mapToClienteResponse(cliente);
    }

    // ==========================================================
    // MÉTODOS DE ATUALIZAÇÃO
    // ==========================================================
    @Override
    public ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));

        // Validação de segurança: apenas o usuário dono do perfil pode atualizar
        Long usuarioLogadoId = SecurityUtils.getCurrentUserId();
        if (!cliente.getUsuario().getId().equals(usuarioLogadoId)) {
            throw new BusinessException("Acesso negado. Você só pode atualizar o seu próprio perfil.");
        }

        // Validação de CPF
        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
            throw new BusinessException("CPF é obrigatório.");
        }
        if (!cliente.getCpf().equals(dto.getCpf()) &&
                clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }

        // Atualiza apenas os campos do Cliente
        cliente.setNome(dto.getNome());
        cliente.setTelefone(dto.getTelefone());
        cliente.setCpf(dto.getCpf());

        return mapToClienteResponse(clienteRepository.save(cliente));
    }

    // ==========================================================
    // MÉTODOS DE ATIVAÇÃO/DESATIVAÇÃO
    // ==========================================================
    @Override
    public ClienteResponseDTO ativarDesativarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));

        Usuario usuario = cliente.getUsuario();
        if (usuario == null) {
            throw new EntityNotFoundException("Cliente não possui um usuário associado.");
        }

        // Alterna status do Usuário
        usuario.setAtivo(!usuario.getAtivo());
        usuarioRepository.save(usuario);

        return mapToClienteResponse(cliente);
    }

    // ==========================================================
    // MÉTODOS DE LISTAGEM
    // ==========================================================
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientesAtivos() {
        return clienteRepository.findByUsuarioAtivoTrue().stream()
                .map(this::mapToClienteResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listarClientesAtivosPaginado(Pageable pageable) {
        return clienteRepository.findByUsuarioAtivoTrue(pageable)
                .map(this::mapToClienteResponse);
    }

    // ==========================================================
    // MÉTODO AUXILIAR DE MAPEAMENTO
    // ----------------------------------------------------------
    // Converte Cliente -> ClienteResponseDTO, incluindo dados do Usuário.
    // ==========================================================
    private ClienteResponseDTO mapToClienteResponse(Cliente cliente) {
        ClienteResponseDTO dto = modelMapper.map(cliente, ClienteResponseDTO.class);

        if (cliente.getUsuario() != null) {
            dto.setEmail(cliente.getUsuario().getEmail());
            dto.setAtivo(cliente.getUsuario().getAtivo());
            dto.setDataCadastro(cliente.getUsuario().getDataCriacao());
        }

        
        return dto;
    }
}
