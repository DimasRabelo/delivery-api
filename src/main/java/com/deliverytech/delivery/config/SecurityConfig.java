package com.deliverytech.delivery.config;

import com.deliverytech.delivery.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Configuração central do Spring Security para a aplicação.
 *
 * Esta classe define:
 * 1. A cadeia de filtros de segurança (quais endpoints são públicos/privados).
 * 2. A configuração de autenticação JWT (usando {@link JwtAuthenticationFilter}).
 * 3. O tratamento de exceções de autenticação (retorno 401 customizado).
 * 4. A configuração de CORS (Cross-Origin Resource Sharing).
 * 5. Beans essenciais como {@link PasswordEncoder} e {@link AuthenticationManager}.
 *
 * A anotação {@link EnableMethodSecurity} habilita o uso de anotações como @PreAuthorize
 * em nível de método nos controllers.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor

public class SecurityConfig {

    /**
     * Filtro customizado que intercepta todas as requisições para validar o token JWT
     * presente no header 'Authorization'.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Expõe o {@link PasswordEncoder} como um Bean gerenciado pelo Spring.
     * Define o BCrypt como o algoritmo de hashing de senhas.
     *
     * @return Uma instância de {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expõe o {@link AuthenticationManager} como um Bean.
     * Este bean é necessário para o processo de autenticação manual
     * (ex: no endpoint de login no {@link com.deliverytech.delivery.controller.auth.AuthController}).
     *
     * @param authConfig A configuração de autenticação do Spring.
     * @return O {@link AuthenticationManager} gerenciado.
     * @throws Exception Se houver erro ao obter o AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura e define a cadeia de filtros de segurança (Security Filter Chain) principal.
     * É aqui que a maior parte da configuração de segurança da aplicação é definida.
     *
     * @param http O construtor do HttpSecurity para configurar a cadeia.
     * @return A cadeia de filtros de segurança construída.
     * @throws Exception Se houver erro na configuração.
     */
   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Desabilita o CSRF (Cross-Site Request Forgery)
                .csrf(csrf -> csrf.disable())

                // 2. Habilita o CORS (Cross-Origin Resource Sharing)
                .cors(cors -> {
                })

                // 3. Configura o tratamento de exceções de autenticação (Erro 401)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // ... (seu código de tratamento de erro 401 customizado - perfeito, não mude nada) ...
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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
                            String json = String.format(
                                    "{ \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\" }",
                                    message, request.getRequestURI()
                            );
                            response.getWriter().write(json);
                            response.flushBuffer();
                        })
                )

                // 4. Define as regras de autorização de requisições
                .authorizeHttpRequests(auth -> auth
                        // 4.1. Endpoints PÚBLICOS (não exigem token)
                        .requestMatchers(
                                
                               // Endpoints do Swagger
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                
                                // Health Check PÚBLICO
                                "/actuator/health",

                                // Libera a página HTML do Dashboard
                                 "/dashboard",
                        // Libera a API de métricas que o HTML chama
                                "/dashboard/api/metrics",
                                "/dashboard/api/set-users/**",
                                
                                // Outros Endpoints Públicos
                                "/h2-console/**",
                                "/api/auth/**",
                                "/api/restaurantes/**",
                                "/api/produtos/**"
                        ).permitAll() // Permite todos os listados acima

                        // ==========================================================
                        // ⬇️ AQUI ESTÁ A NOVA REGRA DA ATIVIDADE ⬇️
                        // ==========================================================
                        // 4.2. Endpoints SENSÍVEIS do Actuator (exigem Role ADMIN)
                        //     Qualquer outra rota /actuator/ (como /info, /metrics, /env)
                        //     que não foi liberada acima (como /health) vai cair aqui.
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // ==========================================================
                        
                        // 4.3. Todo o resto (anyRequest) exige autenticação
                        .anyRequest().authenticated()
                )

                // 5. Configura a política de gerenciamento de sessão (STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 6. Adiciona nosso filtro JWT
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Permite que o console do H2 funcione
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    /**
     * Expõe um Bean para configurar o CORS (Cross-Origin Resource Sharing) globalmente.
     * Permite que o frontend (rodando em outra origem/domínio) acesse esta API.
     *
     * @return A fonte de configuração do CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permite requisições de qualquer origem (em produção, pode ser restrito)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // Permite os principais métodos HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // Permite todos os headers
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // Permite o envio de credenciais (como cookies ou tokens de autorização)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Aplica esta configuração para todos os paths (/**) da aplicação
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}