package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import com.deliverytech.delivery.entity.Cliente;
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

/**
 * Utilitário para lidar com operações de JSON Web Token (JWT),
 * como geração, validação e extração de claims.
 */
@Component
public class JwtUtil {

    /**
     * Logger para registrar falhas de validação de token.
     */
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * Chave secreta usada para assinar e validar os tokens.
     * Injetado de 'application.properties'.
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * Tempo de expiração do token em milissegundos.
     * Injetado de 'application.properties'.
     */
    @Value("${app.jwt.expiration-ms}")
    private long expiration;

    /**
     * Converte a string 'secret' em uma chave de assinatura segura (SecretKey).
     * @return A SecretKey para assinar o JWT.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Gera um novo token JWT para um usuário.
     * Adiciona claims customizados como userId, role, nome e restauranteId.
     *
     * @param userDetails Os detalhes do usuário (obtidos do UserDetailsService).
     * @return Uma string de token JWT.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof Usuario usuario) {
            claims.put("userId", usuario.getId());
            claims.put("role", usuario.getRole().name());
            
            // Busca o nome do perfil Cliente associado
            Cliente cliente = usuario.getCliente();
            if (cliente != null) {
                claims.put("nome", cliente.getNome());
            } else {
                claims.put("nome", null); // (Admin/Restaurante não tem 'nome' no perfil)
            }
            
            if (usuario.getRestauranteId() != null) {
                claims.put("restauranteId", usuario.getRestauranteId());
            }
        }

        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername()) // O "subject" é o email (username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrai um "claim" (informação) específico do token usando um resolver.
     *
     * @param token O token JWT.
     * @param claimsResolver A função que extrai o claim.
     * @param <T> O tipo do claim.
     * @return O claim extraído.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Faz o parse do token e retorna todos os claims (corpo) dele.
     * Lança exceções específicas se o token estiver malformado, expirado ou com assinatura inválida.
     *
     * @param token O token JWT.
     * @return O objeto Claims.
     */
    private Claims extractAllClaims(String token) 
            throws ExpiredJwtException, SignatureException, MalformedJwtException, JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrai o 'username' (subject) do token.
     * @param token O token JWT.
     * @return O username (email).
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração do token.
     * @param token O token JWT.
     * @return A data de expiração.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Verifica se o token está expirado.
     * @param token O token JWT.
     * @return true se o token estiver expirado, false caso contrário.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            // Se a própria extração falhar por expiração, ele está expirado.
            return true;
        }
    }

    /**
     * Valida um token JWT.
     * Verifica se o username bate com o UserDetails e se não está expirado.
     * Captura e loga exceções de validação (assinatura, malformado, etc.).
     *
     * @param token O token JWT.
     * @param userDetails Os detalhes do usuário carregado do banco.
     * @return true se o token for válido, false caso contrário.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        
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

    /**
     * Extrai o claim customizado 'userId'.
     * @param token O token JWT.
     * @return O ID do usuário.
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extrai o claim customizado 'role'.
     * @param token O token JWT.
     * @return A Role (como String).
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrai o claim customizado 'restauranteId' (se existir).
     * @param token O token JWT.
     * @return O ID do restaurante (ou nulo).
     */
    public Long extractRestauranteId(String token) {
        return extractClaim(token, claims -> claims.get("restauranteId", Long.class));
    }
}