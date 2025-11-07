package com.deliverytech.delivery.controller;

import com.deliverytech.delivery.dto.request.ClienteDTO;
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.ClienteResponseDTO;
import com.deliverytech.delivery.dto.response.PagedResponseWrapper; // Importe o Wrapper Paginado
import com.deliverytech.delivery.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Importe o Pageable correto
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
@Validated // Habilita validação em parâmetros (ex: @Positive, @Email)
@Tag(name = "2. Clientes", description = "Gerenciamento de clientes (Admin) e auto-serviço")
@SecurityRequirement(name = "bearerAuth") // Aplica o cadeado a todos os endpoints
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    /**
     * Cadastra um novo cliente.
     * Este endpoint é redundante se /api/auth/register já faz isso para clientes.
     * Assumindo que este é um endpoint para ADMINS criarem clientes.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastrar cliente (ADMIN)",
               description = "Cria um novo cliente no sistema. Requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email já em uso ou dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ApiResponseWrapper<ClienteResponseDTO>> cadastrarCliente(
            @Valid @RequestBody ClienteDTO dto) {
        
        ClienteResponseDTO cliente = clienteService.cadastrarCliente(dto);
        ApiResponseWrapper<ClienteResponseDTO> response = 
                new ApiResponseWrapper<>(true, cliente, "Cliente criado com sucesso");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos os clientes ativos de forma paginada.
     * Acesso restrito a usuários com a role 'ADMIN'.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes (ADMIN, Paginado)",
               description = "Lista todos os clientes ativos do sistema com paginação. Requer role ADMIN.")
    @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso")
    public ResponseEntity<PagedResponseWrapper<ClienteResponseDTO>> listarClientes(
            @Parameter(description = "Informações de paginação (size, page, sort)") Pageable pageable) {
        
        Page<ClienteResponseDTO> clientes = clienteService.listarClientesAtivosPaginado(pageable);
        return ResponseEntity.ok(new PagedResponseWrapper<>(clientes));
    }

    /**
     * Busca um cliente específico pelo seu ID.
     * Acesso permitido para 'ADMIN' ou para o próprio 'CLIENTE'.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and #id == principal.id)")
    @Operation(summary = "Buscar cliente por ID",
               description = "Busca um cliente. Requer role ADMIN ou ser o próprio cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ClienteResponseDTO>> buscarPorId(
            @Parameter(description = "ID do cliente a ser buscado", required = true, example = "1") 
            @PathVariable @Positive Long id) {
        
        ClienteResponseDTO cliente = clienteService.buscarClientePorId(id);
        ApiResponseWrapper<ClienteResponseDTO> response = 
                new ApiResponseWrapper<>(true, cliente, "Cliente encontrado");
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um cliente específico pelo seu email.
     * Acesso restrito a usuários com a role 'ADMIN'.
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buscar cliente por Email (ADMIN)",
               description = "Busca um cliente pelo email. Requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ClienteResponseDTO>> buscarPorEmail(
            @Parameter(description = "Email do cliente a ser buscado", required = true, example = "cliente@email.com") 
            @PathVariable @Email String email) {
        
        ClienteResponseDTO cliente = clienteService.buscarClientePorEmail(email);
        ApiResponseWrapper<ClienteResponseDTO> response = 
                new ApiResponseWrapper<>(true, cliente, "Cliente encontrado");
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza os dados de um cliente existente.
     * Acesso restrito a 'ADMIN' ou ao próprio 'CLIENTE'.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('CLIENTE') and #id == principal.id)")
    @Operation(summary = "Atualizar dados do cliente",
               description = "Atualiza dados de um cliente. Requer role ADMIN ou ser o próprio cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<ApiResponseWrapper<ClienteResponseDTO>> atualizarCliente(
            @Parameter(description = "ID do cliente a ser atualizado", required = true, example = "1") 
            @PathVariable @Positive Long id,
            @Valid @RequestBody ClienteDTO dto) {
        
        ClienteResponseDTO cliente = clienteService.atualizarCliente(id, dto);
        ApiResponseWrapper<ClienteResponseDTO> response = 
                new ApiResponseWrapper<>(true, cliente, "Cliente atualizado com sucesso");
        return ResponseEntity.ok(response);
    }

    /**
     * Desativa um cliente (exclusão lógica / soft delete).
     * Acesso restrito a usuários com a role 'ADMIN'.
     * Retorna 204 No Content, como é padrão para DELETE.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desativar (soft delete) cliente (ADMIN)",
               description = "Desativa um cliente (exclusão lógica). Requer role ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente desativado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    public ResponseEntity<Void> ativarDesativarCliente(
            @Parameter(description = "ID do cliente a ser desativado", required = true, example = "1") 
            @PathVariable @Positive Long id) {
        
        clienteService.ativarDesativarCliente(id);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}