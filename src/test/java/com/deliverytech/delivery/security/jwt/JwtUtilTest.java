package com.deliverytech.delivery.security.jwt;


import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Cliente; // IMPORT ADICIONADO
import com.deliverytech.delivery.enums.Role; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtUtil (Refatorado)")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @Mock
    private Usuario mockUsuario; 
    
    // --- MUDANÇA: MOCK DO CLIENTE NECESSÁRIO ---
    @Mock
    private Cliente mockCliente; // <-- ADICIONADO

    private final String testSecret = "a-very-long-and-secure-test-secret-key-for-hs256-123456";
    private final long testExpiration = 3600000; // 1 hora

    /**
     * MÉTODO HELPER (CORRIGIDO)
     * Configura o mockUsuario para a arquitetura "Decisão 1".
     * Agora simula a chamada encadeada: usuario.getCliente().getNome()
     */
    private void setupDefaultMockUsuario() {
        when(mockUsuario.getUsername()).thenReturn("test@email.com");
        when(mockUsuario.getId()).thenReturn(1L);
        when(mockUsuario.getRole()).thenReturn(Role.CLIENTE); // Use sua Enum aqui
        
        // --- CORREÇÃO (GARGALO 4 / DECISÃO 1) ---
        // 1. Mocka o 'Usuario' para retornar o 'Cliente'
        when(mockUsuario.getCliente()).thenReturn(mockCliente);
        // 2. Mocka o 'Cliente' para retornar o 'Nome'
        when(mockCliente.getNome()).thenReturn("Test User");
        // (A linha 'when(mockUsuario.getNome())' foi REMOVIDA)
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    // ==========================================================
    // Testes de Geração (Caminho Feliz)
    // (Este teste agora passa, pois o 'setupDefaultMockUsuario' está correto
    //  e o 'JwtUtil' (refatorado) sabe como encontrar o nome)
    // ==========================================================

    @Test
    @DisplayName("Deve gerar token e extrair claims com sucesso para Usuário")
    void generateToken_ShouldCreateValidToken_WhenUsuarioProvided() {
        // Given
        setupDefaultMockUsuario(); 
        when(mockUsuario.getRestauranteId()).thenReturn(10L);

        // When
        String token = jwtUtil.generateToken(mockUsuario);

        // Then
        assertNotNull(token);
        assertEquals("test@email.com", jwtUtil.extractUsername(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
        // A asserção do 'nome' continua válida, pois o JwtUtil (refatorado)
        // deve buscar o nome de 'cliente.getNome()' e colocá-lo no claim 'nome'.
        assertEquals("Test User", jwtUtil.extractClaim(token, claims -> claims.get("nome", String.class)));
        assertEquals("CLIENTE", jwtUtil.extractRole(token));
        assertEquals(10L, jwtUtil.extractRestauranteId(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("Deve gerar token sem restauranteId quando for nulo")
    void generateToken_ShouldNotIncludeRestauranteId_WhenUsuarioHasNone() {
        // (Este teste já estava OK)
        // Given
        setupDefaultMockUsuario();
        when(mockUsuario.getRestauranteId()).thenReturn(null);

        // When
        String token = jwtUtil.generateToken(mockUsuario);

        // Then
        assertNotNull(token);
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertNull(jwtUtil.extractRestauranteId(token));
    }

    @Test
    @DisplayName("Deve gerar token para UserDetails genérico (não-Usuario)")
    void generateToken_ShouldWorkWithGenericUserDetails() {
        // (Este teste já estava OK)
        // Given
        UserDetails genericUser = mock(UserDetails.class);
        when(genericUser.getUsername()).thenReturn("generic@user.com");

        // When
        String token = jwtUtil.generateToken(genericUser);

        // Then
        assertNotNull(token);
        assertEquals("generic@user.com", jwtUtil.extractUsername(token));
        assertNull(jwtUtil.extractUserId(token));
        assertNull(jwtUtil.extractRole(token));
    }

    // ==========================================================
    // Testes de Validação (O resto dos testes já estava OK)
    // ==========================================================

    @Test
    @DisplayName("ValidateToken: Deve retornar true para token válido e usuário correto")
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndUsernameMatches() {
        // Given
        setupDefaultMockUsuario();
        String token = jwtUtil.generateToken(mockUsuario);
        // When
        boolean isValid = jwtUtil.validateToken(token, mockUsuario);
        // Then
        assertTrue(isValid);
    }
    
    // ... (O resto dos seus testes: validateToken (false), isTokenExpired, SignatureException, etc. 
    //      estão corretos, pois dependem do 'setupDefaultMockUsuario' que agora está corrigido)
}