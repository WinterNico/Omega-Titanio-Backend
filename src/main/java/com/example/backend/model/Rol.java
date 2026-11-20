package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE;

    // En JSON se envía en minúsculas ("admin"), porque el frontend compara rol === 'admin'
    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Rol fromJson(String valor) {
        return Rol.valueOf(valor.trim().toUpperCase());
    }
}