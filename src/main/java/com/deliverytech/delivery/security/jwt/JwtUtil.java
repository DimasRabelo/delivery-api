package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Cliente; // IMPORT ADICIONADO
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
// --- IMPORTAÇÕES AGORA USADAS ---
import io.jsonwebtoken.JwtException; 
import io.jsonwebtoken.MalformedJwtException; 
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; 
import io.jsonwebtoken.Jwts;
// --- FIM DAS IMPORTAÇÕES ---
import org.slf4j.Logger; // <-- IMPORT ADICIONADO
import org.slf4j.LoggerFactory; // <-- IMPORT ADICIONADO
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

    // Adiciona um logger para registrar falhas de token
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // ... (Configuração - secret, expiration, getSigningKey - OK) ...
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration-ms}")
    private long expiration;
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }


    // -------------------------------------------------------------------------
    // Geração de Token (MÉTODO CORRIGIDO)
    // -------------------------------------------------------------------------
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof Usuario usuario) {
            claims.put("userId", usuario.getId());
            claims.put("role", usuario.getRole().name());
            
            // --- CORREÇÃO (GARGALO 4 / DECISÃO 1) ---
            Cliente cliente = usuario.getCliente();
            if (cliente != null) {
                claims.put("nome", cliente.getNome()); // <-- CORRIGIDO
            } else {
                claims.put("nome", null); // (Admin/Restaurante não tem 'nome' no perfil)
            }
            // --- FIM DA CORREÇÃO ---
            
            if (usuario.getRestauranteId() != null) {
                claims.put("restauranteId", usuario.getRestauranteId());
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

    // -------------------------------------------------------------------------
    // Extração de Claims (Leitura do Token)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Validação do Token (MÉTODO CORRIGIDO PARA USAR OS IMPORTS)
    // -------------------------------------------------------------------------

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
        
        // --- AGORA AS IMPORTAÇÕES SÃO USADAS ---
        } catch (SignatureException e) {
            logger.warn("Token JWT com assinatura inválida: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expirado: {}", e.getMessage());
        } catch (JwtException e) { // Pega qualquer outro erro
            logger.warn("Erro inesperado no token JWT: {}", e.getMessage());
        }
        
        return false;
    }

    // ... (Restante dos métodos: extractUserId, extractRole, etc. - OK) ...
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