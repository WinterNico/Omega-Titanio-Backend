package com.example.backend.repository;

import com.example.backend.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Lo usa el login del Frente 2: el correo no distingue mayúsculas
    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByRun(String run);

    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByRun(String run);

    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    boolean existsByRunAndIdNot(String run, Long id);
}