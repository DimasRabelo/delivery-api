package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.ClienteResponseDTO;
import java.util.List;

public interface ClienteService {

    ClienteResponseDTO cadastrarCliente(ClienteDTO dto);

    ClienteResponseDTO buscarClientePorId(Long id);

    ClienteResponseDTO buscarClientePorEmail(String email);

    ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto);

    ClienteResponseDTO ativarDesativarCliente(Long id);

    List<ClienteResponseDTO> listarClientesAtivos();
}
