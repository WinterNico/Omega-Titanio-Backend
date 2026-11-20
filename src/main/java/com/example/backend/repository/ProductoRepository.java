package com.example.backend.repository;

import com.example.backend.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // WHERE categoria_id = ?
    List<Producto> findByCategoriaId(Long categoriaId);

    // WHERE oferta = true
    List<Producto> findByOfertaTrue();

    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);

    // Lo usa CategoriaService para no borrar una categoría que tiene productos
    boolean existsByCategoriaId(Long categoriaId);
}