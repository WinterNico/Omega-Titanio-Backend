package com.example.backend.service;

import com.example.backend.dto.ProductoResponse;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Producto;
import com.example.backend.repository.ProductoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Filtros opcionales. Si vienen ambos, por ahora se aplica el de categoría.
    public List<ProductoResponse> listar(Long categoriaId, Boolean oferta) {
        List<Producto> productos;
        if (categoriaId != null) {
            productos = productoRepository.findByCategoriaId(categoriaId);
        } else if (Boolean.TRUE.equals(oferta)) {
            productos = productoRepository.findByOfertaTrue();
        } else {
            productos = productoRepository.findAll();
        }
        return productos.stream().map(this::toResponse).toList();
    }

    public ProductoResponse obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    private ProductoResponse toResponse(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getPrecio(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getStock(),
                p.getStockCritico(),
                p.getOferta(),
                p.getDescuento(),
                p.getImagen(),
                new ArrayList<>(p.getCapturas()), // copia: fuerza la carga de la colección dentro de la transacción
                p.getTrailer()
        );
    }
}