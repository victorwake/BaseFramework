# Documentación de la API — BaseFramework

API REST segura con autenticación JWT, RBAC, refresh tokens, rate limiting y Swagger.

---

## Índice

1. [Requisitos](#1-requisitos)
2. [Inicio rápido](#2-inicio-rápido)
3. [Perfiles de ejecución](#3-perfiles-de-ejecución)
4. [Swagger / OpenAPI](#4-swagger--openapi)
5. [Autenticación (JWT + Refresh Token)](#5-autenticación-jwt--refresh-token)
6. [Endpoints](#6-endpoints)
   - [Auth](#61-auth)
   - [Usuarios](#62-usuarios)
   - [Roles](#63-roles)
   - [Permisos](#64-permisos)
   - [Módulos](#65-módulos)
7. [DTOs — Estructura de datos](#7-dtos--estructura-de-datos)
8. [RBAC — Control de acceso por roles](#8-rbac--control-de-acceso-por-roles)
9. [Rate Limiting](#9-rate-limiting)
10. [Manejo de errores](#10-manejo-de-errores)
11. [Seed Data — Datos iniciales](#11-seed-data--datos-iniciales)
12. [Recomendaciones adicionales](#12-recomendaciones-adicionales)

---

## 1. Requisitos

| Requisito | Versión |
|-----------|---------|
| Java | 17+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |

---

## 2. Inicio rápido

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd base_framework

# 2. Crear la base de datos en MySQL
mysql -u root -e "CREATE DATABASE IF NOT EXISTS base_framework_db;"

# 3. Configurar credenciales en application.properties
#    Editar src/main/resources/application.properties:
#    spring.datasource.username=root
#    spring.datasource.password=tu_password

# 4. Compilar y ejecutar
./mvnw spring-boot:run
```

La API arranca en `http://localhost:8080`.

Al iniciar por primera vez, se ejecuta el **DataInitializer** que crea automáticamente módulos, permisos, roles y un usuario administrador por defecto:

| Campo | Valor |
|-------|-------|
| Email | `admin@email.com` |
| Contraseña | `admin123` |
| Rol | `ROLE_ADMIN` (todos los permisos) |

---

## 3. Perfiles de ejecución

El proyecto tiene 3 perfiles Spring:

### `default` (development local)

Se usa cuando no se especifica ningún perfil. Lee de `application.properties`.

```bash
./mvnw spring-boot:run
```

Características:
- `ddl-auto=update` — Hibernate crea/actualiza tablas automáticamente
- `show-sql=true` — Muestra las consultas SQL en consola
- `jwt.expiration=86400000` (24 horas)
- Pool Hikari: max 10, min 5

### `dev`

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# o
java -jar target/base_framework.jar --spring.profiles.active=dev
```

Características: mismas que `default` pero en formato YAML.

### `prod`

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
# o
java -jar target/base_framework.jar --spring.profiles.active=prod
```

Características:
- `ddl-auto=validate` — Solo valida el schema, no lo modifica
- `show-sql=false` — No expone consultas en logs
- `jwt.expiration=3600000` (1 hora, más seguro)
- Pool Hikari: max 20, min 10
- **Variables de entorno requeridas:**

| Variable | Descripción |
|----------|-------------|
| `DB_USERNAME` | Usuario de base de datos |
| `DB_PASSWORD` | Contraseña de base de datos |
| `JWT_SECRET` | Clave secreta para firmar JWT |
| `CORS_ORIGINS` | Orígenes permitidos separados por coma |

---

## 4. Swagger / OpenAPI

La API está completamente documentada con Swagger/OpenAPI.

| Recurso | URL |
|---------|-----|
| Swagger UI | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| OpenAPI spec | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |

Para probar endpoints protegidos desde Swagger UI:
1. Abrir `http://localhost:8080/swagger-ui.html`
2. Hacer clic en el botón **"Authorize"**
3. Pegar el token JWT: `Bearer <token>`
4. Cerrar el diálogo y probar los endpoints

---

## 5. Autenticación (JWT + Refresh Token)

### 5.1 Flujo completo

```
                     access token (24h)              access token (24h)
  Usuario                  +                                 +
      |                refresh token (7d)                refresh token (ROTADO)
      |                     |                                 |
      v                     v                                 v
  [POST /auth/login] ──> [usar API] ──> [POST /auth/refresh] ──> [usar API]
      |                     |                                   |
      |               cuando expira                           con nuevo token
      |               el access token
      |
  body: { email, password }
  resp: { token, refreshToken }
```

### 5.2 Login

```
POST /auth/login
Content-Type: application/json

{
  "email": "admin@email.com",
  "password": "admin123"
}
```

Respuesta exitosa (200):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 5.3 Usar el Access Token

Todos los endpoints protegidos requieren el header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 5.4 Refrescar Token

Cuando el access token expira (24h en dev / 1h en prod), se usa el refresh token para obtener un nuevo par:

```
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

Respuesta (200):
```json
{
  "token": "nuevo_token_jwt...",
  "refreshToken": "nuevo-uuid..."
}
```

**Importante:** El refresh token anterior se revoca automáticamente (token rotation). Cada refresco invalida el token anterior por seguridad.

### 5.5 Obtener perfil del usuario autenticado

```
GET /auth/me
Authorization: Bearer <token>
```

Respuesta (200):
```json
{
  "id": 1,
  "name": "Admin",
  "email": "admin@email.com",
  "createdAt": "2025-01-01T00:00:00",
  "roles": [ { "id": 1, "name": "ROLE_ADMIN", "permissions": [...] } ],
  "modules": [],
  "lastModule": null
}
```

### 5.6 Cambiar contraseña

```
POST /auth/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "currentPassword": "admin123",
  "newPassword": "nuevaPass123"
}
```

Respuesta: `200 OK`

### 5.7 Algoritmo del JWT

| Propiedad | Valor |
|-----------|-------|
| Algoritmo | HS256 (HMAC-SHA256) |
| Claims | `sub` = email, `iat`, `exp` |
| Secret | Configurable via `jwt.secret` |
| Expiración | `jwt.expiration` (default 24h, en prod 1h) |
| Refresh expiración | `jwt.refresh-expiration` (default 7 días) |

---

## 6. Endpoints

### 6.1 Auth

Ruta base: `/auth`

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/auth/login` | ❌ | Iniciar sesión |
| POST | `/auth/refresh` | ❌ | Refrescar token |
| GET | `/auth/me` | ✅ | Perfil del usuario logueado |
| POST | `/auth/change-password` | ✅ | Cambiar contraseña |

### 6.2 Usuarios

Ruta base: `/api/v1/users`
Tag Swagger: "Usuarios"

| Método | Ruta | Permiso | Descripción |
|--------|------|---------|-------------|
| GET | `/api/v1/users` | `USER_READ` | Listar usuarios (paginado + búsqueda) |
| GET | `/api/v1/users/{id}` | `USER_READ` | Obtener usuario por ID |
| POST | `/api/v1/users` | `USER_CREATE` | Crear usuario (rol ROLE_USER por defecto) |
| PUT | `/api/v1/users/{id}` | `USER_UPDATE` | Actualizar nombre y email |
| PUT | `/api/v1/users/{id}/roles` | `USER_UPDATE` | Asignar roles a un usuario |
| PUT | `/api/v1/users/{id}/modules` | `USER_UPDATE` | Asignar módulos a un usuario |
| DELETE | `/api/v1/users/{id}` | `USER_DELETE` | Eliminar usuario |

**Ejemplo: Listar usuarios con paginación y búsqueda**

```
GET /api/v1/users?search=admin&page=0&size=20
Authorization: Bearer <token>
```

Respuesta:
```json
{
  "content": [ { "id": 1, "name": "Admin", "email": "admin@email.com", ... } ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1
}
```

**Parámetros de paginación:**

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `search` | String | — | Filtro por nombre o email (parcial, ignore case) |
| `page` | int | 0 | Número de página (0-indexed) |
| `size` | int | 20 | Elementos por página |

**Ejemplo: Crear usuario**

```
POST /api/v1/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Juan Pérez",
  "email": "juan@email.com",
  "password": "password123"
}
```

**Ejemplo: Asignar roles**

```
PUT /api/v1/users/1/roles
Authorization: Bearer <token>
Content-Type: application/json

{
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

**Ejemplo: Asignar módulos**

```
PUT /api/v1/users/1/modules
Authorization: Bearer <token>
Content-Type: application/json

{
  "moduleIds": [1, 2]
}
```

### 6.3 Roles

Ruta base: `/api/v1/roles`
Tag Swagger: "Roles"

| Método | Ruta | Permiso | Descripción |
|--------|------|---------|-------------|
| GET | `/api/v1/roles` | `ROLE_READ` | Listar roles (paginado + búsqueda) |
| POST | `/api/v1/roles` | `ROLE_CREATE` | Crear rol |
| PUT | `/api/v1/roles/{id}/permissions` | `ROLE_UPDATE` | Asignar permisos a un rol |
| DELETE | `/api/v1/roles/{id}` | `ROLE_DELETE` | Eliminar rol |

**Ejemplo: Listar roles**

```
GET /api/v1/roles?search=ADMIN&page=0&size=10
Authorization: Bearer <token>
```

**Ejemplo: Crear rol**

```
POST /api/v1/roles
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "ROLE_MANAGER"
}
```

**Ejemplo: Asignar permisos a un rol**

```
PUT /api/v1/roles/1/permissions
Authorization: Bearer <token>
Content-Type: application/json

[ "USER_READ", "USER_CREATE", "ROLE_READ" ]
```

### 6.4 Permisos

Ruta base: `/api/v1/permissions`
Tag Swagger: "Permisos"

| Método | Ruta | Permiso | Descripción |
|--------|------|---------|-------------|
| GET | `/api/v1/permissions` | `PERMISSION_READ` | Listar todos los permisos |
| POST | `/api/v1/permissions` | `PERMISSION_CREATE` | Crear permiso |
| DELETE | `/api/v1/permissions/{id}` | `PERMISSION_DELETE` | Eliminar permiso |

**Ejemplo: Listar permisos**

```
GET /api/v1/permissions
Authorization: Bearer <token>
```

Respuesta:
```json
[
  { "id": 1, "name": "USER_READ", "module": { "id": 1, "name": "DASHBOARD" } },
  { "id": 2, "name": "USER_CREATE", "module": { "id": 1, "name": "DASHBOARD" } },
  ...
]
```

**Ejemplo: Crear permiso**

```
POST /api/v1/permissions
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "REPORT_READ"
}
```

### 6.5 Módulos

Ruta base: `/api/v1/modules`
Tag Swagger: "Modulos"

| Método | Ruta | Permiso | Descripción |
|--------|------|---------|-------------|
| GET | `/api/v1/modules` | `MODULE_READ` | Listar todos los módulos |
| GET | `/api/v1/modules/{id}` | `MODULE_READ` | Obtener módulo por ID |
| POST | `/api/v1/modules` | `MODULE_CREATE` | Crear módulo |
| PUT | `/api/v1/modules/{id}` | `MODULE_UPDATE` | Actualizar nombre del módulo |
| DELETE | `/api/v1/modules/{id}` | `MODULE_DELETE` | Eliminar módulo |

**Ejemplo: Crear módulo**

```
POST /api/v1/modules
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "REPORTS"
}
```

**Ejemplo: Actualizar módulo**

```
PUT /api/v1/modules/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "DASHBOARD_V2"
}
```

---

## 7. DTOs — Estructura de datos

### LoginRequest

```json
{
  "email": "user@email.com",
  "password": "miPassword123"
}
```

| Campo | Tipo | Validación |
|-------|------|------------|
| email | String | `@NotBlank` `@Email` |
| password | String | `@NotBlank` |

### AuthResponse

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "uuid-string"
}
```

### RefreshTokenRequest

```json
{
  "refreshToken": "uuid-string"
}
```

### CreateUserRequest

```json
{
  "name": "Juan Pérez",
  "email": "juan@email.com",
  "password": "password123"
}
```

| Campo | Tipo | Validación |
|-------|------|------------|
| name | String | `@NotBlank` `@Size(min=2, max=100)` |
| email | String | `@NotBlank` `@Email` |
| password | String | `@NotBlank` `@Size(min=6, max=100)` |

### UpdateUserRequest

```json
{
  "name": "Juan Pérez Actualizado",
  "email": "juan.nuevo@email.com"
}
```

| Campo | Tipo | Validación |
|-------|------|------------|
| name | String | `@NotBlank` `@Size(min=2, max=100)` |
| email | String | `@NotBlank` `@Email` |

### UpdateUserRolesRequest

```json
{
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

| Campo | Tipo | Validación |
|-------|------|------------|
| roles | List<String> | `@NotEmpty` |

### UpdateUserModulesRequest

```json
{
  "moduleIds": [1, 2, 3]
}
```

| Campo | Tipo | Validación |
|-------|------|------------|
| moduleIds | List<Long> | `@NotEmpty` |

### ChangePasswordRequest

```json
{
  "currentPassword": "admin123",
  "newPassword": "nuevaPassSegura456"
}
```

| Campo | Tipo | Validación |
|-------|------|------------|
| currentPassword | String | `@NotBlank` |
| newPassword | String | `@NotBlank` `@Size(min=6, max=100)` |

---

## 8. RBAC — Control de acceso por roles

### 8.1 Modelo de seguridad

```
Usuario ────< Rol ────< Permiso ────< Módulo
  │                        │
  └────< Módulo (directo)  │
                            │
                   USER_READ ── Módulo DASHBOARD
                   USER_CREATE ── Módulo DASHBOARD
                   ROLE_READ ── Módulo RISK
                   PERMISSION_READ ── Módulo RISK
                   MODULE_READ ── Módulo RISK
```

### 8.2 Permisos existentes (seed data)

| Permiso | Módulo |
|---------|--------|
| `USER_READ` | DASHBOARD |
| `USER_CREATE` | DASHBOARD |
| `USER_UPDATE` | DASHBOARD |
| `USER_DELETE` | DASHBOARD |
| `ROLE_READ` | RISK |
| `ROLE_CREATE` | RISK |
| `ROLE_UPDATE` | RISK |
| `ROLE_DELETE` | RISK |
| `PERMISSION_READ` | RISK |
| `PERMISSION_CREATE` | RISK |
| `PERMISSION_DELETE` | RISK |
| `MODULE_READ` | RISK |
| `MODULE_CREATE` | RISK |
| `MODULE_UPDATE` | RISK |
| `MODULE_DELETE` | RISK |

### 8.3 Roles existentes

| Rol | Permisos |
|-----|----------|
| `ROLE_USER` | Ninguno |
| `ROLE_ADMIN` | Todos (15) |
| `ROLE_SUPER_ADMIN` | Todos (15) |

### 8.4 Mapeo endpoint → permiso requerido

| Endpoint | Permiso |
|----------|---------|
| `GET /api/v1/users` | `USER_READ` |
| `GET /api/v1/users/{id}` | `USER_READ` |
| `POST /api/v1/users` | `USER_CREATE` |
| `PUT /api/v1/users/{id}` | `USER_UPDATE` |
| `PUT /api/v1/users/{id}/roles` | `USER_UPDATE` |
| `PUT /api/v1/users/{id}/modules` | `USER_UPDATE` |
| `DELETE /api/v1/users/{id}` | `USER_DELETE` |
| `GET /api/v1/roles` | `ROLE_READ` |
| `POST /api/v1/roles` | `ROLE_CREATE` |
| `PUT /api/v1/roles/{id}/permissions` | `ROLE_UPDATE` |
| `DELETE /api/v1/roles/{id}` | `ROLE_DELETE` |
| `GET /api/v1/permissions` | `PERMISSION_READ` |
| `POST /api/v1/permissions` | `PERMISSION_CREATE` |
| `DELETE /api/v1/permissions/{id}` | `PERMISSION_DELETE` |
| `GET /api/v1/modules` | `MODULE_READ` |
| `GET /api/v1/modules/{id}` | `MODULE_READ` |
| `POST /api/v1/modules` | `MODULE_CREATE` |
| `PUT /api/v1/modules/{id}` | `MODULE_UPDATE` |
| `DELETE /api/v1/modules/{id}` | `MODULE_DELETE` |

Los endpoints de `/auth/**` son **públicos** (no requieren autenticación).

---

## 9. Rate Limiting

El endpoint `/auth/login` tiene rate limiting para prevenir ataques de fuerza bruta.

| Propiedad | Valor |
|-----------|-------|
| Límite | 5 intentos por IP |
| Ventana | 60 segundos |
| Código HTTP | `429 Too Many Requests` |
| Respuesta | `{"error":"Demasiados intentos. Has superado el límite de 5 intentos permitidos. Espera X segundos antes de volver a intentar."}` |
| Header | `Retry-After: X` (segundos restantes) |

Si se supera el límite, la IP queda bloqueada durante 1 minuto antes de poder reintentar.

---

## 10. Manejo de errores

### 10.1 Formato de error

Todos los errores se devuelven como `ProblemDetail` (RFC 9457):

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No static resource ...",
  "instance": "/api/v1/users/999"
}
```

### 10.2 Códigos de error

| Código | Significado | Causa común |
|--------|-------------|-------------|
| `400` | Bad Request | Datos inválidos (validación fallida) |
| `401` | Unauthorized | Token faltante, inválido o expirado |
| `403` | Forbidden | No tiene el permiso requerido |
| `404` | Not Found | Recurso no encontrado |
| `429` | Too Many Requests | Límite de login excedido |
| `500` | Internal Server Error | Error interno del servidor |

### 10.3 Errores de validación (400)

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "errors": {
    "name": "El nombre es obligatorio",
    "email": "Debe ser un email válido",
    "password": "La contraseña debe tener al menos 6 caracteres"
  }
}
```

### 10.4 Errores de autenticación (401)

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Full authentication is required to access this resource"
}
```

### 10.5 Errores de autorización (403)

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Access Denied"
}
```

---

## 11. Seed Data — Datos iniciales

Al arrancar la aplicación por primera vez (o con la BD vacía), el `DataInitializer` crea automáticamente:

**Módulos:**
- `DASHBOARD`
- `RISK`

**Permisos (15):**
- `USER_READ`, `USER_CREATE`, `USER_UPDATE`, `USER_DELETE` → Módulo DASHBOARD
- `ROLE_READ`, `ROLE_CREATE`, `ROLE_UPDATE`, `ROLE_DELETE` → Módulo RISK
- `PERMISSION_READ`, `PERMISSION_CREATE`, `PERMISSION_DELETE` → Módulo RISK
- `MODULE_READ`, `MODULE_CREATE`, `MODULE_UPDATE`, `MODULE_DELETE` → Módulo RISK

**Roles:**
- `ROLE_USER` — sin permisos
- `ROLE_ADMIN` — todos los permisos
- `ROLE_SUPER_ADMIN` — todos los permisos

**Usuario administrador por defecto:**

| Campo | Valor |
|-------|-------|
| name | `Admin` |
| email | `admin@email.com` |
| password | `admin123` |
| roles | `ROLE_ADMIN` |

> ⚠️ **Importante:** Cambiar la contraseña del admin por defecto en producción.

---

## 12. Recomendaciones adicionales

### Para desarrollo

```bash
# Ejecutar con perfil dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Ver SQL en consola (ya activo en default y dev)
spring.jpa.show-sql=true
```

### Para producción

1. Configurar variables de entorno `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGINS`
2. Usar un JWT secret de al menos 256 bits (32 caracteres)
3. Usar `ddl-auto=validate` y gestionar el schema con Flyway o Liquibase
4. Ejecutar con perfil `prod`
5. Considerar usar HTTPS (configurar en el reverse proxy o en Spring)

### Para probar rápidamente con curl

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@email.com","password":"admin123"}' \
  | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Token: $TOKEN"

# Listar usuarios
curl -s http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" | jq .

# Crear usuario
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@email.com","password":"test123"}' | jq .
```
