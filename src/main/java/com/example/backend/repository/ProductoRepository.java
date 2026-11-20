package com.example.backend.repository;

import com.example.backend.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring genera la consulta a partir del nombre del método: WHERE categoria_id = ?
    List<Producto> findByCategoriaId(Long categoriaId);

    // WHERE oferta = true
    List<Producto> findByOfertaTrue();

    // Lo usará ProductoService (Parte E) para evitar códigos duplicados
    boolean existsByCodigo(String codigo);
}