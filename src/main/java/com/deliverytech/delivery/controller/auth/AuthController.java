package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.dto.auth.LoginRequest;
import com.deliverytech.delivery.dto.auth.LoginResponse;
import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.security.jwt.JwtUtil;
import com.deliverytech.delivery.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final Long jwtExpiration = 86400000L;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );

            Usuario usuario = (Usuario) auth.getPrincipal();
            String token = jwtUtil.generateToken(usuario);

            UserResponse userResponse = new UserResponse(usuario);
            LoginResponse loginResponse = new LoginResponse(token, jwtExpiration, userResponse);

            return ResponseEntity.ok(loginResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (authService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Email já está em uso");
        }

        Usuario novoUsuario = authService.criarUsuario(registerRequest);
        UserResponse userResponse = new UserResponse(novoUsuario);
        return ResponseEntity.status(201).body(userResponse);
    }

   @GetMapping("/me")
public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Token ausente ou inválido. Acesso negado."));
    }

    Object principal = authentication.getPrincipal();
    if (!(principal instanceof Usuario usuario)) {
        return ResponseEntity.status(401)
                .body(Map.of("message", "Token inválido. Acesso negado."));
    }

    return ResponseEntity.ok(new UserResponse(usuario));
}

}
