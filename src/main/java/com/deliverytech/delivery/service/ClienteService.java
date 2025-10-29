package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClienteService {

    ClienteResponseDTO cadastrarCliente(ClienteDTO dto);

    ClienteResponseDTO buscarClientePorId(Long id);

    ClienteResponseDTO buscarClientePorEmail(String email);

    ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto);

    ClienteResponseDTO ativarDesativarCliente(Long id);

    List<ClienteResponseDTO> listarClientesAtivos();

    // 🔹 Novo método paginado
    Page<ClienteResponseDTO> listarClientesAtivosPaginado(Pageable pageable);
}
