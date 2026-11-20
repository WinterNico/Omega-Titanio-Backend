package com.example.backend.repository;

import com.example.backend.model.Boleta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    // Historial "Mis compras": las más recientes primero
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    // Lista de órdenes para admin y vendedor
    List<Boleta> findAllByOrderByFechaDesc();

    // Evita repetir un número de orden al generarlo
    boolean existsByNroOrden(String nroOrden);
}