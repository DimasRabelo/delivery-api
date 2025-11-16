package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.auth.LoginRequest;
import com.deliverytech.delivery.dto.auth.LoginResponse;
import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.security.jwt.JwtUtil;
import com.deliverytech.delivery.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Tag(name = "1. Autenticação", description = "Endpoints para login e registro de clientes.")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms:60000}")
    private long jwtExpirationMs;

    
    /**
     * Autentica um usuário com email e senha.
     */
    @Operation(summary = "Autentica um usuário (Login)", description = "Valida email/senha e retorna um token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida",
                         content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
    try {
        // Tenta autenticar usando o Spring Security AuthenticationManager
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getSenha()
                )
        );

        // --- INÍCIO DA CORREÇÃO ---

        // 1. Obtém o objeto Usuario autenticado
        Usuario usuario = (Usuario) auth.getPrincipal();

        // 🔑 GUARDA DE SEGURANÇA: BARRAR RESTAURANTE NO LOGIN GERAL
        if (usuario.getRole() == Role.RESTAURANTE) {
            // Lançar exceção para ser capturada pelo bloco catch
            // Retorna a mensagem de erro que o Frontend espera ver
            throw new BadCredentialsException("Acesso Negado: Use o Login Administrativo do Restaurante.");
        }

        // --- FIM DA CORREÇÃO ---

        // 2. Se a Role for CLIENTE/ADMIN/ENTREGADOR, gera o token
        String token = jwtUtil.generateToken(usuario);
        Date expirationDate = jwtUtil.extractExpiration(token);
        UserResponse userResponse = new UserResponse(usuario);
        long segundos = jwtExpirationMs / 1000;

        LoginResponse loginResponse = new LoginResponse(token, expirationDate, userResponse, segundos);

        return ResponseEntity.ok(loginResponse);

    } catch (BadCredentialsException e) {
        // Esta exceção agora captura tanto a falha de senha quanto a falha da guarda de Role.
        // O Spring Security trata isso como um 401 Unauthorized.
        
        // Retorna a mensagem customizada do erro (que pode ser "Acesso Negado: Use o Login..."
        // ou "Credenciais inválidas")
        return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("message", "Erro interno: " + e.getMessage()));
    }
}
/**
     * Autentica um usuário RESTAURANTE com email e senha.
     */
    @Operation(summary = "Autentica um usuário RESTAURANTE (Login)", 
               description = "Valida email/senha e retorna um token JWT para RESTAURANTES.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "401", description = "Credenciais ou Role inválidas")
    })
    @PostMapping("/login-restaurante") // NOVO ENDPOINT DEDICADO
    public ResponseEntity<?> loginRestaurante(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Tenta autenticar usando o Spring Security AuthenticationManager
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );

            // --- GUARDA DE SEGURANÇA INVERTIDA ---

            // 1. Obtém o objeto Usuario autenticado
            Usuario usuario = (Usuario) auth.getPrincipal();

            // 🔑 GUARDA DE SEGURANÇA INVERTIDA: Permitir SOMENTE RESTAURANTE
            if (usuario.getRole() != Role.RESTAURANTE) {
                // Se a Role NÃO for RESTAURANTE (é Cliente, Entregador, Admin), barra o acesso.
                throw new BadCredentialsException("Acesso Negado: Este endpoint é exclusivo para Restaurantes.");
            }

            // 2. Se a Role for RESTAURANTE, gera o token
            String token = jwtUtil.generateToken(usuario);
            Date expirationDate = jwtUtil.extractExpiration(token);
            UserResponse userResponse = new UserResponse(usuario);
            long segundos = jwtExpirationMs / 1000;

            LoginResponse loginResponse = new LoginResponse(token, expirationDate, userResponse, segundos);

            return ResponseEntity.ok(loginResponse);

        } catch (BadCredentialsException e) {
            // Retorna a mensagem customizada do erro.
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro interno: " + e.getMessage()));
        }
    }

    /**
     * Registra um novo CLIENTE no sistema.
     *
     * @param registerRequest DTO com os dados do novo cliente.
     * @return ResponseEntity 201 (Created) com o UserResponse do usuário criado.
     */
    @Operation(summary = "Registra um novo Cliente",
               description = "Cria um novo Cliente (Usuário + Perfil + Endereço). Valida se o email já existe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso",
                         content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email já está em uso ou dados de validação inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerCliente(@Valid @RequestBody RegisterRequest registerRequest) {
        
        // 1. Validação de e-mail duplicado
        if (authService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email já está em uso"
            ));
        }

        // 2. Delega a lógica de registro (cria Usuário, Cliente e Endereço) para o serviço
        Usuario novoUsuario = authService.registrarCliente(registerRequest);
        
        // 3. Retorna o usuário criado (sem dados sensíveis)
        UserResponse userResponse = new UserResponse(novoUsuario);
        return ResponseEntity.status(201).body(userResponse);
    }

    
    /**
     * Obtém os dados do usuário atualmente autenticado (logado).
     */
    @Operation(summary = "Obtém dados do usuário logado (requer token)",
               description = "Retorna os dados do usuário autenticado (baseado no token JWT enviado).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário autenticado",
                         content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido.")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // Valida se o usuário está autenticado e se o principal é uma instância de Usuario
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Usuario)) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Token ausente ou inválido. Acesso negado."));
        }

        // Obtém o objeto Usuario injetado pelo Spring Security
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(new UserResponse(usuario));
    }
}