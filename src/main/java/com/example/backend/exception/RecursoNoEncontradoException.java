package com.example.backend.exception;

// Se lanza cuando un recurso no existe (producto, usuario, orden, etc.). El GlobalExceptionHandler lo convierte en 404.
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}