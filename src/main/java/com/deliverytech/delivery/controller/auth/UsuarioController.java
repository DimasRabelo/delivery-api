package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.service.auth.UsuarioService;
import com.deliverytech.delivery.dto.auth.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Listar todos os usuários
    @GetMapping
    public ResponseEntity<List<UserResponse>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.buscarTodos();
        List<UserResponse> response = usuarios.stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            return ResponseEntity.ok(new UserResponse(usuario));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Usuário não encontrado");
        }
    }

    // Atualizar usuário
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario usuarioAtualizado) {
        try {
            Usuario usuario = usuarioService.atualizar(id, usuarioAtualizado);
            return ResponseEntity.ok(new UserResponse(usuario));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Erro ao atualizar usuário: " + e.getMessage());
        }
    }

    // Deletar usuário
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
