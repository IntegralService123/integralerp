package com.example.integral_erp.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                // AUTH
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/me").authenticated()

                // PRODUTOS
                .requestMatchers(HttpMethod.GET, "/api/produtos/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.POST, "/api/produtos/**")
                    .hasRole("BASE_ADMIN")

                .requestMatchers(HttpMethod.PUT, "/api/produtos/**")
                    .hasRole("BASE_ADMIN")

                .requestMatchers(HttpMethod.DELETE, "/api/produtos/**")
                    .hasRole("BASE_ADMIN")

                // CATEGORIAS
                .requestMatchers(HttpMethod.GET, "/api/categorias/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.POST, "/api/categorias/**")
                    .hasRole("BASE_ADMIN")

                // ESTOQUE
                .requestMatchers("/api/estoque/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR")

                // IMAGEM 
                .requestMatchers(HttpMethod.POST, "/api/imagens/**")
                    .hasRole("BASE_ADMIN")

                .requestMatchers("/uploads/**").permitAll()

                // CARRINHO
                .requestMatchers(HttpMethod.POST, "/api/carrinho/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.GET, "/api/carrinho/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.PATCH, "/api/carrinho/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.DELETE, "/api/carrinho/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // PEDIDOS
                .requestMatchers(HttpMethod.POST, "/api/pedidos/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                .requestMatchers(HttpMethod.GET, "/api/pedidos/**")
                    .hasAnyRole("BASE_ADMIN", "DISTRIBUIDOR", "CLIENTE")

                // MERCADO PAGO
                .requestMatchers(HttpMethod.POST, "/api/pagamentos/webhook").permitAll()
                
                .anyRequest().authenticated()
            )
            .userDetailsService(usuarioDetailsService)
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            )
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("https://integralerpweb.vercel.app"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
