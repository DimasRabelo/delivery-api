package com.deliverytech.delivery.security.jwt; 

import com.deliverytech.delivery.service.auth.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    // --- DEPENDÊNCIAS MOCADAS ---
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthService authService;

    // --- OBJETOS HTTP MOCADOS ---
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    // --- SYSTEM UNDER TEST (SUT) ---
    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Objeto de apoio
    private UserDetails mockUserDetails;
    private final String validToken = "valid.jwt.token";
    private final String testEmail = "test@user.com";

    @BeforeEach
    void setUp() {
        // Cria um UserDetails mock padrão para os testes
        mockUserDetails = new User(testEmail, "password", new ArrayList<>());

        // !! IMPORTANTE !!
        // Limpa o contexto de segurança ANTES de cada teste
        // O SecurityContextHolder é 'static' e 'ThreadLocal',
        // então testes podem interferir uns nos outros se não for limpo.
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // Limpa o contexto DEPOIS do teste por segurança
        SecurityContextHolder.clearContext();
    }

    // ==========================================================
    // Teste 1: Caminho Feliz (Token Válido)
    // ==========================================================
    @Test
    @DisplayName("Deve autenticar usuário com token válido")
    void doFilterInternal_ShouldSetAuthentication_WhenTokenIsValid() throws ServletException, IOException {
        // -----------------
        // Given (Arrange)
        // -----------------
        // 1. Simula o header "Authorization: Bearer ..."
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        // 2. Simula o JwtUtil extraindo o username
        when(jwtUtil.extractUsername(validToken)).thenReturn(testEmail);
        // 3. Simula o AuthService carregando o usuário
        when(authService.loadUserByUsername(testEmail)).thenReturn(mockUserDetails);
        // 4. Simula o JwtUtil validando o token (true)
        when(jwtUtil.validateToken(validToken, mockUserDetails)).thenReturn(true);

        // -----------------
        // When (Act)
        // -----------------
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // -----------------
        // Then (Assert)
        // -----------------
        // 1. Verifica se a autenticação foi DE FINIDA no SecurityContext
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testEmail, SecurityContextHolder.getContext().getAuthentication().getName());
        // 2. Verifica se o filtro continuou a cadeia
        verify(filterChain, times(1)).doFilter(request, response);
        // 3. Verifica se NENHUMA exceção foi setada no request
        verify(request, never()).setAttribute(eq("JWT_EXCEPTION"), any());
    }

    // ==========================================================
    // Testes de Falha (Linhas Perdidas)
    // ==========================================================

    @Test
    @DisplayName("Não deve autenticar se o header Authorization estiver ausente")
    void doFilterInternal_ShouldNotAuthenticate_WhenHeaderIsMissing() throws ServletException, IOException {
        // Given
        // Header está nulo
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // 1. Verifica se a autenticação continua NULA
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // 2. Verifica se o filtro continuou (isso é importante)
        verify(filterChain, times(1)).doFilter(request, response);
        // 3. Verifica se o JwtUtil NUNCA foi chamado
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Não deve autenticar se o header não começar com 'Bearer '")
    void doFilterInternal_ShouldNotAuthenticate_WhenHeaderIsMalformed() throws ServletException, IOException {
        // Given
        // Header tem um formato inválido (ex: Basic Auth)
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNzd29yZA==");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("Deve setar 'JWT_EXCEPTION' no request quando token expirar")
    void doFilterInternal_ShouldSetRequestAttribute_WhenTokenIsExpired() throws ServletException, IOException {
        // Given
        String expiredToken = "expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        // 1. Simula o JwtUtil lançando a exceção de expiração
        ExpiredJwtException expiredException = new ExpiredJwtException(null, null, "Token expirado");
        when(jwtUtil.extractUsername(expiredToken)).thenThrow(expiredException);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // 1. Verifica se a autenticação NÃO foi setada
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // 2. Verifica se o filtro continuou
        verify(filterChain, times(1)).doFilter(request, response);
        // 3. !! VERIFICAÇÃO PRINCIPAL !!
        // Confirma que a exceção foi salva no request para o EntryPoint tratar
        verify(request, times(1)).setAttribute("JWT_EXCEPTION", expiredException);
    }

    @Test
    @DisplayName("Deve setar 'JWT_EXCEPTION' no request quando token for malformado")
    void doFilterInternal_ShouldSetRequestAttribute_WhenTokenIsMalformed() throws ServletException, IOException {
        // Given
        String malformedToken = "not.a.real.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + malformedToken);

        // 1. Simula o JwtUtil lançando a exceção de formato
        MalformedJwtException malformedException = new MalformedJwtException("Token inválido");
        when(jwtUtil.extractUsername(malformedToken)).thenThrow(malformedException);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, times(1)).doFilter(request, response);
        // 2. Verifica se a exceção foi salva no request
        verify(request, times(1)).setAttribute("JWT_EXCEPTION", malformedException);
    }

    @Test
    @DisplayName("Não deve autenticar se o usuário já estiver autenticado")
    void doFilterInternal_ShouldSkipAuthentication_WhenUserIsAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        // 1. Coloca uma autenticação PRÉ-EXISTENTE no contexto
        UserDetails existingUser = new User("existing@user.com", "", new ArrayList<>());
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        // 2. Simula um header válido (que seria processado se não houvesse auth)
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtil.extractUsername(validToken)).thenReturn(testEmail); // Token de outro usuário

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // 1. Verifica se a autenticação original (existingAuth) FOI MANTIDA
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        assertEquals("existing@user.com", SecurityContextHolder.getContext().getAuthentication().getName());
        // 2. Verifica se o filtro continuou
        verify(filterChain, times(1)).doFilter(request, response);
        // 3. Verifica se o AuthService NUNCA foi chamado (pois a auth já existia)
        verify(authService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).validateToken(any(), any());
    }
}