package com.deliverytech.delivery.config;

import com.deliverytech.delivery.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; 

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuração central de segurança do Spring Security.
 * Habilita a segurança em nível de método (ex: @PreAuthorize) e
 * configura o filtro JWT, CORS, CSRF e as regras de autorização de HTTP.
 */
@Configuration
@EnableMethodSecurity // Habilita o uso de anotações como @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Define o encoder de senhas da aplicação (BCrypt) como um Bean.
     * @return O PasswordEncoder a ser usado pelo Spring Security.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expõe o AuthenticationManager do Spring Security como um Bean.
     * Necessário para o processo de autenticação manual no AuthController.
     * @param authConfig Configuração de autenticação injetada.
     * @return O AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

   /**
    * Configura a cadeia de filtros de segurança (o "firewall" da aplicação).
    * Define quais endpoints são públicos, quais são protegidos e como
    * lidar com exceções de autenticação.
    *
    * @param http O construtor HttpSecurity.
    * @return O SecurityFilterChain construído.
    */
   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF (Cross-Site Request Forgery), comum em APIs stateless
                .csrf(csrf -> csrf.disable())
                
                // Habilita o CORS, que usará o Bean 'corsConfigurationSource' abaixo
                .cors(cors -> {}) 
                
                // Configura o tratamento de exceções de autenticação
                .exceptionHandling(ex -> ex
                        // Define um ponto de entrada customizado para erros 401 (Unauthorized)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            // Tenta pegar a exceção real que o JwtAuthenticationFilter armazenou
                            Exception exception = (Exception) request.getAttribute("JWT_EXCEPTION");
                            String message = "Token ausente ou inválido";
                            if (exception != null) {
                                String exName = exception.getClass().getSimpleName();
                                switch (exName) {
                                    case "ExpiredJwtException":
                                        message = "Token expirado";
                                        break;
                                    case "MalformedJwtException":
                                    case "SignatureException":
                                    case "JwtException":
                                        message = "Token inválido";
                                        break;
                                }
                            }
                            
                            // Escreve o JSON de erro padronizado
                            String json = String.format(
                                    "{ \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\" }",
                                    message, request.getRequestURI()
                            );
                            response.getWriter().write(json);
                            response.flushBuffer();
                        })
                )
                // Define as regras de autorização para os endpoints
                .authorizeHttpRequests(auth -> auth
                        // Permite todas as requisições 'OPTIONS' (preflight de CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                
                        // Endpoints PÚBLICOS
                        .requestMatchers(
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/health", // Health check público
                                "/dashboard",
                                "/dashboard/api/metrics",
                                "/dashboard/api/set-users/**",
                                "/h2-console/**", // Acesso ao H2 Console
                                "/api/auth/**", // Login e Registro
                                "/api/restaurantes/**", // Consulta de restaurantes
                                "/api/produtos/**" // Consulta de produtos/cardápios
                        ).permitAll() 

                        // Endpoint de Actuator (exceto /health) restrito ao ADMIN
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        
                        // Todo o resto exige autenticação
                        .anyRequest().authenticated()
                )
                // Configura a política de sessão como STATELESS (sem estado)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Adiciona o filtro JWT antes do filtro de autenticação padrão do Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Permite o H2 Console ser exibido em um <iframe>
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    /**
     * Configura as regras de CORS (Cross-Origin Resource Sharing) da aplicação.
     * Define quais origens, métodos e headers são permitidos.
     *
     * @return A fonte de configuração CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite requisições de qualquer origem (em produção, restrinja isso)
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); 
        
        // Permite os métodos HTTP necessários, incluindo PATCH
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Permite todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Permite o envio de credenciais (cookies, etc.)
        configuration.setAllowCredentials(true); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuração a todos os paths da API
        source.registerCorsConfiguration("/**", configuration); 
        return source;
    }
}