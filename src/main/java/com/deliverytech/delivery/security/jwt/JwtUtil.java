package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException; 
import io.jsonwebtoken.MalformedJwtException; 
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; 
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof Usuario usuario) {
            claims.put("userId", usuario.getId());
            claims.put("role", usuario.getRole().name());
            
            // Lógica do Nome (usando os novos campos da Entidade)
            if (usuario.getNome() != null) {
                claims.put("nome", usuario.getNome());
            } else if (usuario.getCliente() != null) {
                claims.put("nome", usuario.getCliente().getNome());
            } else if (usuario.getRestaurante() != null) {
                claims.put("nome", usuario.getRestaurante().getNome());
            } else {
                claims.put("nome", usuario.getEmail());
            }
            
            // --- CORREÇÃO AQUI: Acessando o ID via Objeto Restaurante ---
            if (usuario.getRestaurante() != null) {
                claims.put("restauranteId", usuario.getRestaurante().getId());
            }
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ... (o restante dos métodos de extração e validação permanece igual) ...
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) 
            throws ExpiredJwtException, SignatureException, MalformedJwtException, JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.warn("Erro na validação do token: {}", e.getMessage());
        }
        return false;
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long extractRestauranteId(String token) {
        return extractClaim(token, claims -> claims.get("restauranteId", Long.class));
    }
}