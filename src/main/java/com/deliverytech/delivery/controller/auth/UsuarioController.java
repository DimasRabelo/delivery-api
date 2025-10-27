package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.service.auth.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller para operações de Gerenciamento (CRUD) de Usuários.
 *
 * Este controller lida com listagem, busca, atualização e deleção de usuários.
 *
 * @implNote Todos os endpoints neste controller (sob "/api/usuarios") são protegidos
 * e requerem um token JWT válido, conforme definido no {@link com.deliverytech.delivery.config.SecurityConfig}.
 */
@Tag(name = "2. Usuários (CRUD)", description = "Endpoints para gerenciamento de usuários. Requer autenticação.")
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
// Adiciona o requisito de segurança em todos os endpoints deste controller
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    /**
     * Serviço que contém a lógica de negócios para operações de Usuário.
     * (Injetado via @Autowired por campo).
     */
    @Autowired
    private UsuarioService usuarioService;

    /**
     * Lista todos os usuários cadastrados no sistema.
     *
     * @return ResponseEntity 200 (OK) com uma lista de {@link UserResponse}.
     */
    @Operation(summary = "Lista todos os usuários",
               description = "Retorna uma lista de todos os usuários cadastrados, formatados como UserResponse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários obtida com sucesso",
                         content = @Content(mediaType = "application/json",
                                 array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.buscarTodos();
        
        // Mapeia a lista de Entidades para uma lista de DTOs seguros
        List<UserResponse> response = usuarios.stream()
                .map(UserResponse::new) // Usa o construtor de mapeamento do DTO
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Busca um usuário específico pelo seu ID.
     *
     * @param id O ID do usuário a ser buscado.
     * @return ResponseEntity 200 (OK) com o {@link UserResponse} do usuário
     * ou ResponseEntity 404 (Not Found) se o usuário não for encontrado.
     */
    @Operation(summary = "Busca um usuário por ID",
               description = "Retorna os dados de um usuário específico, formatado como UserResponse.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                         content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(new UserResponse(usuario));
        } catch (Exception e) {
            // Idealmente, capturar uma exceção específica (ex: EntityNotFoundException)
            return ResponseEntity.status(404).body("Usuário não encontrado");
        }
    }

    /**
     * Atualiza os dados de um usuário existente.
     *
     * @param id O ID do usuário a ser atualizado.
     * @param usuarioAtualizado Objeto {@link Usuario} com os novos dados.
     * @return ResponseEntity 200 (OK) com o {@link UserResponse} do usuário atualizado
     * ou ResponseEntity 404 (Not Found) se o usuário não for encontrado.
     *
     * @implNote **Este método atualmente aceita a
     * entidade {@link Usuario} completa no corpo da requisição. 
     */
    @Operation(summary = "Atualiza um usuário por ID",
               description = "Atualiza os dados de um usuário existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                         content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "Erro ao atualizar usuário"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        try {
            Usuario usuario = usuarioService.atualizar(id, usuarioAtualizado);
            return ResponseEntity.ok(new UserResponse(usuario));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    /**
     * Deleta um usuário do sistema.
     * (Dependendo da regra de negócio, pode ser uma deleção lógica (setar `ativo = false`).)
     *
     * @param id O ID do usuário a ser deletado.
     * @return ResponseEntity 204 (No Content) se a deleção for bem-sucedida
     * ou ResponseEntity 404 (Not Found) se o usuário não for encontrado.
     */
    @Operation(summary = "Deleta um usuário por ID",
               description = "Remove um usuário do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao deletar usuário"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            usuarioService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Erro ao deletar usuário: " + e.getMessage());
        }
    }
}