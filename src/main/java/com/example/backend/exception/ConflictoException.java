package com.example.backend.exception;

// Se lanza ante datos duplicados (correo, RUN, código) o stock insuficiente. El GlobalExceptionHandler lo convierte en 409.
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}