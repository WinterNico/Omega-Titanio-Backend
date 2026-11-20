package com.example.backend.service;

import com.example.backend.dto.CategoriaRequest;
import com.example.backend.dto.CategoriaResponse;
import com.example.backend.exception.ConflictoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Categoria;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest req) {
        if (categoriaRepository.existsByNombre(req.nombre())) {
            throw new ConflictoException("Ya existe una categoría llamada '" + req.nombre() + "'");
        }
        if (categoriaRepository.existsBySlug(req.slug())) {
            throw new ConflictoException("Ya existe una categoría con el slug '" + req.slug() + "'");
        }
        Categoria categoria = new Categoria();
        aplicar(categoria, req);
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria categoria = buscar(id);
        if (categoriaRepository.existsByNombreAndIdNot(req.nombre(), id)) {
            throw new ConflictoException("Ya existe otra categoría llamada '" + req.nombre() + "'");
        }
        if (categoriaRepository.existsBySlugAndIdNot(req.slug(), id)) {
            throw new ConflictoException("Ya existe otra categoría con el slug '" + req.slug() + "'");
        }
        aplicar(categoria, req);
        return toResponse(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (productoRepository.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar: la categoría tiene productos asociados");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria buscar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + id));
    }

    private void aplicar(Categoria categoria, CategoriaRequest req) {
        categoria.setNombre(req.nombre().trim());
        categoria.setSlug(req.slug().trim());
        categoria.setImagen(req.imagen());
    }

    private CategoriaResponse toResponse(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getSlug(), c.getImagen());
    }
}