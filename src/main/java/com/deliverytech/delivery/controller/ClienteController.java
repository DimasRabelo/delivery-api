package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.ClienteDTO;
import com.deliverytech.delivery.dto.ClienteResponseDTO;
import com.deliverytech.delivery.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // Cadastrar novo cliente
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrarCliente(@Valid @RequestBody ClienteDTO dto) {
        ClienteResponseDTO cliente = clienteService.cadastrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        ClienteResponseDTO cliente = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    // Buscar cliente por email
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResponseDTO> buscarPorEmail(@PathVariable String email) {
        ClienteResponseDTO cliente = clienteService.buscarClientePorEmail(email);
        return ResponseEntity.ok(cliente);
    }

    // Atualizar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable Long id, 
                                                               @Valid @RequestBody ClienteDTO dto) {
        ClienteResponseDTO cliente = clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok(cliente);
    }

    // Ativar/Desativar cliente (soft delete)
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT) // retorna 204
public void ativarDesativarCliente(@PathVariable Long id) {
    clienteService.ativarDesativarCliente(id);
}

    // Listar todos os clientes ativos
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesAtivos() {
        List<ClienteResponseDTO> cliente = clienteService.listarClientesAtivos();
        return ResponseEntity.ok(cliente);
    }
}
