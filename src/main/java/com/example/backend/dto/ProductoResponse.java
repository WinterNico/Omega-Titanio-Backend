package com.example.backend.dto;

import java.util.List;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Integer precio,
        Long categoriaId,
        String categoriaNombre,
        Integer stock,
        Integer stockCritico,
        Boolean oferta,
        Integer descuento,
        String imagen,
        List<String> capturas,
        String trailer
) {}