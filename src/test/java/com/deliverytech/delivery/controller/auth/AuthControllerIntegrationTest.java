package com.deliverytech.delivery.controller.auth;

import com.deliverytech.delivery.dto.auth.LoginRequest;
import com.deliverytech.delivery.dto.auth.RegisterRequest;
import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role;
import com.deliverytech.delivery.repository.auth.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser; // Importante
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de Integração para o AuthController.
 * Carrega o contexto completo da aplicação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Usa o application-test.properties (H2)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Limpa o H2
@DisplayName("Testes de Integração do AuthController")
class AuthControllerIntegrationTest {

    // --- DEPENDÊNCIAS REAIS INJETADAS ---
    // (Não há mais @MockBean)
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository; // O repositório REAL

    @Autowired
    private PasswordEncoder passwordEncoder; // O encoder REAL

    @BeforeEach
    void setup() {
        // Limpa o banco antes de cada teste
        usuarioRepository.deleteAll();
    }

    /**
     * Método auxiliar para criar um usuário real no banco H2
     */
    private Usuario criarUsuarioNoBanco(String email, String senha, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome("Test User");
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha)); // Criptografa a senha
        usuario.setRole(role);
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
        
    }

    // ==========================================================
    // Testes do /login (Cobrir o 'catch BadCredentialsException')
    // ==========================================================

    @Test
    @DisplayName("Login: Deve retornar 401 (Unauthorized) quando a senha está errada")
    void login_ShouldReturn401_WhenPasswordIsWrong() throws Exception {
        // Given
        // 1. Cria um usuário REAL no banco
        criarUsuarioNoBanco("user@email.com", "senha-certa", Role.CLIENTE);

        // 2. Prepara uma requisição de login com a SENHA ERRADA
        LoginRequest loginRequest = new LoginRequest("user@email.com", "senha-errada");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // 1. O AuthenticationManager REAL vai falhar
                .andExpect(status().isUnauthorized())
                // 2. O 'catch (BadCredentialsException e)' será executado
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    // ==========================================================
    // Testes do /register (Cobrir o 'if (existsByEmail)')
    // ==========================================================
    
    @Test
    @DisplayName("Register: Deve retornar 400 (Bad Request) quando email já existe")
    void register_ShouldReturn400_WhenEmailAlreadyExists() throws Exception {
        // Given
        // 1. Cria um usuário REAL no banco
        criarUsuarioNoBanco("email.existente@email.com", "senha123", Role.CLIENTE);

        // 2. Prepara uma requisição de registro com o MESMO email
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setNome("Outro Nome");
        registerRequest.setEmail("email.existente@email.com");
        registerRequest.setSenha("outraSenha");
        registerRequest.setRole(Role.CLIENTE);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // 1. O AuthService REAL vai retornar true para existsByEmail
                .andExpect(status().isBadRequest())
                // 2. O 'if (authService.existsByEmail...)' será executado
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    // ==========================================================
    // Testes do /me (Cobrir o 'if (!(principal instanceof Usuario))')
    // ==========================================================
    
    @Test
    @WithMockUser // Esta anotação cria um Principal que NÃO é 'instanceof Usuario'
    @DisplayName("GetMe: Deve retornar 401 se o Principal não for 'Usuario' (ex: @WithMockUser)")
    void getMe_ShouldReturn401_WhenPrincipalIsNotUsuarioInstance() throws Exception {
        // Given
        // @WithMockUser já simulou um usuário "genérico" logado.

        // When & Then
        mockMvc.perform(get("/api/auth/me"))
                // 1. O Security Filter vai deixar passar (pois @WithMockUser autentica)
                .andExpect(status().isUnauthorized())
                // 2. O 'if (!(principal instanceof Usuario usuario))' será executado
                .andExpect(jsonPath("$.message").value("Token inválido. Acesso negado."));
    }

    
}