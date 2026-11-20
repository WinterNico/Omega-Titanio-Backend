package com.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// Se ejecutara por cada peticion, antes de llegar a los controladores
// No es @Component a proposito, lo registra el SecurityConfig asi no se ejecuta dos veces
public class JwtAuthFilter {
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService){
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")){
                String token = header.substring(7); // esto quita el "bearer "

                try {
                    Claims claims = jwtService.validar(token);
                    // Solo access tokens sirven para entrar a la API
                    if (JwtService.TIPO_ACCESS.equals(claims.get("tipo", String.class))){
                        String rol = claims.get("rol", String.class);
                        var autenticacion = new UsernamePasswordAuthenticationToken(
                                claims.getSubject(),
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                        SecurityContextHolder.getContext().setAuthentication(autenticacion);
                    }
                } catch (JwtException | IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
                chain.doFilter(request, response); // Con esto pasa la peticion a otro sitio
            }
        }


}
