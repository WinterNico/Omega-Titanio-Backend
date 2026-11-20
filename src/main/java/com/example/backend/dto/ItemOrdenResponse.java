package com.example.backend.dto;

// "id" es el id del producto y "quantity" la cantidad, igual que el carrito del frontend
public record ItemOrdenResponse(
        Long id,
        String nombre,
        String imagen,
        Integer precioUnitario,
        Integer quantity,
        Integer subtotal
) {}