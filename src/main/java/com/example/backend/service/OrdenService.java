package com.example.backend.service;

import com.example.backend.dto.DatosEnvioDto;
import com.example.backend.dto.ItemOrdenResponse;
import com.example.backend.dto.ItemRequest;
import com.example.backend.dto.OrdenRequest;
import com.example.backend.dto.OrdenResponse;
import com.example.backend.exception.ConflictoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Boleta;
import com.example.backend.model.DatosEnvio;
import com.example.backend.model.DetalleBoleta;
import com.example.backend.model.EstadoBoleta;
import com.example.backend.model.Producto;
import com.example.backend.model.Usuario;
import com.example.backend.repository.BoletaRepository;
import com.example.backend.repository.ProductoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdenService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;

    public OrdenService(BoletaRepository boletaRepository, ProductoRepository productoRepository) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
    }

    // Todo el método es una transacción: si algo falla, no queda ninguna orden ni ningún descuento de stock a medias
    @Transactional
    public OrdenResponse crearOrden(OrdenRequest req, Usuario usuario) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener al menos un producto");
        }

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario); // null cuando la compra no tiene sesión iniciada
        boleta.setFecha(LocalDateTime.now());
        boleta.setNroOrden(generarNroOrden());
        boleta.setCodigoOrden("ORDER" + ThreadLocalRandom.current().nextInt(10000, 100000));
        boleta.setCliente(toEntidad(req.cliente()));

        int total = 0;
        for (ItemRequest item : req.items()) {
            // Nombre y precio salen de la base de datos, nunca de lo que envía el cliente
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Producto no encontrado con id " + item.productoId()));

            int precioUnitario = ProductoService.precioFinal(producto);
            int subtotal = precioUnitario * item.cantidad();

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotal);
            boleta.agregarDetalle(detalle);

            total += subtotal;
        }
        boleta.setTotal(total);

        if (req.simularFallo()) {
            // Pago rechazado: la orden queda registrada como FALLIDO y no descuenta stock
            boleta.setEstado(EstadoBoleta.FALLIDO);
        } else {
            validarStock(boleta);
            ajustarStock(boleta, -1);
            boleta.setEstado(EstadoBoleta.EXITOSO);
        }

        return toResponse(boletaRepository.save(boleta));
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return boletaRepository.findAllByOrderByFechaDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorUsuario(Long usuarioId) {
        return boletaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    // Cambiar el estado mueve el stock: a EXITOSO lo descuenta, a FALLIDO lo devuelve
    @Transactional
    public OrdenResponse actualizarEstado(Long id, EstadoBoleta nuevoEstado) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() == nuevoEstado) {
            return toResponse(boleta);
        }
        if (nuevoEstado == EstadoBoleta.EXITOSO) {
            validarStock(boleta);
            ajustarStock(boleta, -1);
        } else {
            ajustarStock(boleta, +1);
        }
        boleta.setEstado(nuevoEstado);
        return toResponse(boleta);
    }

    // Al borrar una orden exitosa se devuelve el stock de sus productos
    @Transactional
    public void eliminar(Long id) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() == EstadoBoleta.EXITOSO) {
            ajustarStock(boleta, +1);
        }
        boletaRepository.delete(boleta);
    }

    private Boleta buscar(Long id) {
        return boletaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id " + id));
    }

    // Suma cuánto pide la orden de cada producto, por si un mismo producto aparece en varios items
    private void validarStock(Boleta boleta) {
        Map<Producto, Integer> demanda = new HashMap<>();
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            demanda.merge(detalle.getProducto(), detalle.getCantidad(), Integer::sum);
        }
        demanda.forEach((producto, pedido) -> {
            if (producto.getStock() < pedido) {
                throw new ConflictoException("Stock insuficiente para " + producto.getNombre()
                        + " (disponible: " + producto.getStock() + ")");
            }
        });
    }

    // signo -1 descuenta, +1 devuelve
    private void ajustarStock(Boleta boleta, int signo) {
        for (DetalleBoleta detalle : boleta.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() + signo * detalle.getCantidad());
        }
    }

    // Formato del ejemplo del README: fecha AAAAMMDD + correlativo de 5 dígitos (ej: 2026111900001)
    private String generarNroOrden() {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long correlativo = boletaRepository.count() + 1;
        String nro = fecha + String.format("%05d", correlativo);
        while (boletaRepository.existsByNroOrden(nro)) {
            correlativo++;
            nro = fecha + String.format("%05d", correlativo);
        }
        return nro;
    }

    private DatosEnvio toEntidad(DatosEnvioDto d) {
        DatosEnvio datos = new DatosEnvio();
        datos.setNombre(d.nombre());
        datos.setApellidos(d.apellidos());
        datos.setCorreo(d.correo());
        datos.setCalle(d.calle());
        datos.setDepartamento(d.departamento());
        datos.setRegion(d.region());
        datos.setComuna(d.comuna());
        datos.setIndicaciones(d.indicaciones());
        return datos;
    }

    private OrdenResponse toResponse(Boleta b) {
        List<ItemOrdenResponse> items = b.getDetalles().stream()
                .map(d -> new ItemOrdenResponse(
                        d.getProducto().getId(),
                        d.getProducto().getNombre(),
                        d.getProducto().getImagen(),
                        d.getPrecioUnitario(),
                        d.getCantidad(),
                        d.getSubtotal()))
                .toList();

        DatosEnvio c = b.getCliente();
        DatosEnvioDto cliente = new DatosEnvioDto(
                c.getNombre(), c.getApellidos(), c.getCorreo(), c.getCalle(),
                c.getDepartamento(), c.getRegion(), c.getComuna(), c.getIndicaciones());

        Long usuarioId = b.getUsuario() == null ? null : b.getUsuario().getId();

        return new OrdenResponse(
                b.getId(),
                b.getNroOrden(),
                b.getCodigoOrden(),
                b.getFecha(),
                b.getEstado(),
                usuarioId,
                cliente,
                items,
                b.getTotal()
        );
    }
}