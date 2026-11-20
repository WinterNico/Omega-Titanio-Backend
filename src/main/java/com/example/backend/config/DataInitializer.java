package com.example.backend.config;

import com.example.backend.model.Categoria;
import com.example.backend.model.Producto;
import com.example.backend.model.Rol;
import com.example.backend.model.Usuario;
import com.example.backend.repository.CategoriaRepository;
import com.example.backend.repository.ProductoRepository;
import com.example.backend.repository.UsuarioRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

// Carga datos de ejemplo al arrancar, pero SOLO si las tablas están vacías.
// Los datos son los mismos de src/data/db.js del frontend.
@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner cargarDatos(CategoriaRepository categoriaRepo,
                                         ProductoRepository productoRepo,
                                         UsuarioRepository usuarioRepo,
                                         PasswordEncoder passwordEncoder) {
        return args -> {
            if (categoriaRepo.count() > 0) {
                return; // ya hay datos: no se vuelven a insertar
            }

            // ---- Categorías ----
            Categoria aventura = categoriaRepo.save(crearCategoria(
                    "Aventura / Acción", "aventura", "/assets/img/mhwilds2.jpg"));
            Categoria multijugador = categoriaRepo.save(crearCategoria(
                    "Multijugador / Shooter", "multijugador", "/assets/img/marvelrivals.jpg"));
            Categoria lucha = categoriaRepo.save(crearCategoria(
                    "Lucha / Anime", "lucha", "/assets/img/sparkingsueno.jpg"));
            Categoria rpg = categoriaRepo.save(crearCategoria(
                    "RPG / Mundo Abierto", "rpg", "/assets/img/cyberbug7777.jpg"));

            // ---- Productos ----
            String trailer = "https://www.youtube.com/embed/zPwK-h8RHWs";

            productoRepo.saveAll(List.of(
                    crearProducto("GAME-001", "Monster Hunter Wilds", 27990, aventura, 15, true, 20,
                            "/assets/img/mhwilds2.jpg",
                            List.of("/assets/img/ssmhw.jpg", "/assets/img/ss2mhw.jpg",
                                    "/assets/img/ss3mhw.jpg", "/assets/img/ss4mhw.jpg"),
                            trailer,
                            "Explora un mundo vivo y salvaje lleno de monstruos colosales y ecosistemas dinámicos con clima cambiante."),

                    crearProducto("GAME-002", "Marvel Rivals", 0, multijugador, 99, false, 0,
                            "/assets/img/marvelrivals.jpg",
                            List.of("/assets/img/marvelrivals.jpg", "/assets/img/GWpeEsvXcAATxfL.jpg"),
                            trailer,
                            "Hero Shooter multijugador por equipos con superhéroes de Marvel, habilidades combinadas y escenarios destructibles."),

                    crearProducto("GAME-003", "DB: Sparking! ZERO", 45990, lucha, 20, false, 0,
                            "/assets/img/sparkingsueno.jpg",
                            List.of("/assets/img/sparkingsueno.jpg"),
                            trailer,
                            "El regreso de la saga Budokai Tenkaichi con combates 3D a toda velocidad y más de 180 luchadores."),

                    crearProducto("GAME-004", "Cyberpunk 2077", 29990, rpg, 12, true, 25,
                            "/assets/img/cyberbug7777.jpg",
                            List.of("/assets/img/cyberbug7777.jpg"),
                            trailer,
                            "RPG de acción y aventura en mundo abierto ambientado en Night City, una megalópolis futurista."),

                    crearProducto("GAME-005", "Elden Ring", 38990, rpg, 8, false, 0,
                            "/assets/img/eldenrign.jpg",
                            List.of("/assets/img/eldenrign.jpg"),
                            trailer,
                            "Álzate, Sinluz, y recorre las Tierras Intermedias en un épico RPG de acción y fantasía oscura."),

                    crearProducto("GAME-006", "Hollow Knight", 8500, aventura, 3, true, 15,
                            "/assets/img/hollowknight.jpg",
                            List.of("/assets/img/hollowknight.jpg"),
                            trailer,
                            "Forja tu camino en Hallownest, una aventura clásica de acción lateral 2D a través de un reino de insectos.")
            ));

            // ---- Usuarios (contraseñas con BCrypt) ----
            usuarioRepo.saveAll(List.of(
                    crearUsuario("19011022K", "Pedro", "Hacker", "pedro@duoc.cl", "admin123",
                            Rol.ADMIN, "Los Crisantemos, Edificio Norte", "Depto 603", "Cerrillos", passwordEncoder),
                    crearUsuario("209876543", "Cliente", "Gamer", "gamer.chile@gmail.com", "gamer123",
                            Rol.CLIENTE, "Av. Pajaritos 3100", "Depto 402", "Maipú", passwordEncoder),
                    crearUsuario("111111111", "Vendedor", "Tienda", "vendedor@duoc.cl", "vende123",
                            Rol.VENDEDOR, "Av. Libertador 1000", "", "Santiago", passwordEncoder)
            ));
        };
    }

    private Categoria crearCategoria(String nombre, String slug, String imagen) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setSlug(slug);
        c.setImagen(imagen);
        return c;
    }

    private Producto crearProducto(String codigo, String nombre, Integer precio, Categoria categoria,
                                   Integer stock, Boolean oferta, Integer descuento, String imagen,
                                   List<String> capturas, String trailer, String descripcion) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setCategoria(categoria);
        p.setStock(stock);
        p.setStockCritico(5);
        p.setOferta(oferta);
        p.setDescuento(descuento);
        p.setImagen(imagen);
        p.setCapturas(new java.util.ArrayList<>(capturas));
        p.setTrailer(trailer);
        return p;
    }

    private Usuario crearUsuario(String run, String nombre, String apellidos, String correo,
                                 String passwordPlano, Rol rol, String calle, String departamento,
                                 String comuna, PasswordEncoder encoder) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setPassword(encoder.encode(passwordPlano));
        u.setRol(rol);
        u.setRegion("Región Metropolitana de Santiago");
        u.setComuna(comuna);
        u.setCalle(calle);
        u.setDepartamento(departamento);
        return u;
    }
}