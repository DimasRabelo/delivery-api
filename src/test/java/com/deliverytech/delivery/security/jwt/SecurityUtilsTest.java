package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Restaurante; 
import com.deliverytech.delivery.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails; 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock; 

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SecurityUtils")
class SecurityUtilsTest {

    // --- Mocks Necessários ---
    @Mock
    private SecurityContext mockSecurityContext;

    @Mock
    private Authentication mockAuthentication;

    @Mock
    private Usuario mockUsuario; 
    
    @Mock
    private Restaurante mockRestaurante; 

    @Mock
    private Role mockRole; 
    
    @AfterEach
    void tearDown() {
        // Limpa o contexto de segurança após cada teste
        SecurityContextHolder.clearContext();
    }

    /**
     * Configura o contexto de segurança para o caminho feliz.
     */
    private void setupSecurityContext() {
        // A autenticação deve ser considerada autenticada para o caminho feliz
        when(mockAuthentication.isAuthenticated()).thenReturn(true); 
        when(mockAuthentication.getPrincipal()).thenReturn(mockUsuario);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);
    }
    
    // ==========================================================
    // Testes de Caminho Feliz
    // ==========================================================

    @Test
    @DisplayName("getCurrentUser: Deve retornar o usuário quando autenticado")
    void getCurrentUser_ShouldReturnUser_WhenAuthenticated() {
        // Given
        setupSecurityContext();
        when(mockUsuario.getId()).thenReturn(1L); 

        // When
        Usuario usuario = SecurityUtils.getCurrentUser();

        // Then
        assertNotNull(usuario);
        assertEquals(1L, usuario.getId());
    }
    
    @Test
    @DisplayName("Helpers (ID, Email, Restaurante): Devem retornar dados corretos")
    void helperMethods_ShouldReturnCorrectData_WhenUserIsAuthenticated() {
        // Given
        setupSecurityContext();
        
        when(mockUsuario.getRestaurante()).thenReturn(mockRestaurante); 
        when(mockRestaurante.getId()).thenReturn(10L); 
        
        when(mockUsuario.getId()).thenReturn(1L);
        when(mockUsuario.getEmail()).thenReturn("test@user.com");
        when(mockUsuario.getRole()).thenReturn(mockRole);
        when(mockRole.name()).thenReturn("CLIENTE");
        
        // When & Then
        assertEquals(1L, SecurityUtils.getCurrentUserId());
        assertEquals("test@user.com", SecurityUtils.getCurrentUserEmail());
        assertEquals("CLIENTE", SecurityUtils.getCurrentUserRole());
        assertEquals(10L, SecurityUtils.getCurrentRestauranteId());
    }

    @Test
    @DisplayName("Role Checkers (hasRole, isCliente): Devem retornar booleano correto")
    void roleCheckers_ShouldReturnCorrectBoolean_WhenUserIsAuthenticated() {
        // Given
        setupSecurityContext();
        when(mockUsuario.getRole()).thenReturn(mockRole);
        when(mockRole.name()).thenReturn("CLIENTE");

        // When & Then
        assertTrue(SecurityUtils.hasRole("CLIENTE"));
        assertFalse(SecurityUtils.hasRole("ADMIN"));

        assertTrue(SecurityUtils.isCliente());
        assertFalse(SecurityUtils.isAdmin());
        assertFalse(SecurityUtils.isRestaurante());
        assertFalse(SecurityUtils.isEntregador());
    }
    
    @Test
    @DisplayName("getUsuarioLogado (Instância): Deve retornar o usuário logado")
    void getUsuarioLogado_ShouldReturnCurrentUser_WhenUserIsAuthenticated() {
        // Given
        setupSecurityContext();
        when(mockUsuario.getId()).thenReturn(1L);
        
        SecurityUtils securityUtilsInstance = new SecurityUtils();

        // When
        Usuario usuario = securityUtilsInstance.getUsuarioLogado();

        // Then
        assertNotNull(usuario);
        assertEquals(1L, usuario.getId());
    }

    // ==========================================================
    // Testes de Falha (O código de produção agora lança a exceção)
    // ==========================================================

    @Test
    @DisplayName("getCurrentUser: Deve lançar exceção se NINGUÉM estiver logado")
    void getCurrentUser_ShouldThrowException_WhenAuthenticationIsNull() {
        // Given
        // Força o getAuthentication() a retornar null (usuário anônimo/não logado)
        when(mockSecurityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(mockSecurityContext); 
        
        // When & Then
        // O código de produção (SecurityUtils) agora lança a exceção esperada
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> SecurityUtils.getCurrentUser() 
        );
        assertEquals("Usuário não autenticado", exception.getMessage()); 
    }

    @Test
    @DisplayName("getCurrentUser: Deve lançar exceção se Principal não for Usuario")
    void getCurrentUser_ShouldThrowException_WhenPrincipalIsNotUserDetails() {
        // Given
        // Cria um Principal que é um UserDetails, mas não é nossa entidade Usuario
        UserDetails genericUserDetails = mock(UserDetails.class);
        when(mockAuthentication.getPrincipal()).thenReturn(genericUserDetails); 
        // Forçamos que a autenticação esteja 'autenticada' para testar a checagem de tipo
        when(mockAuthentication.isAuthenticated()).thenReturn(true); 
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        // When & Then
        // O SecurityUtils deve lançar a exceção na checagem 'instanceof Usuario'
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> SecurityUtils.getCurrentUser() 
        );
        assertEquals("Usuário não autenticado", exception.getMessage());
    }
}