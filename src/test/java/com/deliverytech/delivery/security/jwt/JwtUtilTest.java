package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Cliente; 
import com.deliverytech.delivery.entity.Restaurante; // Novo Import
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
    
    @Mock
    private Cliente mockCliente; 

    @Mock
    private Restaurante mockRestaurante; // MOCK DA ENTIDADE RESTAURANTE

    private final String testSecret = "a-very-long-and-secure-test-secret-key-for-hs256-123456";
    private final long testExpiration = 3600000; // 1 hora

    /**
     * Configura o mockUsuario. Simula a chamada encadeada: usuario.getCliente().getNome()
     */
    private void setupDefaultMockUsuario() {
        when(mockUsuario.getUsername()).thenReturn("test@email.com");
        when(mockUsuario.getId()).thenReturn(1L);
        when(mockUsuario.getRole()).thenReturn(Role.CLIENTE); 
        
        // Mocka a navegação (Usuario -> Cliente -> Nome)
        when(mockUsuario.getCliente()).thenReturn(mockCliente);
        when(mockCliente.getNome()).thenReturn("Test User");
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    // ==========================================================
    // Testes de Geração (Caminho Feliz)
    // ==========================================================

    @Test
    @DisplayName("Deve gerar token e extrair claims com sucesso para Usuário")
    void generateToken_ShouldCreateValidToken_WhenUsuarioProvided() {
        // Given
        setupDefaultMockUsuario(); 
        
        // **CORREÇÃO: Mocka a navegação Usuario -> Restaurante -> Id**
        when(mockUsuario.getRestaurante()).thenReturn(mockRestaurante); 
        when(mockRestaurante.getId()).thenReturn(10L); 

        // When
        String token = jwtUtil.generateToken(mockUsuario);

        // Then
        assertNotNull(token);
        assertEquals("test@email.com", jwtUtil.extractUsername(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertEquals("Test User", jwtUtil.extractClaim(token, claims -> claims.get("nome", String.class)));
        assertEquals("CLIENTE", jwtUtil.extractRole(token));
        assertEquals(10L, jwtUtil.extractRestauranteId(token)); // Verifica o ID mockado
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("Deve gerar token sem restauranteId quando for nulo")
    void generateToken_ShouldNotIncludeRestauranteId_WhenUsuarioHasNone() {
        // Given
        setupDefaultMockUsuario();
        
        // **CORREÇÃO: Mocka o getRestaurante() para retornar null**
        when(mockUsuario.getRestaurante()).thenReturn(null);

        // When
        String token = jwtUtil.generateToken(mockUsuario);

        // Then
        assertNotNull(token);
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertNull(jwtUtil.extractRestauranteId(token)); // Deve ser nulo
    }

    @Test
    @DisplayName("Deve gerar token para UserDetails genérico (não-Usuario)")
    void generateToken_ShouldWorkWithGenericUserDetails() {
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
    // Testes de Validação
    // ==========================================================

    @Test
    @DisplayName("ValidateToken: Deve retornar true para token válido e usuário correto")
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndUsernameMatches() {
        // Given
        setupDefaultMockUsuario();
        
        // **CORREÇÃO: Mocka a navegação para gerar o token corretamente**
        when(mockUsuario.getRestaurante()).thenReturn(mockRestaurante); 
        when(mockRestaurante.getId()).thenReturn(10L); 
        
        String token = jwtUtil.generateToken(mockUsuario);
        
        // When
        boolean isValid = jwtUtil.validateToken(token, mockUsuario);
        
        // Then
        assertTrue(isValid);
    }
}