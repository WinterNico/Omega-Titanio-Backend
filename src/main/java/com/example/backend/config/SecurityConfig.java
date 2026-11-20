package com.example.backend.config;

import com.example.backend.dto.ErrorResponse;
import com.example.backend.security.JwtAuthFilter;
import com.example.backend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String API = "/api/v1";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Rutas públicas (Swagger, errores, CORS y Auth)
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                API + "/auth/login",
                                API + "/auth/registro",
                                API + "/auth/refresh"
                        ).permitAll()

                        // 2. Catálogo (Lectura pública, edición solo ADMIN)
                        .requestMatchers(
                                HttpMethod.GET,
                                API + "/categorias",
                                API + "/categorias/**",
                                API + "/productos",
                                API + "/productos/**"
                        ).permitAll()
                        .requestMatchers(
                                API + "/categorias",
                                API + "/categorias/**",
                                API + "/productos",
                                API + "/productos/**"
                        ).hasRole("ADMIN")

                        // 3. Usuarios (Solo ADMIN)
                        .requestMatchers(
                                API + "/usuarios",
                                API + "/usuarios/**"
                        ).hasRole("ADMIN")

                        // 4. Órdenes (Permisos según acción y rol)
                        .requestMatchers(HttpMethod.POST, API + "/ordenes")
                        .hasAnyRole("CLIENTE", "ADMIN")
                        .requestMatchers(HttpMethod.GET, API + "/ordenes")
                        .hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, API + "/ordenes/mias", API + "/ordenes/*")
                        .authenticated()
                        .requestMatchers(API + "/ordenes/**")
                        .hasRole("ADMIN")

                        // 5. Cualquier otra ruta requiere sesión
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) ->
                                escribirError(res, 401, "No autenticado", "Debes iniciar sesión o tu sesión expiró")
                        )
                        .accessDeniedHandler((req, res, deniedEx) ->
                                escribirError(res, 403, "Acceso denegado", "Tu rol no tiene permiso para esta acción")
                        )
                )
                .addFilterBefore(
                        new JwtAuthFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    private void escribirError(HttpServletResponse res, int status, String error, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                mensaje,
                null
        );
        objectMapper.writeValue(res.getOutputStream(), body);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
