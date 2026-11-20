package com.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Se ejecutara por cada peticion, antes de llegar a los controladores
// No es @Component a proposito, lo registra el SecurityConfig asi no se ejecuta dos veces
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService){
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")){
            String token = header.substring(7); // esto quita el "Bearer "

            try {
                Claims claims = jwtService.validar(token);
                // Solo access tokens sirven para entrar a la API
                if (JwtService.TIPO_ACCESS.equals(claims.get("tipo", String.class))){
                    String rol = claims.get("rol", String.class);
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            claims.getSubject(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase(Locale.ROOT))));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Se limpia el contexto
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response); // Esto para que las rutas publicas puedan pasar
    }
}
