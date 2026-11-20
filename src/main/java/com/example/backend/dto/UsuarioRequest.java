package com.example.backend.dto;

import com.example.backend.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El RUN es obligatorio")
        @Pattern(regexp = "^[0-9]{7,8}[0-9Kk]$", message = "RUN inválido (formato: 12345678K)")
        String run,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "Correo inválido")
        @Size(max = 150)
        String correo,

        // Opcional: al editar, null o vacía significa "no cambiar la contraseña"
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres")
        String password,

        Rol rol,

        @Size(max = 100)
        String region,

        @Size(max = 100)
        String comuna,

        @Size(max = 150)
        String calle,

        @Size(max = 50)
        String departamento
) {}