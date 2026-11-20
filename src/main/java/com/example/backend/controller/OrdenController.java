package com.example.backend.controller;

import com.example.backend.dto.EstadoOrdenRequest;
import com.example.backend.dto.OrdenRequest;
import com.example.backend.dto.OrdenResponse;
import com.example.backend.model.Usuario;
import com.example.backend.service.OrdenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/ordenes")
@Tag(name = "Órdenes", description = "Compras (checkout), historial y administración de órdenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    // TODO FRENTE 2: restringir a cliente y admin. Cuando haya JWT, obtener el Usuario de la sesión.
    @Operation(summary = "Crea una orden (checkout)",
            description = "El precio de cada producto se toma de la base de datos. Si simularFallo es true, la orden queda FALLIDO y no descuenta stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orden creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o carrito vacío"),
            @ApiResponse(responseCode = "404", description = "Algún producto no existe"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente")
    })
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody OrdenRequest req) {
        Usuario usuario = null; // TODO FRENTE 2: usuario de la sesión
        OrdenResponse creada = ordenService.crearOrden(req, usuario);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    // TODO FRENTE 2: restringir a admin y vendedor
    @Operation(summary = "Lista todas las órdenes (admin, vendedor)")
    @ApiResponse(responseCode = "200", description = "Lista de órdenes, las más recientes primero")
    @GetMapping
    public List<OrdenResponse> listarTodas() {
        return ordenService.listarTodas();
    }

    // Debe ir antes de /{id}: "mias" no es un número. TODO FRENTE 2: usar el id del usuario del token.
    @Operation(summary = "Historial de compras del usuario logueado",
            description = "Requiere sesión. Mientras no exista el login del Frente 2, responde 401.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial del usuario"),
            @ApiResponse(responseCode = "401", description = "No hay sesión iniciada")
    })
    @GetMapping("/mias")
    public List<OrdenResponse> listarMias() {
        // TODO FRENTE 2: reemplazar por el id real del usuario autenticado
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión para ver tus compras");
    }

    // TODO FRENTE 2: restringir a admin, vendedor y dueño de la orden
    @Operation(summary = "Detalle de una orden")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe la orden")
    })
    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id) {
        return ordenService.obtenerPorId(id);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Cambia el estado de una orden (admin)",
            description = "Pasar a EXITOSO descuenta stock; pasar a FALLIDO lo devuelve.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe la orden"),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente para dejarla EXITOSO")
    })
    @PatchMapping("/{id}/estado")
    public OrdenResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoOrdenRequest req) {
        return ordenService.actualizarEstado(id, req.estado());
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Elimina una orden (admin)",
            description = "Si la orden era EXITOSO, devuelve el stock de sus productos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Orden eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe la orden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}