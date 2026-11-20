package com.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProductoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 20)
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150)
        String nombre,

        @Size(max = 500)
        String descripcion,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        Integer precio,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull(message = "El stock es obligatorio")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        // Opcional: si viene null, el servicio usa 5
        @PositiveOrZero
        Integer stockCritico,

        Boolean oferta,

        @Min(value = 0, message = "El descuento debe estar entre 0 y 100")
        @Max(value = 100, message = "El descuento debe estar entre 0 y 100")
        Integer descuento,

        @Size(max = 255)
        String imagen,

        List<String> capturas,

        @Size(max = 255)
        String trailer
) {}