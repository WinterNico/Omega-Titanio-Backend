package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.backend.dto.DatosEnvioDto;
import com.example.backend.dto.ItemRequest;
import com.example.backend.dto.OrdenRequest;
import com.example.backend.dto.OrdenResponse;
import com.example.backend.exception.ConflictoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Boleta;
import com.example.backend.model.Categoria;
import com.example.backend.model.DetalleBoleta;
import com.example.backend.model.EstadoBoleta;
import com.example.backend.model.Producto;
import com.example.backend.repository.BoletaRepository;
import com.example.backend.repository.ProductoRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private OrdenService ordenService;

    private Producto monsterHunter; // 27990 con 20% de oferta = 22392
    private Producto hollowKnight;  // 8500 con 15% de oferta = 7225
    private Producto eldenRing;     // 38990 sin oferta

    @BeforeEach
    void setUp() {
        monsterHunter = producto("Monster Hunter Wilds", 27990, true, 20, 15);
        hollowKnight = producto("Hollow Knight", 8500, true, 15, 3);
        eldenRing = producto("Elden Ring", 38990, false, 0, 8);
    }

    @Test
    void calculaTotalConDescuentoYDescuentaStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(monsterHunter));
        when(productoRepository.findById(6L)).thenReturn(Optional.of(hollowKnight));
        when(boletaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenRequest req = new OrdenRequest(cliente(),
                List.of(new ItemRequest(1L, 2), new ItemRequest(6L, 1)), false);

        OrdenResponse resp = ordenService.crearOrden(req, null);

        assertEquals(EstadoBoleta.EXITOSO, resp.estado());
        assertEquals(22392, resp.items().get(0).precioUnitario());
        assertEquals(44784, resp.items().get(0).subtotal());
        assertEquals(7225, resp.items().get(1).precioUnitario());
        assertEquals(52009, resp.total());
        assertEquals(13, monsterHunter.getStock()); // 15 - 2
        assertEquals(2, hollowKnight.getStock());   // 3 - 1
    }

    @Test
    void usaPrecioNormalCuandoNoHayOferta() {
        when(productoRepository.findById(5L)).thenReturn(Optional.of(eldenRing));
        when(boletaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenResponse resp = ordenService.crearOrden(
                new OrdenRequest(cliente(), List.of(new ItemRequest(5L, 1)), false), null);

        assertEquals(38990, resp.total());
    }

    @Test
    void stockInsuficienteLanzaConflictoYNoGuardaNada() {
        when(productoRepository.findById(6L)).thenReturn(Optional.of(hollowKnight)); // stock 3

        OrdenRequest req = new OrdenRequest(cliente(), List.of(new ItemRequest(6L, 10)), false);

        assertThrows(ConflictoException.class, () -> ordenService.crearOrden(req, null));
        assertEquals(3, hollowKnight.getStock());
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void sumaLasCantidadesDeUnMismoProductoAntesDeValidarStock() {
        when(productoRepository.findById(6L)).thenReturn(Optional.of(hollowKnight)); // stock 3

        // Cada item pide 2 (cabe en 3), pero juntos piden 4
        OrdenRequest req = new OrdenRequest(cliente(),
                List.of(new ItemRequest(6L, 2), new ItemRequest(6L, 2)), false);

        assertThrows(ConflictoException.class, () -> ordenService.crearOrden(req, null));
        assertEquals(3, hollowKnight.getStock());
    }

    @Test
    void simularFalloGuardaFallidoSinTocarStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(monsterHunter));
        when(boletaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrdenResponse resp = ordenService.crearOrden(
                new OrdenRequest(cliente(), List.of(new ItemRequest(1L, 2)), true), null);

        assertEquals(EstadoBoleta.FALLIDO, resp.estado());
        assertEquals(15, monsterHunter.getStock());
    }

    @Test
    void ordenSinItemsLanzaExcepcion() {
        OrdenRequest req = new OrdenRequest(cliente(), List.of(), false);

        assertThrows(IllegalArgumentException.class, () -> ordenService.crearOrden(req, null));
        verifyNoInteractions(productoRepository, boletaRepository);
    }

    @Test
    void productoInexistenteLanzaNoEncontrado() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        OrdenRequest req = new OrdenRequest(cliente(), List.of(new ItemRequest(99L, 1)), false);

        assertThrows(RecursoNoEncontradoException.class, () -> ordenService.crearOrden(req, null));
    }

    @Test
    void eliminarOrdenExitosaDevuelveElStock() {
        monsterHunter.setStock(13); // ya descontado por la compra

        Boleta boleta = new Boleta();
        boleta.setEstado(EstadoBoleta.EXITOSO);
        DetalleBoleta detalle = new DetalleBoleta();
        detalle.setProducto(monsterHunter);
        detalle.setCantidad(2);
        boleta.agregarDetalle(detalle);

        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));

        ordenService.eliminar(1L);

        assertEquals(15, monsterHunter.getStock());
        verify(boletaRepository).delete(boleta);
    }

    // ---------- Ayudantes ----------

    private Producto producto(String nombre, int precio, boolean oferta, int descuento, int stock) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setOferta(oferta);
        p.setDescuento(descuento);
        p.setStock(stock);
        p.setStockCritico(5);
        p.setImagen("/assets/img/test.jpg");
        p.setCategoria(new Categoria());
        return p;
    }

    private DatosEnvioDto cliente() {
        return new DatosEnvioDto("Cliente", "Gamer", "gamer.chile@gmail.com",
                "Av. Pajaritos 3100", "Depto 402", "Región Metropolitana de Santiago", "Maipú", "");
    }
}