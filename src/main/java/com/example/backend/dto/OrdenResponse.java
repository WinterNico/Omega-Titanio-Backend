package com.example.backend.dto;

import com.example.backend.model.EstadoBoleta;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long id,
        String nroOrden,
        String codigoOrden,
        LocalDateTime fecha,
        EstadoBoleta estado,
        Long usuarioId,
        DatosEnvioDto cliente,
        List<ItemOrdenResponse> items,
        Integer total
) {}