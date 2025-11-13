// package com.deliverytech.delivery.security.jwt;

// import com.deliverytech.delivery.entity.Usuario;
// import com.deliverytech.delivery.enums.Role;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContext;
// import org.springframework.security.core.context.SecurityContextHolder;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// @DisplayName("Testes do SecurityUtils")
// class SecurityUtilsTest {

//     // --- Mocks Necessários ---
//     @Mock
//     private SecurityContext mockSecurityContext;

//     @Mock
//     private Authentication mockAuthentication;

//     @Mock
//     private Usuario mockUsuario; // Nossa entidade principal

//     @Mock
//     private Role mockRole; // A Role do usuário

//     @AfterEach
//     void tearDown() {
//         SecurityContextHolder.clearContext();
//     }

//     /**
//      * MÉTODO HELPER REVISADO:
//      * Agora ele apenas configura o contexto,
//      * sem criar 'stubs' desnecessários no mockUsuario.
//      */
//     private void setupSecurityContext() {
//         // 1. Configura a autenticação para retornar o mockUsuario como "principal"
//         when(mockAuthentication.getPrincipal()).thenReturn(mockUsuario);

//         // 2. Configura o contexto para retornar a autenticação
//         when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

//         // 3. Define o contexto mockado como o contexto "real"
//         SecurityContextHolder.setContext(mockSecurityContext);
//     }

//     // ==========================================================
//     // Testes de Caminho Feliz
//     // ==========================================================

//     @Test
//     @DisplayName("getCurrentUser: Deve retornar o usuário quando autenticado")
//     void getCurrentUser_ShouldReturnUser_WhenAuthenticated() {
//         // Given
//         setupSecurityContext();
//         // Adicionamos o stub *específico* que este teste usa:
//         when(mockUsuario.getId()).thenReturn(1L); 

//         // When
//         Usuario usuario = SecurityUtils.getCurrentUser();

//         // Then
//         assertNotNull(usuario);
//         assertEquals(1L, usuario.getId()); // A asserção usa o stub
//     }
    
//     @Test
//     @DisplayName("Helpers (ID, Email, Restaurante): Devem retornar dados corretos")
//     void helperMethods_ShouldReturnCorrectData_WhenUserIsAuthenticated() {
//         // Given
//         setupSecurityContext();
//         // Este teste usa TODOS os stubs, então todos são necessários:
//         when(mockUsuario.getId()).thenReturn(1L);
//         when(mockUsuario.getEmail()).thenReturn("test@user.com");
//         when(mockUsuario.getRestauranteId()).thenReturn(10L);
//         when(mockUsuario.getRole()).thenReturn(mockRole);
//         when(mockRole.name()).thenReturn("CLIENTE");
        
//         // When & Then
//         assertEquals(1L, SecurityUtils.getCurrentUserId());
//         assertEquals("test@user.com", SecurityUtils.getCurrentUserEmail());
//         assertEquals("CLIENTE", SecurityUtils.getCurrentUserRole());
//         assertEquals(10L, SecurityUtils.getCurrentRestauranteId());
//     }

//     @Test
//     @DisplayName("Role Checkers (hasRole, isCliente): Devem retornar booleano correto")
//     void roleCheckers_ShouldReturnCorrectBoolean_WhenUserIsAuthenticated() {
//         // Given
//         setupSecurityContext();
//         // Este teste só precisa dos stubs de Role:
//         when(mockUsuario.getRole()).thenReturn(mockRole);
//         when(mockRole.name()).thenReturn("CLIENTE");

//         // When & Then
//         assertTrue(SecurityUtils.hasRole("CLIENTE"));
//         assertFalse(SecurityUtils.hasRole("ADMIN"));

//         assertTrue(SecurityUtils.isCliente());
//         assertFalse(SecurityUtils.isAdmin());
//         assertFalse(SecurityUtils.isRestaurante());
//         assertFalse(SecurityUtils.isEntregador());
//     }
    
//     @Test
//     @DisplayName("getUsuarioLogado (Instância): Deve retornar o usuário logado")
//     void getUsuarioLogado_ShouldReturnCurrentUser_WhenUserIsAuthenticated() {
//         // Given
//         setupSecurityContext();
//         // Este teste só precisa do stub de ID para sua asserção:
//         when(mockUsuario.getId()).thenReturn(1L);
        
//         SecurityUtils securityUtilsInstance = new SecurityUtils();

//         // When
//         Usuario usuario = securityUtilsInstance.getUsuarioLogado();

//         // Then
//         assertNotNull(usuario);
//         assertEquals(1L, usuario.getId());
//     }

//     // ==========================================================
//     // Testes de Falha (Estes já estavam corretos, não precisam de stubs)
//     // ==========================================================

//     @Test
//     @DisplayName("getCurrentUser: Deve lançar exceção se NINGUÉM estiver logado")
//     void getCurrentUser_ShouldThrowException_WhenAuthenticationIsNull() {
//         // Given
//         when(mockSecurityContext.getAuthentication()).thenReturn(null);
//         SecurityContextHolder.setContext(mockSecurityContext);

//         // When & Then
//         RuntimeException exception = assertThrows(
//             RuntimeException.class,
//             () -> SecurityUtils.getCurrentUser()
//         );
//         assertEquals("Usuário não autenticado", exception.getMessage());
//     }

//     @Test
//     @DisplayName("getCurrentUser: Deve lançar exceção se Principal não for UserDetails")
//     void getCurrentUser_ShouldThrowException_WhenPrincipalIsNotUserDetails() {
//         // Given
//         when(mockAuthentication.getPrincipal()).thenReturn("um-objeto-qualquer"); // String não é UserDetails
//         when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
//         SecurityContextHolder.setContext(mockSecurityContext);

//         // When & Then
//         RuntimeException exception = assertThrows(
//             RuntimeException.class,
//             () -> SecurityUtils.getCurrentUser()
//         );
//         assertEquals("Usuário não autenticado", exception.getMessage());
//     }

//     @Test
//     @DisplayName("hasRole: Deve retornar 'false' e cobrir o 'catch' quando ninguém está logado")
//     void hasRole_ShouldReturnFalse_WhenNoUserIsAuthenticated() {
//         // Given
//         when(mockSecurityContext.getAuthentication()).thenReturn(null);
//         SecurityContextHolder.setContext(mockSecurityContext);

//         // When
//         boolean result = SecurityUtils.hasRole("ADMIN");

//         // Then
//         assertFalse(result); // Prova que o 'catch' foi executado
//     }
// }