package com.example.backend.dto;

public record CategoriaResponse(
        Long id,
        String nombre,
        String slug,
        String imagen
) {}