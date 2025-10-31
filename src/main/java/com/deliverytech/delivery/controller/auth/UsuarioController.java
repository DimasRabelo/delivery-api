package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.dto.auth.UsuarioUpdateDTO; 
import com.deliverytech.delivery.dto.response.ApiResponseWrapper;
import com.deliverytech.delivery.dto.response.PagedResponseWrapper;
import com.deliverytech.delivery.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * Controller para operações de Gerenciamento (CRUD) de Usuários.
 * Todos os endpoints aqui são protegidos e requerem autorização granular.
 */
@Tag(name = "6. Usuários (Admin)", description = "Gerenciamento de usuários. Requer role ADMIN.") // <-- Tag renomeada
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@Validated // Adicionado para @Positive
@SecurityRequirement(name = "bearerAuth") // Segurança aplicada a todos
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todos os usuários cadastrados no sistema (paginado).
     * Acesso restrito a usuários com a role 'ADMIN'.
     */
    @Operation(summary = "Lista todos os usuários (ADMIN, Paginado)",
               description = "Retorna uma lista paginada de todos os usuários. Requer role ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários obtida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- SEGURANÇA ADICIONADA
    public ResponseEntity<PagedResponseWrapper<UserResponse>> listarUsuarios(Pageable pageable) {
        
        // Assumindo que o service foi atualizado para aceitar Pageable e retornar Page<UserResponse>
        Page<UserResponse> usuariosPage = usuarioService.buscarTodos(pageable); 
        
        return ResponseEntity.ok(new PagedResponseWrapper<>(usuariosPage));
    }

    /**
     * Busca um usuário específico pelo seu ID.
     * Acesso permitido para 'ADMIN' ou para o próprio usuário.
     */
    @Operation(summary = "Busca um usuário por ID (Admin ou Próprio)",
               description = "Retorna os dados de um usuário. Requer role ADMIN ou ser o próprio usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id") // <-- SEGURANÇA ADICIONADA
    public ResponseEntity<ApiResponseWrapper<UserResponse>> buscarPorId(
            @Parameter(description = "ID do usuário") 
            @PathVariable @Positive Long id) {
        
        // REMOVIDO try-catch: Deixe o @RestControllerAdvice (GlobalExceptionHandler) tratar a exceção
        UserResponse usuario = usuarioService.buscarPorIdResponse(id); // Assumindo que service retorna DTO
        
        return ResponseEntity.ok(new ApiResponseWrapper<>(true, usuario, "Usuário encontrado"));
    }

    /**
     * Atualiza os dados de um usuário existente (ex: nome, email).
     * NÃO use este método para atualizar senha ou role.
     * Acesso permitido para 'ADMIN' ou para o próprio usuário.
     *
     * @implNote Recebe um DTO (UsuarioUpdateDTO) e não a Entidade, por segurança.
     */
    @Operation(summary = "Atualiza um usuário por ID (Admin ou Próprio)",
               description = "Atualiza os dados de um usuário (ex: nome). Requer role ADMIN ou ser o próprio usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id") // <-- SEGURANÇA ADICIONADA
    public ResponseEntity<ApiResponseWrapper<UserResponse>> atualizar(
            @Parameter(description = "ID do usuário") @PathVariable @Positive Long id, 
            @Parameter(description = "Dados a serem atualizados") @RequestBody @Valid UsuarioUpdateDTO dto) { // <-- DTO NO LUGAR DA ENTIDADE
        
        // REMOVIDO try-catch
        UserResponse usuario = usuarioService.atualizar(id, dto); // Assumindo que service aceita o DTO

        return ResponseEntity.ok(new ApiResponseWrapper<>(true, usuario, "Usuário atualizado com sucesso"));
    }

    /**
     * Deleta um usuário do sistema (soft ou hard delete).
     * Acesso restrito a usuários com a role 'ADMIN'.
     */
    @Operation(summary = "Deleta um usuário por ID (ADMIN)",
               description = "Remove um usuário do sistema. Requer role ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (não é ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- SEGURANÇA ADICIONADA
    public ResponseEntity<Void> deletar(
            @Parameter(description = "ID do usuário") @PathVariable @Positive Long id) {
        
        // REMOVIDO try-catch
        usuarioService.deletar(id);
        
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}