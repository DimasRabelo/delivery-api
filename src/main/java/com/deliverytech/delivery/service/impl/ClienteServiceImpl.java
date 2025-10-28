package com.deliverytech.delivery.service.impl;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.entity.Cliente;
import com.deliverytech.delivery.exception.BusinessException;
import com.deliverytech.delivery.repository.ClienteRepository;
import com.deliverytech.delivery.service.ClienteService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ClienteResponseDTO cadastrarCliente(ClienteDTO dto) {
        // Validar email único (existente)
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + dto.getEmail());
        }

        // ==============================================
        // ADICIONADO: Validação de CPF (do DTO)
        // ==============================================
        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
             throw new BusinessException("CPF é obrigatório."); // Garante que o DTO tem
        }
        if (clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }
        // ==============================================

        // Converter DTO para entidade
        Cliente cliente = modelMapper.map(dto, Cliente.class);
        cliente.setAtivo(true); 
        // O ModelMapper vai mapear o 'cpf' do DTO para a entidade automaticamente

        // Salvar cliente
        Cliente clienteSalvo = clienteRepository.save(cliente);

        // Retornar DTO de resposta
        return modelMapper.map(clienteSalvo, ClienteResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado com ID: " + id));

        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarClientePorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado com email: " + email));

        return modelMapper.map(cliente, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado com ID: " + id));

        // Validar email único (se mudou)
        if (!cliente.getEmail().equals(dto.getEmail()) &&
                clienteRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Email já cadastrado: " + dto.getEmail());
        }

        // ==============================================
        // ADICIONADO: Validação de CPF (do DTO)
        // ==============================================
        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
            throw new BusinessException("CPF é obrigatório.");
        }
        // Validar CPF único (se mudou)
        if (!cliente.getCpf().equals(dto.getCpf()) &&
                clienteRepository.existsByCpf(dto.getCpf())) {
            throw new BusinessException("CPF já cadastrado: " + dto.getCpf());
        }
        // ==============================================

        // Atualizar dados
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setEndereco(dto.getEndereco());
        cliente.setCpf(dto.getCpf()); // <-- ADICIONADO: Atualização do CPF

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return modelMapper.map(clienteAtualizado, ClienteResponseDTO.class);
    }

    @Override
    public ClienteResponseDTO ativarDesativarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cliente não encontrado com ID: " + id));

        cliente.setAtivo(!cliente.isAtivo());
        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return modelMapper.map(clienteAtualizado, ClienteResponseDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarClientesAtivos() {
        List<Cliente> clientesAtivos = clienteRepository.findByAtivoTrue();

        return clientesAtivos.stream()
                .map(cliente -> modelMapper.map(cliente, ClienteResponseDTO.class))
                .collect(Collectors.toList());
    }
}