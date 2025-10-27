package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.service.auth.AuthService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de segurança customizado que intercepta todas as requisições HTTP.
 *
 * Esta classe estende {@link OncePerRequestFilter} para garantir que seja executada
 * apenas uma vez por requisição.
 *
 * Sua principal responsabilidade é:
 * 1. Verificar a presença de um token JWT no header "Authorization".
 * 2. Validar o token (assinatura, expiração, etc.) usando o {@link JwtUtil}.
 * 3. Se o token for válido, carregar os dados do usuário (via {@link AuthService}) e
 * definir a autenticação no {@link SecurityContextHolder}, permitindo que a
 * requisição prossiga como "autenticada".
 * 4. Se o token for inválido ou expirado, ele anexa a exceção ao {@link HttpServletRequest}
 * (sob o atributo "JWT_EXCEPTION") para que o {@link com.deliverytech.delivery.config.SecurityConfig}
 * (via AuthenticationEntryPoint) possa tratá-la e retornar um JSON de erro 401 customizado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * Construtor para injeção de dependências.
     *
     * @param authService O serviço de autenticação para carregar dados do usuário (UserDetails).
     * É injetado com {@link Lazy} para quebrar uma possível dependência circular
     * com a configuração de segurança principal (SecurityConfig).
     * @param jwtUtil O componente utilitário para validar e extrair dados do token JWT.
     */
    public JwtAuthenticationFilter(@Lazy AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Método principal do filtro, executado para cada requisição.
     *
     * @param request A requisição HTTP.
     * @param response A resposta HTTP.
     * @param filterChain O objeto que nos permite invocar o próximo filtro na cadeia.
     * @throws ServletException Se ocorrer um erro de servlet.
     * @throws IOException Se ocorrer um erro de I/O.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // --- 1. Extração do Token ---
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // O token deve existir e começar com "Bearer "
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            try {
                // Tenta fazer o parse do token para extrair o username
                username = jwtUtil.extractUsername(jwtToken);
            
            // --- 2. Tratamento de Erros de Token ---
            // Bloco 'catch' crucial para o tratamento de erro 401 customizado.
            // Ele salva a exceção no request para ser lida pelo SecurityConfig.

            } catch (ExpiredJwtException e) {
                // Token expirou
                logger.warn("Token JWT expirado: " + e.getMessage());
                request.setAttribute("JWT_EXCEPTION", e); 
            
            } catch (JwtException e) {
                // Token inválido (Malformed, Signature, etc.)
                logger.warn("Token JWT inválido (Malformed, Signature, etc.): " + e.getMessage());
                request.setAttribute("JWT_EXCEPTION", e);
            
            } catch (Exception e) {
                // Outro erro inesperado
                logger.error("Erro inesperado ao extrair token JWT", e);
                request.setAttribute("JWT_EXCEPTION", e);
            }
        } else {
            logger.warn("Header 'Authorization' ausente ou mal formatado");
        }

        // --- 3. Validação e Autenticação ---
        
        // Se o 'username' foi extraído com sucesso (token válido)
        // E se o usuário ainda não está autenticado no contexto de segurança
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Carrega os detalhes do usuário do banco de dados
            UserDetails userDetails = this.authService.loadUserByUsername(username);

            // Valida se o token pertence a este usuário e se não expirou (dupla checagem)
            if (jwtUtil.validateToken(jwtToken, userDetails)) {
                
                // Cria o objeto de autenticação
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // Define o usuário como autenticado no contexto de segurança do Spring.
                // É esta linha que "autentica" o usuário para esta requisição.
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // --- 4. Continuação da Cadeia de Filtros ---
        
        // Passa a requisição (e resposta) para o próximo filtro na cadeia.
        // Se a autenticação foi definida acima, o usuário estará "logado".
        // Se não (ex: token inválido), o SecurityContext estará vazio, e os
        // filtros de autorização do Spring irão rejeitar a requisição,
        // acionando nosso AuthenticationEntryPoint customizado (do SecurityConfig).
        filterChain.doFilter(request, response);
    }
}