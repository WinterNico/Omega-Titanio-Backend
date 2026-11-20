package com.example.backend.service;

import com.example.backend.dto.UsuarioRequest;
import com.example.backend.dto.UsuarioResponse;
import com.example.backend.exception.ConflictoException;
import com.example.backend.exception.RecursoNoEncontradoException;
import com.example.backend.model.Boleta;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorRun(String run) {
        return usuarioRepository.findByRun(normalizarRun(run))
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con RUN " + run));
    }

    // Lo usará el login del Frente 2 para validar correo y contraseña
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(normalizarCorreo(correo));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest req) {
        exigirPassword(req);
        validarUnicos(req, null);
        Usuario usuario = new Usuario();
        aplicar(usuario, req);
        usuario.setRol(req.rol() == null ? Rol.CLIENTE : req.rol());
        usuario.setPassword(passwordEncoder.encode(req.password()));
        return toResponse(usuarioRepository.save(usuario));
    }

    // Lo usará el registro del Frente 2 (/auth/registro). El rol siempre es CLIENTE.
    @Transactional
    public UsuarioResponse registrarCliente(UsuarioRequest req) {
        exigirPassword(req);
        validarUnicos(req, null);
        Usuario usuario = new Usuario();
        aplicar(usuario, req);
        usuario.setRol(Rol.CLIENTE);
        usuario.setPassword(passwordEncoder.encode(req.password()));
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest req) {
        Usuario usuario = buscar(id);
        if (!usuario.getRun().equals(normalizarRun(req.run()))) {
            throw new IllegalArgumentException("El RUN no se puede modificar");
        }
        validarUnicos(req, id);
        aplicar(usuario, req);
        if (req.rol() != null) {
            usuario.setRol(req.rol());
        }
        // En edición, la contraseña es opcional: si viene vacía, no se cambia
        if (req.password() != null && !req.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscar(id);
        // Las órdenes se conservan (la relación usuario → boleta es opcional): solo se desvinculan
        for (Boleta boleta : usuario.getBoletas()) {
            boleta.setUsuario(null);
        }
        usuarioRepository.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado con id " + id));
    }

    private void exigirPassword(UsuarioRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria al crear un usuario");
        }
    }

    // Al crear idActual es null. Al editar, se excluye al propio usuario de la búsqueda de duplicados.
    private void validarUnicos(UsuarioRequest req, Long idActual) {
        String correo = normalizarCorreo(req.correo());
        String run = normalizarRun(req.run());

        boolean correoTomado = idActual == null
                ? usuarioRepository.existsByCorreoIgnoreCase(correo)
                : usuarioRepository.existsByCorreoIgnoreCaseAndIdNot(correo, idActual);
        if (correoTomado) {
            throw new ConflictoException("Ya existe un usuario con el correo " + correo);
        }

        boolean runTomado = idActual == null
                ? usuarioRepository.existsByRun(run)
                : usuarioRepository.existsByRunAndIdNot(run, idActual);
        if (runTomado) {
            throw new ConflictoException("Ya existe un usuario con el RUN " + run);
        }
    }

    private void aplicar(Usuario usuario, UsuarioRequest req) {
        usuario.setRun(normalizarRun(req.run()));
        usuario.setNombre(req.nombre().trim());
        usuario.setApellidos(req.apellidos().trim());
        usuario.setCorreo(normalizarCorreo(req.correo()));
        usuario.setRegion(req.region());
        usuario.setComuna(req.comuna());
        usuario.setCalle(req.calle());
        usuario.setDepartamento(req.departamento());
    }

    private String normalizarRun(String run) {
        return run.trim().toUpperCase();
    }

    private String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    private UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getRun(),
                u.getNombre(),
                u.getApellidos(),
                u.getCorreo(),
                u.getRol(),
                u.getRegion(),
                u.getComuna(),
                u.getCalle(),
                u.getDepartamento(),
                u.getFechaRegistro()
        );
    }
}