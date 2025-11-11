package com.deliverytech.delivery.service;

import com.deliverytech.delivery.dto.request.ClienteDTO;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface de serviço para gerenciamento de Clientes.
 * Contém as operações principais de CRUD, ativação/desativação e listagem (simples e paginada).
 */
public interface ClienteService {

    // ==========================================================
    // --- CADASTRO ---
    // ==========================================================
    /**
     * Cadastra um novo cliente no sistema.
     * @param dto DTO contendo dados do cliente a ser cadastrado.
     * @return ClienteResponseDTO com os dados do cliente cadastrado.
     */
    ClienteResponseDTO cadastrarCliente(ClienteDTO dto);

    // ==========================================================
    // --- BUSCAS ---
    // ==========================================================
    /**
     * Busca um cliente pelo seu ID.
     * @param id Identificador único do cliente.
     * @return ClienteResponseDTO com os dados do cliente encontrado.
     */
    ClienteResponseDTO buscarClientePorId(Long id);

    /**
     * Busca um cliente pelo email do usuário associado.
     * @param email Email do usuário.
     * @return ClienteResponseDTO com os dados do cliente encontrado.
     */
    ClienteResponseDTO buscarClientePorEmail(String email);

    // ==========================================================
    // --- ATUALIZAÇÃO ---
    // ==========================================================
    /**
     * Atualiza os dados de um cliente existente.
     * @param id ID do cliente a ser atualizado.
     * @param dto DTO com os novos dados.
     * @return ClienteResponseDTO com os dados atualizados.
     */
    ClienteResponseDTO atualizarCliente(Long id, ClienteDTO dto);

    // ==========================================================
    // --- ATIVAÇÃO / DESATIVAÇÃO ---
    // ==========================================================
    /**
     * Ativa ou desativa um cliente, invertendo o status atual.
     * @param id ID do cliente.
     * @return ClienteResponseDTO com o status atualizado.
     */
    ClienteResponseDTO ativarDesativarCliente(Long id);

    // ==========================================================
    // --- LISTAGEM SIMPLES ---
    // ==========================================================
    /**
     * Lista todos os clientes ativos do sistema.
     * @return Lista de ClienteResponseDTO.
     */
    List<ClienteResponseDTO> listarClientesAtivos();

    // ==========================================================
    // --- LISTAGEM PAGINADA ---
    // ==========================================================
    /**
     * Lista clientes ativos de forma paginada.
     * @param pageable Parâmetros de paginação.
     * @return Página de ClienteResponseDTO.
     */
    Page<ClienteResponseDTO> listarClientesAtivosPaginado(Pageable pageable);
}
