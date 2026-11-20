package com.example.backend.security;

import com.example.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final long duracionAccessMs;
    private final long duracionRefreshMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiration-ms}") long duracionAccessMs,
            @Value("${app.jwt.refresh-expiration-ms}") long duracionRefreshMs
    ){
        // Convertimos el texto secreto en una clave HMAC para firmar
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionAccessMs = duracionAccessMs;
        this.duracionRefreshMs = duracionRefreshMs;
    }

    // Metodos generales
    public String generarAccessToken(Usuario usuario){
        return generar(usuario, TIPO_ACCESS, duracionAccessMs);
    }

    public String generarRefreshToken(Usuario usuario){
        return generar(usuario, TIPO_REFRESH, duracionRefreshMs);
    }

    public long getDuracionAccessMs(){
        return duracionAccessMs;
    }

    // Verificamos la firma y el vencimiento, se lanza JwtException en caso de falla
    public Claims validar(String token){
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String generar(Usuario usuario, String tipo, long duracionMs){
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("rol", usuario.getRol())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(clave)
                .compact();
    }


}
