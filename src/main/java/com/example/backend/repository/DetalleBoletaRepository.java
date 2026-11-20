package com.example.backend.repository;

import com.example.backend.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    // Lo usa ProductoService: un producto que ya está en una orden no se puede borrar
    boolean existsByProductoId(Long productoId);
}