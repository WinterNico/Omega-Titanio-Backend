package com.example.backend.controller;

import com.example.backend.dto.ProductoRequest;
import com.example.backend.dto.ProductoResponse;
import com.example.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo público y administración de videojuegos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Lista productos",
            description = "Filtros opcionales: ?categoriaId=1 o ?oferta=true")
    @ApiResponse(responseCode = "200", description = "Lista de productos")
    @GetMapping
    public List<ProductoResponse> listar(
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean oferta) {
        return productoService.listar(categoriaId, oferta);
    }

    @Operation(summary = "Obtiene un producto por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un producto con ese id")
    })
    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Crea un producto (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "La categoría no existe"),
            @ApiResponse(responseCode = "409", description = "El código ya existe")
    })
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req) {
        ProductoResponse creado = productoService.crear(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Actualiza un producto (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe el producto o la categoría"),
            @ApiResponse(responseCode = "409", description = "El código ya existe")
    })
    @PutMapping("/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return productoService.actualizar(id, req);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Elimina un producto (admin). No se puede si aparece en órdenes.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe el producto"),
            @ApiResponse(responseCode = "409", description = "El producto aparece en órdenes registradas")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}