package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.UsuarioRequest;
import com.example.backend.exception.ConflictoException;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void crearSinPasswordLanzaExcepcion() {
        UsuarioRequest req = request("111111111", "ana@duoc.cl", null, Rol.CLIENTE);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.crear(req));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void correoDuplicadoLanzaConflicto() {
        when(usuarioRepository.existsByCorreoIgnoreCase("ana@duoc.cl")).thenReturn(true);

        UsuarioRequest req = request("111111111", "ana@duoc.cl", "clave123", Rol.CLIENTE);

        assertThrows(ConflictoException.class, () -> usuarioService.crear(req));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void laContrasenaSeGuardaHasheada() {
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.crear(request("111111111", "ana@duoc.cl", "clave123", Rol.CLIENTE));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertNotEquals("clave123", guardado.getPassword());
        assertTrue(passwordEncoder.matches("clave123", guardado.getPassword()));
    }

    @Test
    void registrarClienteFuerzaRolCliente() {
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Aunque el request pida ADMIN, el registro público siempre crea un cliente
        usuarioService.registrarCliente(request("111111111", "ana@duoc.cl", "clave123", Rol.ADMIN));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals(Rol.CLIENTE, captor.getValue().getRol());
    }

    @Test
    void actualizarNoPermiteCambiarElRun() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        UsuarioRequest req = request("209876543", "pedro@duoc.cl", null, Rol.ADMIN);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.actualizar(1L, req));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarSinPasswordConservaLaActual() {
        Usuario existente = usuarioExistente();
        String hashOriginal = existente.getPassword();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        usuarioService.actualizar(1L, request("19011022K", "pedro@duoc.cl", "", Rol.ADMIN));

        assertEquals(hashOriginal, existente.getPassword());
    }

    @Test
    void actualizarConNuevaPasswordLaRehashea() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        usuarioService.actualizar(1L, request("19011022K", "pedro@duoc.cl", "nueva1234", Rol.ADMIN));

        assertTrue(passwordEncoder.matches("nueva1234", existente.getPassword()));
    }

    @Test
    void actualizarConCorreoDeOtroUsuarioLanzaConflicto() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByCorreoIgnoreCaseAndIdNot("gamer.chile@gmail.com", 1L)).thenReturn(true);

        UsuarioRequest req = request("19011022K", "gamer.chile@gmail.com", null, Rol.ADMIN);

        assertThrows(ConflictoException.class, () -> usuarioService.actualizar(1L, req));
    }

    // ---------- Ayudantes ----------

    private UsuarioRequest request(String run, String correo, String password, Rol rol) {
        return new UsuarioRequest(run, "Ana", "Pérez", correo, password, rol,
                "Región Metropolitana de Santiago", "Maipú", "Calle 1", "");
    }

    private Usuario usuarioExistente() {
        Usuario u = new Usuario();
        u.setRun("19011022K");
        u.setNombre("Pedro");
        u.setApellidos("Hacker");
        u.setCorreo("pedro@duoc.cl");
        u.setRol(Rol.ADMIN);
        u.setPassword(passwordEncoder.encode("admin123"));
        return u;
    }
}