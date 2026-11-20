package com.example.backend.dto;

// Lo que recibe el frontend al hacer login, registro o refresh
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tipo,          // siempre "Bearer"
        long expiraEnMs,      // cuanto dura el access token
        UsuarioResponse usuario
) {}