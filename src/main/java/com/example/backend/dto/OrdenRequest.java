package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrdenRequest(
        @NotNull(message = "Los datos del cliente son obligatorios")
        @Valid
        DatosEnvioDto cliente,

        @NotEmpty(message = "La orden debe tener al menos un producto")
        @Valid
        List<ItemRequest> items,

        // true = simula un pago fallido (la orden se guarda como FALLIDO y no descuenta stock)
        boolean simularFallo
) {}