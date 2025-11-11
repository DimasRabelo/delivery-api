package com.deliverytech.delivery.config;

import com.deliverytech.delivery.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// --- 1. ADICIONAR IMPORT 'HttpMethod' ---
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

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}) // Usa o Bean 'corsConfigurationSource' abaixo
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            // ... (Seu tratamento de 401 customizado - perfeito)
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
                .authorizeHttpRequests(auth -> auth
                        // --- 2. REGRA ADICIONADA PARA CORRIGIR O 'OPTIONS' 403 ---
                        // Permite todas as requisições 'OPTIONS' (preflight de CORS)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                
                        // 4.1. Endpoints PÚBLICOS (seu código original)
                        .requestMatchers(
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/actuator/health",
                                "/dashboard",
                                "/dashboard/api/metrics",
                                "/dashboard/api/set-users/**",
                                "/h2-console/**",
                                "/api/auth/**",
                                "/api/restaurantes/**",
                                "/api/produtos/**"
                        ).permitAll() 

                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        
                        // 4.3. Todo o resto exige autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // --- 3. MÉTODO "PATCH" ADICIONADO AQUI ---
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}