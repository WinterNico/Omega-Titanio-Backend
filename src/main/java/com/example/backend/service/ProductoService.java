package com.example.backend.service;

import com.example.backend.dto.ProductoRequest;
import com.example.backend.dto.ProductoResponse;
import com.example.backend.exception.ConflictoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Categoria;
import com.example.backend.model.Producto;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.DetalleBoletaRepository;
import com.example.backend.repository.ProductoRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private static final String IMAGEN_POR_DEFECTO = "/assets/img/no_imagen_producto.png";
    private static final String TRAILER_POR_DEFECTO = "https://www.youtube.com/embed/zPwK-h8RHWs";

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleBoletaRepository detalleBoletaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           DetalleBoletaRepository detalleBoletaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.detalleBoletaRepository = detalleBoletaRepository;
    }

    // Misma fórmula que el frontend: precio con descuento redondeado al entero más cercano.
    // Es pública y estática para que OrdenService use exactamente la misma regla.
    public static int precioFinal(Producto p) {
        if (Boolean.TRUE.equals(p.getOferta()) && p.getDescuento() != null && p.getDescuento() > 0) {
            return (int) Math.round(p.getPrecio() * (1 - p.getDescuento() / 100.0));
        }
        return p.getPrecio();
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        if (productoRepository.existsByCodigo(req.codigo())) {
            throw new ConflictoException("Ya existe un producto con el código " + req.codigo());
        }
        Producto producto = new Producto();
        aplicar(producto, req, buscarCategoria(req.categoriaId()));
        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto producto = buscar(id);
        if (productoRepository.existsByCodigoAndIdNot(req.codigo(), id)) {
            throw new ConflictoException("Ya existe otro producto con el código " + req.codigo());
        }
        aplicar(producto, req, buscarCategoria(req.categoriaId()));
        return toResponse(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto producto = buscar(id);
        if (detalleBoletaRepository.existsByProductoId(id)) {
            throw new ConflictoException("No se puede eliminar: el producto aparece en órdenes registradas");
        }
        productoRepository.delete(producto);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado con id " + id));
    }

    private Categoria buscarCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada con id " + categoriaId));
    }

    private void aplicar(Producto p, ProductoRequest req, Categoria categoria) {
        boolean enOferta = Boolean.TRUE.equals(req.oferta());
        p.setCodigo(req.codigo().trim());
        p.setNombre(req.nombre().trim());
        p.setDescripcion(req.descripcion() == null ? "" : req.descripcion().trim());
        p.setPrecio(req.precio());
        p.setStock(req.stock());
        p.setStockCritico(req.stockCritico() == null ? 5 : req.stockCritico());
        p.setOferta(enOferta);
        // Regla de negocio: sin oferta no puede haber descuento
        p.setDescuento(enOferta && req.descuento() != null ? req.descuento() : 0);
        p.setImagen(req.imagen() == null || req.imagen().isBlank() ? IMAGEN_POR_DEFECTO : req.imagen().trim());
        p.setCapturas(req.capturas() == null ? new ArrayList<>() : new ArrayList<>(req.capturas()));
        p.setTrailer(req.trailer() == null || req.trailer().isBlank() ? TRAILER_POR_DEFECTO : req.trailer().trim());
        p.setCategoria(categoria);
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
                new ArrayList<>(p.getCapturas()),
                p.getTrailer()
        );
    }
}