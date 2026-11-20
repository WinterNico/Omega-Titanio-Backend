package com.example.backend.controller;

import com.example.backend.dto.UsuarioRequest;
import com.example.backend.dto.UsuarioResponse;
import com.example.backend.service.UsuarioService;
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
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Administración de usuarios (solo admin)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Lista todos los usuarios (admin)")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Obtiene un usuario por id (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario")
    })
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Obtiene un usuario por RUN (admin). Lo usa la pantalla de edición.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un usuario con ese RUN")
    })
    @GetMapping("/run/{run}")
    public UsuarioResponse obtenerPorRun(@PathVariable String run) {
        return usuarioService.obtenerPorRun(run);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Crea un usuario de cualquier rol (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseña ausente"),
            @ApiResponse(responseCode = "409", description = "El correo o el RUN ya existen")
    })
    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest req) {
        UsuarioResponse creado = usuarioService.crear(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Actualiza un usuario (admin)",
            description = "El RUN no se puede cambiar. La contraseña es opcional: si va vacía, no cambia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o intento de cambiar el RUN"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario"),
            @ApiResponse(responseCode = "409", description = "El correo ya existe")
    })
    @PutMapping("/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return usuarioService.actualizar(id, req);
    }

    // TODO FRENTE 2: restringir a ROLE_ADMIN
    @Operation(summary = "Elimina un usuario (admin). Sus órdenes se conservan.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe el usuario")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}