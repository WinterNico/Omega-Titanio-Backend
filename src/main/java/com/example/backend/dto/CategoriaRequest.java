package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "El slug es obligatorio")
        @Size(max = 100)
        @Pattern(regexp = "^[a-z0-9-]+$", message = "El slug solo puede tener minúsculas, números y guiones")
        String slug,

        @Size(max = 255)
        String imagen
) {}