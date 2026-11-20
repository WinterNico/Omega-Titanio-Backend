package com.example.backend.controller;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RefreshRequest;
import com.example.backend.dto.UsuarioRequest;
import com.example.backend.dto.UsuarioResponse;
import com.example.backend.model.Usuario;
import com.example.backend.security.JwtService;
import com.example.backend.service.UsuarioService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación de tokens JWT")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioService usuarioService, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Inicia sesión y entrega access token y refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto"),
            @ApiResponse(responseCode = "400", description = "Faltan datos"),
            @ApiResponse(responseCode = "401", description = "Correo o contraseña incorrectos")
    })
    @SecurityRequirements // publico, sin candado en el Swagger
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        // matches() compara la contraseña escrita con el hash BCrypt guardado
        Usuario usuario = usuarioService.buscarPorCorreo(req.correo())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPassword()))
                // Mismo mensaje si falla el correo o la clave: así no se revela que correos existen
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
        return crearRespuesta(usuario);
    }

    @Operation(summary = "Registra un cliente nuevo y deja la sesión iniciada",
            description = "El rol siempre es cliente, aunque se envíe otro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Correo o RUN ya registrado")
    })
    @SecurityRequirements
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody UsuarioRequest req) {
        UsuarioResponse creado = usuarioService.registrarCliente(req);
        Usuario usuario = usuarioService.buscarPorCorreo(creado.correo()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(crearRespuesta(usuario));
    }

    @Operation(summary = "Renueva los tokens usando el refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens nuevos"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o vencido")
    })
    @SecurityRequirements
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtService.validar(req.refreshToken());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido o vencido");
        }
        if (!JwtService.TIPO_REFRESH.equals(claims.get("tipo", String.class))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token enviado no es un refresh token");
        }
        // Se vuelve a leer el usuario de la BD: si lo borraron o le cambiaron el rol, se refleja al instante
        Usuario usuario = usuarioService.buscarPorCorreo(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El usuario ya no existe"));
        return crearRespuesta(usuario); // también entrega un refresh token nuevo
    }

    @Operation(summary = "Datos del usuario de la sesión actual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario autenticado"),
            @ApiResponse(responseCode = "401", description = "Sin sesión o token vencido")
    })
    @GetMapping("/me")
    public UsuarioResponse me(Authentication auth) {
        return usuarioService.buscarPorCorreo(auth.getName())
                .map(u -> usuarioService.obtenerPorId(u.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El usuario ya no existe"));
    }

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(
                jwtService.generarAccessToken(usuario),
                jwtService.generarRefreshToken(usuario),
                "Bearer",
                jwtService.getDuracionAccessMs(),
                usuarioService.obtenerPorId(usuario.getId()) // DTO sin contraseña
        );
    }
}