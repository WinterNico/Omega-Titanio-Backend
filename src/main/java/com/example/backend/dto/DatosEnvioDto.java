package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Datos del checkout. Viajan como "cliente" tanto en la petición como en la respuesta de la orden.
public record DatosEnvioDto(
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

        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 150)
        String calle,

        @Size(max = 50)
        String departamento,

        @NotBlank(message = "La región es obligatoria")
        @Size(max = 100)
        String region,

        @NotBlank(message = "La comuna es obligatoria")
        @Size(max = 100)
        String comuna,

        @Size(max = 255)
        String indicaciones
) {}