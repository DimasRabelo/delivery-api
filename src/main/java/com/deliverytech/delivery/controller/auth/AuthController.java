package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.auth.LoginRequest;
import com.deliverytech.delivery.dto.auth.LoginResponse;
import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.dto.auth.UserResponse;
import com.deliverytech.delivery.entity.Usuario;
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

/**
 * Controller responsável pelos endpoints públicos de autenticação e registro.
 *
 * Gerencia o login (autenticação), registro (criação de novos usuários) e
 * verificação de dados do usuário autenticado (ex: /me).
 */
@Tag(name = "1. Autenticação", description = "Endpoints para login, registro e verificação de usuário.")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Gerenciador de autenticação principal do Spring Security.
     * Usado para validar as credenciais do usuário no login.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Serviço que lida com a lógica de negócios de usuários (criar, buscar, etc.).
     */
    private final AuthService authService;

    /**
     * Componente utilitário para gerar e validar tokens JWT.
     */
    private final JwtUtil jwtUtil;

    /**
     * Tempo de expiração do token em milissegundos, injetado do 'application.properties'.
     * Usado para calcular o campo 'expiracaoSegundos' na resposta de login.
     * O valor padrão é 60000ms (1 minuto) se a propriedade não for encontrada.
     */
    @Value("${app.jwt.expiration-ms:60000}")
    private long jwtExpirationMs;

    /**
     * Autentica um usuário com email e senha.
     *
     * @param loginRequest DTO contendo o email e a senha para autenticação.
     * @return ResponseEntity 200 (OK) com o {@link LoginResponse} (contendo o token JWT)
     * ou ResponseEntity 401 (Unauthorized) se as credenciais forem inválidas.
     */
    @Operation(summary = "Autentica um usuário (Login)",
               description = "Valida as credenciais (email e senha) e retorna um token JWT se forem válidas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida",
                         content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (email ou senha incorretos)",
                         content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 1. Tenta autenticar usando o AuthenticationManager
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );

            // 2. Se a autenticação foi bem-sucedida, gera o token
            Usuario usuario = (Usuario) auth.getPrincipal();
            String token = jwtUtil.generateToken(usuario);
            Date expirationDate = jwtUtil.extractExpiration(token);

            // 3. Prepara a resposta (DTOs)
            UserResponse userResponse = new UserResponse(usuario);
            long segundos = jwtExpirationMs / 1000; // Converte ms para s

            // 4. Cria a resposta de login completa
            LoginResponse loginResponse = new LoginResponse(
                    token,
                    expirationDate,
                    userResponse,
                    segundos
            );

            return ResponseEntity.ok(loginResponse);

        } catch (BadCredentialsException e) {
            // 5. Captura falha de autenticação (email ou senha errada)
            return ResponseEntity.status(401).body(Map.of(
                    "status", 401,
                    "message", "Credenciais inválidas"
            ));
        } catch (Exception e) {
            // 6. Captura outros erros inesperados
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "message", "Erro interno: " + e.getMessage()
            ));
        }
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * @param registerRequest DTO contendo os dados do novo usuário (nome, email, senha, role).
     * @return ResponseEntity 201 (Created) com o {@link UserResponse} do usuário criado
     * ou ResponseEntity 400 (Bad Request) se o email já estiver em uso.
     */
    @Operation(summary = "Registra um novo usuário",
               description = "Cria um novo usuário (CLIENTE ou RESTAURANTE) no sistema. Valida se o email já existe.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                         content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email já está em uso ou dados de validação inválidos")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // Verifica se o email já está cadastrado
        if (authService.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Email já está em uso"
            ));
        }

        // Cria o novo usuário
        Usuario novoUsuario = authService.criarUsuario(registerRequest);
        
        // Retorna o DTO seguro (sem a senha)
        UserResponse userResponse = new UserResponse(novoUsuario);
        return ResponseEntity.status(201).body(userResponse);
    }

    /**
     * Obtém os dados do usuário atualmente autenticado (logado).
     * Este endpoint requer um token JWT válido no header 'Authorization'.
     *
     * @param authentication Objeto injetado pelo Spring Security contendo o principal (usuário).
     * @return ResponseEntity 200 (OK) com o {@link UserResponse} do usuário logado
     * ou ResponseEntity 401 (Unauthorized) se o token for inválido ou ausente.
     */
    @Operation(summary = "Obtém dados do usuário logado (requer token)",
               description = "Retorna os dados do usuário autenticado (baseado no token JWT enviado).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do usuário autenticado",
                         content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido. Acesso negado.")
    })
    @SecurityRequirement(name = "bearerAuth") // Informa ao Swagger que este endpoint precisa de autenticação
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        // Validação inicial (embora o SecurityConfig já deva barrar)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Token ausente ou inválido. Acesso negado."));
        }

        Object principal = authentication.getPrincipal();

        // Garante que o principal é da nossa classe Usuario
        if (!(principal instanceof Usuario usuario)) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Token inválido. Acesso negado."));
        }

        // Retorna o DTO seguro (sem a senha)
        return ResponseEntity.ok(new UserResponse(usuario));
    }
}