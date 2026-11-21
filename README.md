# Omega Titanio · Backend

API REST de la tienda de videojuegos **Omega Titanio**, desarrollada para la **Evaluación Parcial 3** de DSY1104 Desarrollo Fullstack II (Duoc UC).

Se encarga de los datos de la tienda (productos, categorías, usuarios y órdenes), los guarda en MySQL y protege cada endpoint con **JWT** y **roles**.

- **Frontend (React):** https://github.com/WinterNico/Proyecto-Semestral-Omega-Titanio
- **Documentación interactiva (Swagger):** http://localhost:8080/swagger-ui.html, con el backend corriendo

---

## Equipo

**Sección 002D** · Docente: Víctor Hugo Gómez Atabales

| Integrante | Aporte en el backend |
|---|---|
| Nicolás Morales | Backend base y datos: entidades, repositorios, servicios, controladores CRUD, validaciones, manejo de errores, datos de prueba y tests unitarios (rama `feature/backend-datos`) |
| Exequiel Romero | Seguridad y sesión: JWT (login, registro, refresh), filtro de autenticación, reglas de acceso por rol y control de dueño en las órdenes (rama `feature/seguridad-jwt`) |
| Javier Tapia | Integración del frontend con esta API (repositorio del frontend) |

---

## Tecnologías y por qué

| Tecnología | Versión | Para qué | Por qué la elegimos |
|---|---|---|---|
| Java | 17 | Lenguaje | Versión LTS que pide Spring Boot 3 |
| Spring Boot | 3.5.6 | Framework del backend | Estándar del curso; arma una API REST con muy poca configuración |
| Spring Web | — | Controladores REST | Expone los endpoints que consume React |
| Spring Data JPA + Hibernate | — | Persistencia | Las entidades se mapean a tablas sin escribir SQL a mano |
| MySQL | 8.4 | Base de datos | Base relacional; los datos quedan guardados entre reinicios |
| Bean Validation | — | Validaciones | Valida los datos de entrada con anotaciones (`@NotBlank`, `@Email`, etc.) |
| Spring Security | — | Autenticación y autorización | Filtra cada petición antes de que llegue al controlador |
| JJWT | 0.12.6 | Tokens JWT | Librería estándar para generar y validar tokens firmados (HS256) |
| BCrypt | — | Contraseñas | Las contraseñas nunca se guardan en texto plano |
| springdoc-openapi | 2.8.9 | Swagger | Documenta la API y permite probarla desde el navegador, con token |
| H2 + JUnit + Mockito | — | Tests | Los tests corren en una base en memoria, sin depender de MySQL |

---

## Cómo ejecutar

### 1. Requisitos
- **Java 17 o superior**
- **MySQL 8**. Maven no hace falta instalarlo: el proyecto trae `mvnw`.

### 2. Crear la base de datos (una sola vez)
Conectado a MySQL como `root` (en DBeaver, Workbench o la terminal):

```sql
CREATE DATABASE omega_titanio;
CREATE USER 'omega'@'localhost' IDENTIFIED BY 'omega123';
GRANT ALL PRIVILEGES ON omega_titanio.* TO 'omega'@'localhost';
```

Las tablas **no se crean a mano**: Hibernate las genera al arrancar (`ddl-auto=update`).

### 3. Levantar el backend
En la carpeta del proyecto:

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

Queda corriendo en **http://localhost:8080**. Al arrancar por primera vez, `DataInitializer` carga datos de prueba: 4 categorías, los productos del catálogo y 3 usuarios.

### 4. Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| admin | pedro@duoc.cl | admin123 |
| vendedor | vendedor@duoc.cl | vende123 |
| cliente | gamer.chile@gmail.com | gamer123 |

### 5. Correr los tests

```bash
mvnw.cmd test
```

Son 24 tests: los servicios de productos, usuarios y órdenes, más el arranque de la aplicación. Usan **H2 en memoria**, así que no tocan la base MySQL.

### Problemas comunes
| Síntoma | Solución |
|---|---|
| `Access denied for user 'omega'` | Falta el paso 2 (crear usuario y base) |
| `Field 'xxx' doesn't have a default value` | La base tiene tablas de una versión anterior: borrar y volver a crear `omega_titanio` |
| `Port 8080 already in use` | Ya hay otro backend corriendo: cerrarlo primero |

---

## Estructura del proyecto

```
src/main/java/com/example/backend/
├── config/        SecurityConfig (reglas por rol), CorsConfig, OpenApiConfig (Swagger), DataInitializer (datos de prueba)
├── controller/    Endpoints REST: Auth, Producto, Categoria, Usuario, Orden
├── dto/           Objetos de entrada (Request) y salida (Response) de la API
├── exception/     GlobalExceptionHandler y excepciones propias (404, 409)
├── model/         Entidades JPA: Producto, Categoria, Usuario, Boleta, DetalleBoleta, Rol, EstadoBoleta
├── repository/    Interfaces Spring Data JPA (acceso a la base)
├── security/      JwtService (genera y valida tokens) y JwtAuthFilter (autentica cada petición)
└── service/       Lógica de negocio: stock, precios, orden, encriptado de contraseñas
```

**Arquitectura en capas:** `Controller → Service → Repository → MySQL`. Los controladores no tocan la base directamente; reciben y devuelven **DTOs**, nunca entidades, por lo que, por ejemplo, la contraseña jamás sale en una respuesta.

### Modelo de datos

| Tabla | Contenido |
|---|---|
| `categoria` | Categorías del catálogo (nombre, slug, imagen) |
| `producto` | Juegos: código, precio, stock, stock crítico, oferta y descuento, imágenes y tráiler. Pertenece a una categoría |
| `usuario` | RUN, nombre, correo, contraseña (BCrypt), rol y dirección |
| `boleta` | La orden de compra: número, fecha, estado (`EXITOSO`/`FALLIDO`), datos de envío y total. Pertenece a un usuario |
| `detalle_boleta` | Cada producto de una orden, con su cantidad y el precio al momento de la compra |

---

## Endpoints

Base: `http://localhost:8080/api/v1`. El detalle de cada uno (cuerpos y respuestas) está en Swagger.

### Autenticación · `/auth`
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/auth/login` | público | Valida correo y contraseña y devuelve los tokens |
| POST | `/auth/registro` | público | Crea una cuenta de **cliente** y devuelve los tokens |
| POST | `/auth/refresh` | público (con refresh token) | Entrega un par de tokens nuevo |
| GET | `/auth/me` | logueado | Datos del usuario del token |

### Productos · `/productos`
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/productos` | público | Lista productos (filtros opcionales `?categoriaId=` y `?oferta=true`) |
| GET | `/productos/{id}` | público | Detalle de un producto |
| POST | `/productos` | admin | Crea un producto |
| PUT | `/productos/{id}` | admin | Edita un producto |
| DELETE | `/productos/{id}` | admin | Elimina un producto |

### Categorías · `/categorias`
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/categorias` · `/categorias/{id}` | público | Lista o detalle |
| POST · PUT · DELETE | `/categorias` · `/categorias/{id}` | admin | Crear, editar, eliminar |

### Usuarios · `/usuarios`
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/usuarios` · `/usuarios/{id}` | admin | Lista o detalle |
| GET | `/usuarios/run/{run}` | admin | Busca por RUN |
| POST | `/usuarios` | admin | Crea un usuario con cualquier rol |
| PUT | `/usuarios/{id}` | admin | Edita; si la contraseña va vacía, no se cambia |
| DELETE | `/usuarios/{id}` | admin | Elimina |

### Órdenes · `/ordenes`
| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/ordenes` | cliente, admin | Crea una compra |
| GET | `/ordenes` | admin, vendedor | Todas las órdenes |
| GET | `/ordenes/mias` | logueado | Órdenes del usuario del token |
| GET | `/ordenes/{id}` | dueño, admin, vendedor | Detalle de una orden |
| PATCH | `/ordenes/{id}/estado` | admin | Cambia el estado (EXITOSO / FALLIDO) |
| DELETE | `/ordenes/{id}` | admin | Elimina una orden |

### Reglas de negocio de las órdenes
- El cliente envía **solo el id del producto y la cantidad**. El **precio lo toma el backend** de la base de datos, aplicando el descuento si está en oferta, para que nadie pueda modificar el precio desde el navegador.
- El usuario de la orden se obtiene **del token**, no del cuerpo de la petición.
- Si no hay stock suficiente responde **409 Conflicto** y no se crea nada: toda la compra es una transacción.
- Con `"simularFallo": true` la orden queda **FALLIDO** y no descuenta stock, para demostrar el pago rechazado.
- Al cambiar una orden a FALLIDO o eliminarla, el stock se devuelve.

### Formato de errores
Todos los errores responden con la misma estructura JSON:

```json
{
  "timestamp": "2026-11-21T19:00:00",
  "status": 400,
  "error": "Datos inválidos",
  "mensaje": "Revisa los campos indicados",
  "errores": { "correo": "Correo inválido" }
}
```

| Código | Cuándo |
|---|---|
| 400 | Datos inválidos o JSON mal formado |
| 401 | Sin token, token inválido o vencido |
| 403 | Token válido, pero el rol no tiene permiso |
| 404 | El recurso no existe |
| 409 | Conflicto: correo o RUN repetido, stock insuficiente |

---

## Seguridad y sesión (JWT)

### Cómo funciona
1. `POST /auth/login` valida el correo y compara la contraseña con BCrypt. Si está bien, entrega dos tokens:
   - **Access token** (15 min): va en cada petición como `Authorization: Bearer <token>`.
   - **Refresh token** (7 días): sirve **solo** para pedir tokens nuevos.
2. `JwtAuthFilter` intercepta cada petición: verifica la firma y la expiración del token, comprueba que sea de tipo *access* y le informa a Spring Security quién es el usuario y qué rol tiene.
3. `SecurityConfig` decide, según la ruta y el rol, si la petición pasa (ver la tabla de permisos).
4. Cuando el access token vence, el frontend llama a `POST /auth/refresh` y recibe **un par nuevo** (rotación) sin pedir la contraseña otra vez. Un refresh token no sirve como access token, ni al revés.
5. La API es **stateless**: el servidor no guarda sesiones. Todo viaja firmado dentro del token, con estos claims: correo, id, rol y tipo.

### Permisos por rol

| Acción | Público | Cliente | Vendedor | Admin |
|---|:-:|:-:|:-:|:-:|
| Ver productos y categorías | ✅ | ✅ | ✅ | ✅ |
| Crear / editar / eliminar productos y categorías | | | | ✅ |
| Gestionar usuarios | | | | ✅ |
| Comprar (crear órdenes) | | ✅ | | ✅ |
| Ver sus propias órdenes | | ✅ | ✅ | ✅ |
| Ver todas las órdenes | | | ✅ | ✅ |
| Cambiar estado o eliminar órdenes | | | | ✅ |

- Un cliente que pide una orden ajena (`GET /ordenes/{id}`) recibe **403**.
- 401 y 403 responden con el mismo formato JSON de errores.
- Las restricciones también existen en el frontend (ocultar menús y rutas), pero **la protección real está aquí**: aunque alguien se salte el frontend con Postman, igual choca con Spring Security.

### Configuración (`application.properties`)

| Propiedad | Valor | Qué es |
|---|---|---|
| `app.jwt.secret` | variable de entorno `JWT_SECRET`, o un valor por defecto para desarrollo | Clave para firmar los tokens |
| `app.jwt.expiration-ms` | `900000` (15 min) | Duración del access token |
| `app.jwt.refresh-expiration-ms` | `604800000` (7 días) | Duración del refresh token |

> **Tip para la demo:** pon `app.jwt.expiration-ms=60000` (1 minuto) y en el navegador (F12 → Network) se ve la renovación automática: petición 401 → `/auth/refresh` 200 → reintento 200.

### CORS
`CorsConfig` permite peticiones desde `http://localhost:5173` (el frontend con Vite) con los headers `Authorization` y `Content-Type`.

---

## Probar en Swagger

1. Abrir http://localhost:8080/swagger-ui.html
2. Ejecutar `POST /auth/login` con un usuario de prueba y copiar el `accessToken` de la respuesta.
3. Clic en **Authorize** 🔒 (arriba a la derecha), pegar el token y confirmar.
4. Probar endpoints. Por ejemplo, `GET /usuarios`:

| Token | Resultado |
|---|---|
| sin token | 401 |
| cliente o vendedor | 403 |
| admin | 200 |
