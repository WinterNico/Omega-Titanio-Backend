package com.example.backend.controller;

import com.example.backend.dto.CategoriaRequest;
import com.example.backend.dto.CategoriaResponse;
import com.example.backend.service.CategoriaService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Consulta pública y administración de categorías")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(summary = "Lista todas las categorías")
    @ApiResponse(responseCode = "200", description = "Lista de categorías")
    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @Operation(summary = "Obtiene una categoría por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una categoría con ese id")
    })
    @GetMapping("/{id}")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categoriaService.obtenerPorId(id);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Crea una categoría (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Nombre o slug ya existen")
    })
    @PostMapping
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest req) {
        CategoriaResponse creada = categoriaService.crear(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Actualiza una categoría (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría"),
            @ApiResponse(responseCode = "409", description = "Nombre o slug ya existen")
    })
    @PutMapping("/{id}")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.actualizar(id, req);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Elimina una categoría (admin). No se puede si tiene productos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "404", description = "No existe la categoría"),
            @ApiResponse(responseCode = "409", description = "La categoría tiene productos asociados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}