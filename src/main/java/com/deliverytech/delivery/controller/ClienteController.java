package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.ClienteResponseDTO;
import com.deliverytech.delivery.service.ClienteService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@Tag(name = "Clientes", description = "Operações relacionadas aos clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // --------------------------------------------------------------------------
    // 🔹 CADASTRAR NOVO CLIENTE
    // --------------------------------------------------------------------------
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(@Valid @RequestBody ClienteDTO dto) {
        // Chama o serviço para cadastrar o cliente
        ClienteResponseDTO cliente = clienteService.cadastrarCliente(dto);
        // Retorna HTTP 201 (Created) com os dados do cliente
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    // --------------------------------------------------------------------------
    // 🔹 BUSCAR CLIENTE POR ID
    // --------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        // Chama o serviço para buscar cliente pelo ID
        ClienteResponseDTO cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    // --------------------------------------------------------------------------
    // 🔹 BUSCAR CLIENTE POR EMAIL
    // --------------------------------------------------------------------------
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResponseDTO> buscarPorEmail(@PathVariable String email) {
        // Chama o serviço para buscar cliente pelo email
        ClienteResponseDTO cliente = clienteService.buscarClientePorEmail(email);
        return ResponseEntity.ok(cliente);
    }

    // --------------------------------------------------------------------------
    // 🔹 ATUALIZAR CLIENTE
    // --------------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable Long id, 
                                                               @Valid @RequestBody ClienteDTO dto) {
        // Chama o serviço para atualizar os dados do cliente
        ClienteResponseDTO cliente = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(cliente);
    }

    // --------------------------------------------------------------------------
    // 🔹 ATIVAR/DESATIVAR CLIENTE (SOFT DELETE)
    // --------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // retorna 204
    public void ativarDesativarCliente(@PathVariable Long id) {
        // Chama o serviço para ativar ou desativar o cliente
        clienteService.ativarDesativarCliente(id);
    }

    // --------------------------------------------------------------------------
    // 🔹 LISTAR TODOS OS CLIENTES ATIVOS
    // --------------------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesAtivos() {
        // Chama o serviço para listar todos os clientes ativos
        List<ClienteResponseDTO> cliente = clienteService.listarClientesAtivos();
        return ResponseEntity.ok(cliente);
    }
}
