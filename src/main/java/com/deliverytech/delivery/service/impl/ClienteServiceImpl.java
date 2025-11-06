package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.entity.Usuario; // IMPORT ADICIONADO
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.exception.EntityNotFoundException;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.repository.auth.UsuarioRepository; // IMPORT ADICIONADO
import com.deliverytech.delivery.security.jwt.SecurityUtils; // IMPORT ADICIONADO
import com.deliverytech.delivery.service.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // <-- NECESSÁRIO

    @Autowired
    private ModelMapper modelMapper;

    /**
     * MÉTODO REMOVIDO.
     * O cadastro de cliente (que cria Usuario + Cliente + Endereco)
     * agora é feito pelo AuthService.registrarCliente(RegisterRequest dto).
     */
    @Override
    public ClienteResponseDTO cadastrarCliente(ClienteDTO dto) {
        throw new BusinessException("Método obsoleto. Use o endpoint de registro do AuthService.");
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));
        return mapToClienteResponse(cliente);
    }

    /**
     * Busca um cliente pelo email (CORRIGIDO).
     * Agora busca através do relacionamento com o Usuário.
     */
    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorEmail(String email) {
        // Usa o novo método do repositório
        Cliente cliente = clienteRepository.findByUsuarioEmail(email) 
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com email: " + email));
        return mapToClienteResponse(cliente);
    }

    /**
     * Atualiza o perfil de um cliente (CORRIGIDO).
     * Agora atualiza apenas os dados do CLIENTE (nome, cpf, telefone).
     * O 'email' é atualizado via UsuarioService.
     * O 'endereco' é atualizado via EnderecoService (a ser criado).
     */
    @Override
    public ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto) {
        // 'id' aqui é o ID do Cliente (que é o mesmo ID do Usuário)
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));
        
        // (Validação de segurança: o usuário logado é o dono deste perfil?)
        Long usuarioLogadoId = SecurityUtils.getCurrentUserId();
        if (!cliente.getUsuario().getId().equals(usuarioLogadoId)) {
             throw new BusinessException("Acesso negado. Você só pode atualizar o seu próprio perfil.");
        }

        // Validação de duplicidade de CPF (OK)
        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
            throw new BusinessException("CPF é obrigatório.");
        }
        if (!cliente.getCpf().equals(dto.getCpf()) &&
                clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }

        // --- ATUALIZAÇÃO CORRIGIDA ---
        // Atualiza apenas os campos que pertencem ao Cliente
        cliente.setNome(dto.getNome());
        cliente.setTelefone(dto.getTelefone());
        cliente.setCpf(dto.getCpf());
        
        // (ERROS REMOVIDOS)
        // cliente.setEmail(dto.getEmail()); 
        // cliente.setEndereco(dto.getEndereco());

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return mapToClienteResponse(clienteAtualizado);
    }

    /**
     * Ativa ou desativa um cliente (CORRIGIDO).
     * A lógica de 'ativo' agora está na entidade Usuario.
     */
    @Override
    public ClienteResponseDTO ativarDesativarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com ID: " + id));

        // Pega o Usuário associado
        Usuario usuario = cliente.getUsuario();
        if (usuario == null) {
             throw new EntityNotFoundException("Cliente não possui um usuário associado.");
        }

        // (ERRO CORRIGIDO)
        // Altera o status no Usuário, não no Cliente
        usuario.setAtivo(!usuario.getAtivo());
        usuarioRepository.save(usuario); // Salva a entidade Usuario

        return mapToClienteResponse(cliente);
    }

    /**
     * Lista clientes ativos (CORRIGIDO).
     * Usa o novo método de busca.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientesAtivos() {
        List<Cliente> clientesAtivos = clienteRepository.findByUsuarioAtivoTrue();
        return clientesAtivos.stream()
                .map(this::mapToClienteResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista clientes ativos paginados (CORRIGIDO).
     * Usa o novo método de busca.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listarClientesAtivosPaginado(Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findByUsuarioAtivoTrue(pageable);
        return clientes.map(this::mapToClienteResponse);
    }

    /**
     * Método helper para mapear Cliente -> ClienteResponseDTO (CORRIGIDO).
     * Agora busca os dados (email, ativo) no Usuário associado.
     */
    private ClienteResponseDTO mapToClienteResponse(Cliente cliente) {
        // Mapeia os campos básicos (id, nome, cpf, telefone)
        ClienteResponseDTO dto = modelMapper.map(cliente, ClienteResponseDTO.class);
        
        // Busca os dados do Usuário associado
        if (cliente.getUsuario() != null) {
            dto.setEmail(cliente.getUsuario().getEmail());
            dto.setAtivo(cliente.getUsuario().getAtivo());
            dto.setDataCadastro(cliente.getUsuario().getDataCriacao());
        }
        
        // (O campo 'endereco' (String) não existe mais. 
        // O DTO de resposta precisa ser ajustado ou este campo ficará nulo)
        // dto.setEndereco(???); 
        
        return dto;
    }
}