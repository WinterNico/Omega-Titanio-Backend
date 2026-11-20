package com.example.backend.repository;

import com.example.backend.model.Categoria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Al crear: ¿el nombre o slug ya existe?
    boolean existsByNombre(String nombre);
    boolean existsBySlug(String slug);

    // Al editar: ¿existe otra categoría (distinta de esta) con ese nombre o slug?
    boolean existsByNombreAndIdNot(String nombre, Long id);
    boolean existsBySlugAndIdNot(String slug, Long id);

    Optional<Categoria> findBySlug(String slug);
}