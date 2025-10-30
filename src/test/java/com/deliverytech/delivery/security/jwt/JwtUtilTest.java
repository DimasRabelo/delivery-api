package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.enums.Role; // Verifique se este é o import correto
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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
@DisplayName("Testes do JwtUtil")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @Mock
    private Usuario mockUsuario; 

    private final String testSecret = "a-very-long-and-secure-test-secret-key-for-hs256-123456";
    private final long testExpiration = 3600000; // 1 hora

    /**
     * NOVO MÉTODO HELPER
     * Configura o mockUsuario padrão. Só é chamado pelos testes que o utilizam.
     */
    private void setupDefaultMockUsuario() {
        when(mockUsuario.getUsername()).thenReturn("test@email.com");
        when(mockUsuario.getId()).thenReturn(1L);
        when(mockUsuario.getNome()).thenReturn("Test User");
        when(mockUsuario.getRole()).thenReturn(Role.CLIENTE); // Use sua Enum aqui
    }

    @BeforeEach
    void setUp() {
        // --- setUp() AGORA ESTÁ MAIS LIMPO ---
        // Contém apenas o que é comum a TODOS os testes.
        jwtUtil = new JwtUtil();

        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);

        // Os 'when(mockUsuario...)' foram REMOVIDOS daqui.
    }

    // ==========================================================
    // Testes de Geração (Caminho Feliz)
    // ==========================================================

    @Test
    @DisplayName("Deve gerar token e extrair claims com sucesso para Usuário")
    void generateToken_ShouldCreateValidToken_WhenUsuarioProvided() {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
        when(mockUsuario.getRestauranteId()).thenReturn(10L);

        // When
        String token = jwtUtil.generateToken(mockUsuario);

        // Then
        assertNotNull(token);
        assertEquals("test@email.com", jwtUtil.extractUsername(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertEquals("Test User", jwtUtil.extractClaim(token, claims -> claims.get("nome", String.class)));
        assertEquals("CLIENTE", jwtUtil.extractRole(token));
        assertEquals(10L, jwtUtil.extractRestauranteId(token));
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("Deve gerar token sem restauranteId quando for nulo")
    void generateToken_ShouldNotIncludeRestauranteId_WhenUsuarioHasNone() {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
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
        // Given
        // Este teste NÃO usa o setupDefaultMockUsuario()
        UserDetails genericUser = mock(UserDetails.class);
        when(genericUser.getUsername()).thenReturn("generic@user.com");

        // When
        String token = jwtUtil.generateToken(genericUser);

        // Then
        assertNotNull(token);
        assertEquals("generic@user.com", jwtUtil.extractUsername(token));
        assertNull(jwtUtil.extractUserId(token));
        assertNull(jwtUtil.extractRole(token));
        assertNull(jwtUtil.extractRestauranteId(token));
    }

    // ==========================================================
    // Testes de Validação (Caminho Feliz)
    // ==========================================================

    @Test
    @DisplayName("ValidateToken: Deve retornar true para token válido e usuário correto")
    void validateToken_ShouldReturnTrue_WhenTokenIsValidAndUsernameMatches() {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
        String token = jwtUtil.generateToken(mockUsuario);

        // When
        boolean isValid = jwtUtil.validateToken(token, mockUsuario);

        // Then
        assertTrue(isValid);
    }

    // ==========================================================
    // Testes de Falha (Linhas Perdidas)
    // ==========================================================

    @Test
    @DisplayName("ValidateToken: Deve retornar false quando username é diferente")
    void validateToken_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
        String token = jwtUtil.generateToken(mockUsuario);
        
        UserDetails otherUser = mock(UserDetails.class);
        when(otherUser.getUsername()).thenReturn("other@email.com");

        // When
        boolean isValid = jwtUtil.validateToken(token, otherUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("ExtractAllClaims: Deve lançar ExpiredJwtException para token expirado")
    void extractAllClaims_ShouldThrowExpiredJwtException_WhenTokenIsExpired() throws InterruptedException {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String expiredToken = jwtUtil.generateToken(mockUsuario);

        Thread.sleep(5);

        // When & Then
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.extractUsername(expiredToken);
        });
    }
    
    @Test
    @DisplayName("isTokenExpired: Deve retornar true e cobrir o 'catch' para token expirado")
    void isTokenExpired_ShouldReturnTrueAndCoverCatch_WhenTokenIsExpired() throws InterruptedException {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String expiredToken = jwtUtil.generateToken(mockUsuario);

        Thread.sleep(5);

        // When
        boolean isExpired = jwtUtil.isTokenExpired(expiredToken);

        // Then
        assertTrue(isExpired);
    }


    @Test
    @DisplayName("ExtractAllClaims: Deve lançar SignatureException para token com assinatura inválida")
    void extractAllClaims_ShouldThrowSignatureException_WhenTokenSignatureIsInvalid() {
        // Given
        setupDefaultMockUsuario(); // <--- CHAMADA AO HELPER
        JwtUtil anotherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(anotherJwtUtil, "secret", "this-is-a-completely-different-secret-key-987654");
        ReflectionTestUtils.setField(anotherJwtUtil, "expiration", testExpiration);
        
        String tokenWithBadSignature = anotherJwtUtil.generateToken(mockUsuario);

        // When & Then
        assertThrows(SignatureException.class, () -> {
            jwtUtil.extractUsername(tokenWithBadSignature);
        });
    }
    
    @Test
    @DisplayName("ExtractAllClaims: Deve lançar MalformedJwtException para token malformado")
    void extractAllClaims_ShouldThrowMalformedJwtException_WhenTokenIsGarbage() {
        // Given
        // Este teste NÃO usa o setupDefaultMockUsuario()
        String garbageToken = "isto.nao.e.um.token";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.extractUsername(garbageToken);
        });
    }
}