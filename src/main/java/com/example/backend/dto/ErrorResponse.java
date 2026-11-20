package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

// Formato único para todos los errores de la API. "errores" trae el mensaje por campo cuando falla la validación.
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        Map<String, String> errores
) {}