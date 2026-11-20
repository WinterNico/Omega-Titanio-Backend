package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.ProductoRequest;
import com.example.backend.dto.ProductoResponse;
import com.example.backend.exception.ConflictoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Categoria;
import com.example.backend.model.Producto;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.DetalleBoletaRepository;
import com.example.backend.repository.ProductoRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DetalleBoletaRepository detalleBoletaRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void precioFinalAplicaDescuentoCuandoHayOferta() {
        Producto p = new Producto();
        p.setPrecio(27990);
        p.setOferta(true);
        p.setDescuento(20);

        assertEquals(22392, ProductoService.precioFinal(p));
    }

    @Test
    void precioFinalUsaPrecioNormalSinOferta() {
        Producto p = new Producto();
        p.setPrecio(27990);
        p.setOferta(false);
        p.setDescuento(20); // aunque tenga descuento guardado, sin oferta no se aplica

        assertEquals(27990, ProductoService.precioFinal(p));
    }

    @Test
    void codigoDuplicadoLanzaConflicto() {
        when(productoRepository.existsByCodigo("GAME-001")).thenReturn(true);

        ProductoRequest req = new ProductoRequest("GAME-001", "Juego", "desc", 10000, 1L, 5,
                null, false, 0, null, null, null);

        assertThrows(ConflictoException.class, () -> productoService.crear(req));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void sinOfertaFuerzaDescuentoCeroYStockCriticoPorDefecto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria()));
        when(productoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Se envía descuento 40 pero oferta false: el servicio debe ignorarlo
        ProductoRequest req = new ProductoRequest("GAME-010", "Juego", "desc", 10000, 1L, 5,
                null, false, 40, null, null, null);

        ProductoResponse resp = productoService.crear(req);

        assertEquals(0, resp.descuento());
        assertFalse(resp.oferta());
        assertEquals(5, resp.stockCritico());
        assertEquals("/assets/img/no_imagen_producto.png", resp.imagen());
    }

    @Test
    void categoriaInexistenteLanzaNoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ProductoRequest req = new ProductoRequest("GAME-011", "Juego", "desc", 10000, 99L, 5,
                null, false, 0, null, null, null);

        assertThrows(RecursoNoEncontradoException.class, () -> productoService.crear(req));
    }

    @Test
    void eliminarProductoEnOrdenesLanzaConflicto() {
        Producto producto = new Producto();
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));
        when(detalleBoletaRepository.existsByProductoId(3L)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> productoService.eliminar(3L));
        verify(productoRepository, never()).delete(any());
    }

    @Test
    void eliminarProductoSinOrdenesLoBorra() {
        Producto producto = new Producto();
        when(productoRepository.findById(3L)).thenReturn(Optional.of(producto));

        productoService.eliminar(3L);

        verify(productoRepository).delete(producto);
    }
}