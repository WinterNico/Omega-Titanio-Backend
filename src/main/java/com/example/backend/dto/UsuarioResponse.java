package com.example.backend.dto;

import com.example.backend.model.Rol;
import java.time.LocalDateTime;

// Sin contraseña: nunca se devuelve al cliente
public record UsuarioResponse(
        Long id,
        String run,
        String nombre,
        String apellidos,
        String correo,
        Rol rol,
        String region,
        String comuna,
        String calle,
        String departamento,
        LocalDateTime fechaRegistro
) {}