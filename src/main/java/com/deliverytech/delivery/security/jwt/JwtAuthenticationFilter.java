package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.service.auth.UsuarioService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

   @Override
protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
) throws ServletException, IOException {

    String path = request.getRequestURI();

    if (path.startsWith("/api-docs") ||
    path.startsWith("/swagger-ui") ||
    path.startsWith("/swagger-ui.html") ||
    path.startsWith("/h2-console") ||
    path.startsWith("/api/auth/login") ||
    path.startsWith("/api/auth/register")) {
    filterChain.doFilter(request, response);
    return;
}
    final String requestTokenHeader = request.getHeader("Authorization");
    String username = null;
    String jwtToken = null;

    // JWT Token deve estar no formato "Bearer token"
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
        jwtToken = requestTokenHeader.substring(7);
        try {
            username = jwtUtil.extractUsername(jwtToken);
        } catch (IllegalArgumentException e) {
            logger.error("Não foi possível obter o JWT Token", e);
        } catch (ExpiredJwtException e) {
            logger.error("JWT Token expirado", e);
        } catch (MalformedJwtException e) {
            logger.error("JWT Token malformado", e);
        }
    } else {
        logger.warn("JWT Token não começa com Bearer String");
    }

    // Validar token
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = usuarioService.loadUserByUsername(username);

        if (jwtUtil.validateToken(jwtToken, userDetails)) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    filterChain.doFilter(request, response);
}
};