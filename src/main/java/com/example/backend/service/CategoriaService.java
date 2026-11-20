package com.example.backend.service;

import com.example.backend.dto.CategoriaResponse;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Categoria;
import com.example.backend.repository.CategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoriaResponse obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + id));
    }

    // Convierte la entidad al DTO: la respuesta nunca expone la entidad directamente
    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getSlug(), c.getImagen());
    }
}