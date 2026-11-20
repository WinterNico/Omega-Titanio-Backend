# Omega Titanio · Backend

API REST de la tienda de videojuegos **Omega Titanio** (DSY1104 · Evaluación Parcial 3).

## Tecnologías y por qué

| Tecnología | Para qué | Por qué la elegimos |
|---|---|---|
| Spring Boot 3.5 (Java 17) | Framework del backend | Estándar del curso, arma una API REST rápido |
| Spring Data JPA + MySQL 8.4 | Persistencia | Las entidades se mapean a tablas sin escribir SQL |
| Spring Security | Autenticación y autorización | Filtra cada petición antes de llegar al controlador |
| JJWT 0.12 | Generar y validar JWT | Librería estándar para tokens firmados (HS256) |
| BCrypt | Encriptar contraseñas | Nunca se guarda la contraseña en texto plano |
| springdoc-openapi (Swagger) | Documentación y pruebas | Permite probar la API desde el navegador con el token |

## Cómo ejecutar

1. Tener MySQL corriendo con la base `omega_titanio` y el usuario `omega` / `omega123`.
2. En la carpeta del proyecto:
   ```
   mvnw.cmd spring-boot:run
   ```
3. Abrir Swagger: http://localhost:8080/swagger-ui.html

Al arrancar, `DataInitializer` carga categorías, productos y usuarios de prueba.

### Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| admin | pedro@duoc.cl | admin123 |
| vendedor | vendedor@duoc.cl | vende123 |
| cliente | gamer.chile@gmail.com | gamer123 |

## Seguridad y sesión (rama `feature/seguridad-jwt`)

### Cómo funciona el JWT

1. `POST /api/v1/auth/login` valida correo y contraseña (BCrypt) y entrega dos tokens:
   - **Access token** (15 min): se envía en cada petición como `Authorization: Bearer <token>`.
   - **Refresh token** (7 días): sirve solo para pedir tokens nuevos.
2. `JwtAuthFilter` revisa el token en cada petición, verifica la firma y la expiración, y le dice a Spring quién es el usuario y qué rol tiene.
3. Cuando el access token vence, el frontend llama a `POST /api/v1/auth/refresh` y recibe un par nuevo (**rotación**), sin volver a pedir la contraseña.
4. La API es **stateless**: el servidor no guarda sesiones, toda la información viaja firmada en el token.

### Endpoints de autenticación

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/login` | Inicia sesión y devuelve los tokens |
| POST | `/api/v1/auth/registro` | Crea un cliente y devuelve los tokens |
| POST | `/api/v1/auth/refresh` | Renueva los tokens con el refresh token |
| GET | `/api/v1/auth/me` | Datos del usuario del token |

### Permisos por rol

| Recurso | Público | Cliente | Vendedor | Admin |
|---|---|---|---|---|
| Ver productos y categorías | ✅ | ✅ | ✅ | ✅ |
| Crear/editar/eliminar productos y categorías | | | | ✅ |
| Gestionar usuarios | | | | ✅ |
| Crear órdenes (comprar) | | ✅ | | ✅ |
| Ver mis órdenes | | ✅ | ✅ | ✅ |
| Ver todas las órdenes | | | ✅ | ✅ |

- Sin token o con token inválido: **401**. Con token pero sin permiso: **403**. Ambos responden en JSON con el mismo formato de error.
- Un cliente solo puede ver **sus propias** órdenes; si pide una ajena recibe 403.

### Archivos principales

| Archivo | Qué hace |
|---|---|
| `security/JwtService.java` | Genera y valida los tokens |
| `security/JwtAuthFilter.java` | Lee el token de cada petición y autentica al usuario |
| `config/SecurityConfig.java` | Reglas de acceso por rol y respuestas 401/403 |
| `controller/AuthController.java` | Login, registro, refresh y me |

### Probar en Swagger

1. Ejecutar `POST /auth/login` y copiar el `accessToken`.
2. Clic en **Authorize** 🔒, pegar el token y confirmar.
3. Probar los endpoints: por ejemplo, `GET /usuarios` da 200 con admin y 403 con vendedor.