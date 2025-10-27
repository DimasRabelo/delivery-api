package com.deliverytech.delivery.security.jwt;

import com.deliverytech.delivery.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Componente utilitário para todas as operações relacionadas a JSON Web Tokens (JWT).
 *
 * Esta classe é responsável por:
 * 1. Gerar novos tokens para usuários autenticados.
 * 2. Extrair informações (claims) de tokens existentes.
 * 3. Validar tokens (verificar assinatura, expiração e username).
 *
 * As configurações de 'secret' e 'expiration' são injetadas
 * diretamente do arquivo {@code application.properties}.
 */
@Component
public class JwtUtil {

    // -------------------------------------------------------------------------
    // Configuração (Injetada)
    // -------------------------------------------------------------------------

    /**
     * Chave secreta usada para assinar e validar os tokens.
     * Injetada a partir do application.properties (app.jwt.secret).
     */
    @Value("${app.jwt.secret}")
    private String secret;

    /**
     * Tempo de expiração do token em milissegundos.
     * Injetado a partir do application.properties (app.jwt.expiration-ms).
     */
    @Value("${app.jwt.expiration-ms}")
    private long expiration;

    /**
     * Converte a chave secreta (String) em um objeto {@link SecretKey} criptografado,
     * que é o formato exigido pela biblioteca 'jjwt' para assinatura.
     *
     * @return A chave de assinatura HMAC-SHA.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // -------------------------------------------------------------------------
    // Geração de Token
    // -------------------------------------------------------------------------

    /**
     * Gera um novo token JWT para um usuário, enriquecido com claims customizados.
     *
     * @param userDetails O principal de segurança do usuário (a entidade {@link Usuario}).
     * @return Uma string de token JWT compacta e assinada.
     */
    public String generateToken(UserDetails userDetails) {
        // Cria um mapa de "claims" (informações) customizadas
        Map<String, Object> claims = new HashMap<>();

        // Adiciona dados específicos da nossa entidade Usuario para enriquecer o token
        if (userDetails instanceof Usuario usuario) {
            claims.put("userId", usuario.getId());
            claims.put("role", usuario.getRole().name());
            claims.put("nome", usuario.getNome());
            if (usuario.getRestauranteId() != null) {
                claims.put("restauranteId", usuario.getRestauranteId());
            }
        }

        // Define as datas de emissão e expiração
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration); // Usa o valor injetado

        // Constrói o token
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername()) // "Dono" do token (email)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // -------------------------------------------------------------------------
    // Extração de Claims (Leitura do Token)
    // -------------------------------------------------------------------------

    /**
     * Método genérico para extrair qualquer informação (claim) de um token.
     *
     * @param <T>            O tipo do dado a ser extraído (ex: String, Date, Long).
     * @param token          O token JWT a ser lido.
     * @param claimsResolver Uma função que define qual claim extrair (ex: Claims::getSubject).
     * @return O valor do claim extraído.
     * @throws JwtException Se o token for inválido, expirado ou tiver a assinatura errada.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Método principal de parse do token.
     * Valida a assinatura e retorna todo o payload (corpo) do token.
     *
     * @param token O token JWT a ser "parseado".
     * @return O objeto {@link Claims} contendo todo o payload.
     * @throws ExpiredJwtException   Se o token já expirou.
     * @throws SignatureException    Se a assinatura for inválida.
     * @throws MalformedJwtException Se o token estiver em um formato inválido.
     * @throws JwtException          Para outros erros genéricos de JWT.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extrai o "username" (Subject) do token.
     *
     * @param token O token JWT.
     * @return O email do usuário (o "subject" do token).
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai a data de expiração do token.
     *
     * @param token O token JWT.
     * @return A data de expiração.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // -------------------------------------------------------------------------
    // Validação do Token
    // -------------------------------------------------------------------------

    /**
     * Verifica se o token já expirou.
     *
     * @param token O token JWT.
     * @return 'true' se o token expirou, 'false' caso contrário.
     */
    public boolean isTokenExpired(String token) {
        try {
            // Tenta extrair a data. Se 'extractExpiration' falhar
            // (lançando ExpiredJwtException), o catch trata.
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            // Se o token está expirado, a própria extração falha.
            return true;
        }
    }

    /**
     * Valida um token comparando o username e a data de expiração.
     * (A assinatura é validada implicitamente pelo {@link #extractUsername}).
     *
     * @param token       O token JWT a ser validado.
     * @param userDetails O usuário carregado do banco.
     * @return 'true' se o token for válido, 'false' caso contrário.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // -------------------------------------------------------------------------
    // Claims Customizados
    // -------------------------------------------------------------------------

    /**
     * Extrai o ID do usuário (claim customizado "userId") do token.
     *
     * @param token O token JWT.
     * @return O ID do usuário.
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    /**
     * Extrai a Role (claim customizado "role") do token.
     *
     * @param token O token JWT.
     * @return A role como String (ex: "CLIENTE").
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Extrai o ID do restaurante (claim customizado "restauranteId") do token.
     *
     * @param token O token JWT.
     * @return O ID do restaurante, ou 'null' se o usuário não tiver um.
     */
    public Long extractRestauranteId(String token) {
        return extractClaim(token, claims -> claims.get("restauranteId", Long.class));
    }
}