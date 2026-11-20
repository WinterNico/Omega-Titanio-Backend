# Omega Titanio Backend

Backend de la tienda Omega Titanio (DSY1104, Evaluación Parcial 3, Duoc UC).

**Tecnologías:** Spring Boot 3.5.6 · Java 17 · MySQL 8.4 · Spring Security + JWT · springdoc-openapi (Swagger) · Maven

**Frontend:** https://github.com/WinterNico/Proyecto-Semestral-Omega-Titanio

---

## 1. Requisitos e instalación de MySQL

1. Instalar **JDK 17 o superior** (`java -version` debe mostrar 17 o más).
2. Instalar **MySQL 8.4** con el **MySQL Installer for Windows** (descarga oficial desde https://dev.mysql.com/downloads/installer/). Verifica que la firma digital sea de *Oracle America, Inc.*
    - Tipo de instalación: **Server only**.
    - Puerto: **3306**. Servicio: **MySQL84**, iniciar con el sistema.
    - Contraseña de root: la que elijas (la usarás solo para administrar).
3. Crear la base y el usuario de la aplicación. En DBeaver o en la consola de MySQL, conectado como root:

   ```sql
   CREATE DATABASE IF NOT EXISTS omega_titanio CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
   CREATE USER IF NOT EXISTS 'omega'@'localhost' IDENTIFIED BY 'omega123';
   GRANT ALL PRIVILEGES ON omega_titanio.* TO 'omega'@'localhost';
   FLUSH PRIVILEGES;
   ```

4. En DBeaver, si al conectar sale el error *Public Key Retrieval is not allowed*, en **Propiedades del controlador** pon `allowPublicKeyRetrieval` en `true`.

## 2. Cómo ejecutar

Dentro de la carpeta del backend:

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

- URL base: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui.html`
- Las tablas se crean solas al arrancar (`ddl-auto=update`) y el seed carga datos de ejemplo si la base está vacía.

Para correr los tests (usan H2 en memoria, no necesitan MySQL):

```bash
mvnw.cmd test
```

## 3. Reiniciar la base de datos

Si cambió el esquema y Hibernate no puede actualizarlo, borra y recrea la base:

```sql
DROP DATABASE omega_titanio;
CREATE DATABASE omega_titanio CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

Luego vuelve a arrancar la aplicación. El seed la vuelve a llenar.

## 4. Usuarios de prueba

| Rol | Correo | Contraseña | RUN |
|---|---|---|---|
| Administrador | `pedro@duoc.cl` | `admin123` | `19011022K` |
| Cliente | `gamer.chile@gmail.com` | `gamer123` | `209876543` |
| Vendedor | `vendedor@duoc.cl` | `vende123` | `111111111` |

## 5. Contrato de la API

Base: `http://localhost:8080/api/v1`. JSON en request y response. La columna **Roles** indica quién puede usar cada ruta cuando la seguridad esté activa.

| Método | Ruta | Descripción | Roles | Responsable |
|---|---|---|---|---|
| POST | `/auth/login` | `{correo, password}` → `{accessToken, refreshToken, usuario}` | Público | Seguridad |
| POST | `/auth/registro` | Registro de cliente | Público | Seguridad |
| POST | `/auth/refresh` | `{refreshToken}` → nuevo `accessToken` | Público | Seguridad |
| GET | `/auth/me` | Usuario según el token | Autenticado | Seguridad |
| GET | `/categorias` | Lista categorías | Público | Backend |
| GET | `/categorias/{id}` | Detalle | Público | Backend |
| POST | `/categorias` | Crear | admin | Backend |
| PUT | `/categorias/{id}` | Actualizar | admin | Backend |
| DELETE | `/categorias/{id}` | Eliminar (409 si tiene productos) | admin | Backend |
| GET | `/productos` | Lista (`?categoriaId=`, `?oferta=true`) | Público | Backend |
| GET | `/productos/{id}` | Detalle | Público | Backend |
| POST | `/productos` | Crear | admin | Backend |
| PUT | `/productos/{id}` | Actualizar | admin | Backend |
| DELETE | `/productos/{id}` | Eliminar | admin | Backend |
| GET | `/usuarios` | Lista | admin | Backend |
| GET | `/usuarios/{id}` | Detalle | admin | Backend |
| GET | `/usuarios/run/{run}` | Detalle por RUN | admin | Backend |
| POST | `/usuarios` | Crear (cualquier rol) | admin | Backend |
| PUT | `/usuarios/{id}` | Actualizar | admin | Backend |
| DELETE | `/usuarios/{id}` | Eliminar | admin | Backend |
| POST | `/ordenes` | Crear compra (checkout) | cliente, admin | Backend |
| GET | `/ordenes` | Lista todas | admin, vendedor | Backend |
| GET | `/ordenes/{id}` | Detalle | admin, vendedor, dueño | Backend |
| GET | `/ordenes/mias` | Historial del usuario logueado | Autenticado | Backend + Seguridad |
| PATCH | `/ordenes/{id}/estado` | Cambiar estado | admin | Backend |
| DELETE | `/ordenes/{id}` | Eliminar (devuelve stock) | admin | Backend |

**Vendedor:** solo puede usar `GET /productos`, `GET /productos/{id}`, `GET /ordenes` y `GET /ordenes/{id}`. Todo lo demás responde 403.

### Ejemplo: crear una orden

Request `POST /api/v1/ordenes`:

```json
{
  "cliente": {
    "nombre": "Cliente",
    "apellidos": "Gamer",
    "correo": "gamer.chile@gmail.com",
    "calle": "Av. Pajaritos 3100",
    "departamento": "Depto 402",
    "region": "Región Metropolitana de Santiago",
    "comuna": "Maipú",
    "indicaciones": ""
  },
  "items": [
    { "productoId": 1, "cantidad": 2 },
    { "productoId": 6, "cantidad": 1 }
  ],
  "simularFallo": false
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "nroOrden": "2026111900001",
  "codigoOrden": "ORDER48213",
  "fecha": "2026-11-19T21:30:00",
  "estado": "EXITOSO",
  "usuarioId": 2,
  "cliente": { "...": "mismos datos de envío" },
  "items": [
    { "id": 1, "nombre": "Monster Hunter Wilds", "imagen": "/assets/img/mhwilds2.jpg", "precioUnitario": 22392, "quantity": 2, "subtotal": 44784 },
    { "id": 6, "nombre": "Hollow Knight", "imagen": "/assets/img/hollowknight.jpg", "precioUnitario": 7225, "quantity": 1, "subtotal": 7225 }
  ],
  "total": 52009
}
```

### Ejemplo: producto

Response `GET /api/v1/productos/1`:

```json
{
  "id": 1,
  "codigo": "GAME-001",
  "nombre": "Monster Hunter Wilds",
  "descripcion": "Explora un mundo vivo y salvaje...",
  "precio": 27990,
  "categoriaId": 1,
  "categoriaNombre": "Aventura / Acción",
  "stock": 15,
  "stockCritico": 5,
  "oferta": true,
  "descuento": 20,
  "imagen": "/assets/img/mhwilds2.jpg",
  "capturas": ["/assets/img/ssmhw.jpg"],
  "trailer": "https://www.youtube.com/embed/zPwK-h8RHWs"
}
```

### Errores

Todos los errores tienen esta forma:

```json
{
  "timestamp": "2026-11-19T21:30:00",
  "status": 404,
  "error": "No encontrado",
  "mensaje": "Producto no encontrado con id 999",
  "errores": null
}
```

| Código | Cuándo |
|---|---|
| 400 | Validación fallida (`errores` trae el mensaje por campo) o JSON mal formado |
| 404 | Recurso o ruta inexistente |
| 409 | Duplicado (correo, RUN, código), stock insuficiente o categoría con productos |
| 500 | Error inesperado |

## 6. Reparto de trabajo y ramas

| Frente | Responsable | Ramas |
|---|---|---|
| 1. Backend base y datos | Nicolás | `feature/backend-datos` |
| 2. Seguridad y sesión | Por definir | `feature/seguridad-jwt` |
| 3. Integración frontend | Por definir | (frontend) `feature/integracion-api` |

Flujo: cada integrante trabaja en su rama, hace commits pequeños y frecuentes, y abre un Pull Request a `main`. Antes de abrir el PR, haz `git pull origin main` en tu rama.

## 7. Convención de commits

En español, con prefijo tipo conventional commits:

- `feat: CRUD de productos con validaciones`
- `fix: descuento mal aplicado en subtotal`
- `test: pruebas de OrdenService`
- `docs: contrato de API en README`
- `refactor: ...` / `chore: ...`

## 8. Estructura de paquetes

```
com.example.backend
├── config/      → SecurityConfig (temporal), CorsConfig, OpenApiConfig, DataInitializer
├── model/       → entidades JPA y enums
├── repository/  → interfaces JpaRepository
├── dto/         → records de request y response (nunca se exponen entidades)
├── service/     → lógica de negocio
├── controller/  → @RestController bajo /api/v1
├── exception/   → GlobalExceptionHandler y excepciones propias
└── security/    → reservado para el Frente de seguridad (JWT)
```