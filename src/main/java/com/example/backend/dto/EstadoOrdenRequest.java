package com.example.backend.dto;

import com.example.backend.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

// Cuerpo de PATCH /api/v1/ordenes/{id}/estado
public record EstadoOrdenRequest(
        @NotNull(message = "El estado es obligatorio")
        EstadoBoleta estado
) {}