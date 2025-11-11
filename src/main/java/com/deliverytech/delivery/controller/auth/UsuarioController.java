package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO; 
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.PagedResponseWrapper;
import com.deliverytech.delivery.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List; 

/**
 * Controller para operações de Gerenciamento (CRUD) de Usuários.
 * O acesso a estes endpoints é geralmente restrito a ADMINS.
 */
@Tag(name = "6. Usuários (Admin)", description = "Gerenciamento de usuários. Requer role ADMIN.")
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@Validated 
@SecurityRequirement(name = "bearerAuth") 
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todos os usuários cadastrados no sistema (paginado).
     * Requer role ADMIN.
     */
    @Operation(summary = "Lista todos os usuários (ADMIN, Paginado)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponseWrapper<UserResponse>> listarUsuarios(Pageable pageable) {
        Page<UserResponse> usuariosPage = usuarioService.buscarTodos(pageable); 
        return ResponseEntity.ok(new PagedResponseWrapper<>(usuariosPage));
    }

    /**
     * Lista todos os usuários ENTREGADORES que estão ATIVOS.
     * Acesso restrito a usuários com role 'ADMIN' ou 'RESTAURANTE'.
     */
    @Operation(summary = "Lista todos os Entregadores ativos (ADMIN, RESTAURANTE)",
               description = "Retorna uma lista de todos os usuários com role ENTREGADOR que estão ativos. Requer role ADMIN ou RESTAURANTE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de entregadores obtida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN ou RESTAURANTE)")
    })
    @GetMapping("/entregadores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESTAURANTE')") // Define a permissão de acesso
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> listarEntregadoresAtivos() {
        
        List<UserResponse> entregadores = usuarioService.buscarEntregadoresAtivos(); 
        
        // Envelopa a resposta no formato padrão da API
        ApiResponseWrapper<List<UserResponse>> response = new ApiResponseWrapper<>(true, entregadores, "Entregadores ativos listados com sucesso");
        
        return ResponseEntity.ok(response);
    }


    /**
     * Busca um usuário específico pelo seu ID.
     * Requer role ADMIN ou que o usuário seja o dono da conta.
     */
    @Operation(summary = "Busca um usuário por ID (Admin ou Próprio)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> buscarPorId(
            @PathVariable @Positive Long id) {
        UserResponse usuario = usuarioService.buscarPorIdResponse(id); 
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, usuario, "Usuário encontrado"));
    }

    /**
     * Atualiza os dados de um usuário existente (ex: email).
     * Requer role ADMIN ou que o usuário seja o dono da conta.
     */
    @Operation(summary = "Atualiza um usuário por ID (Admin ou Próprio)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> atualizar(
            @PathVariable @Positive Long id, 
            @RequestBody @Valid UsuarioUpdateDTO dto) {
        UserResponse usuario = usuarioService.atualizar(id, dto);
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, usuario, "Usuário atualizado com sucesso"));
    }

    /**
     * Deleta (logicamente) um usuário do sistema.
     * Requer role ADMIN.
     */
    @Operation(summary = "Deleta um usuário por ID (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(
            @PathVariable @Positive Long id) {
        usuarioService.deletar(id);
        return ResponseEntity.noContent().build(); 
    }
}