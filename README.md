# HotelMatch API

RESTful API de **HotelMatch**, plataforma web de recomendación de habitaciones por porcentaje de compatibilidad con el
perfil de viaje del huésped. Curso 1ASI0705 Arquitectura de Aplicaciones Web (UPC, ciclo 2026-2).

Este repositorio corresponde al **backend** (Web Services). La landing page y la aplicación web (Angular) van en
repositorios separados, como establece el informe TB1.

## Stack

| Componente | Tecnología |
|---|---|
| Lenguaje / framework | Java 17, Spring Boot 3.5 (Web, Data JPA, Validation, Security) |
| Base de datos | PostgreSQL (H2 en memoria para pruebas y demo rápida) |
| Seguridad | JWT firmado (HS256), roles `GUEST` y `ADMIN`, contraseñas con BCrypt |
| Documentación | OpenAPI 3 vía springdoc / Swagger UI |
| i18n | Mensajes en `es-419` (por defecto) y `en`, según `Accept-Language` |
| Pruebas | JUnit 5, Spring Boot Test, MockMvc |

## Cómo ejecutarlo

Requiere JDK 17 o superior. En IntelliJ IDEA: **File > Open** sobre `pom.xml` (Open as Project) y esperar la
importación de Maven. Si IntelliJ pide un SDK, elegir un JDK 17+.

### Opción A: sin instalar nada más (H2 con datos de demostración)

En IntelliJ: abrir `HotelMatchApplication`, **Edit Configurations > Active profiles = `h2`** y ejecutar.
Por consola (en Windows usar `mvnw.cmd` en lugar de `./mvnw`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

### Opción B0: PostgreSQL local con perfil `local`

Crear `src/main/resources/application-local.yml` (está en `.gitignore`, no se sube a GitHub) con la URL, el usuario y
la clave de tu PostgreSQL, y ejecutar con el perfil `local`. La base `hotelmatch` debe existir (crearla en pgAdmin).
Hibernate crea las tablas al arrancar.

### Opción B: con PostgreSQL por variables de entorno

```bash
docker compose up -d
./mvnw spring-boot:run
```

Con un PostgreSQL propio, crear la base `hotelmatch` y definir las variables de entorno `DB_URL`, `DB_USER` y
`DB_PASSWORD`. Para cargar los datos de demostración, agregar `SEED_DEMO_DATA=true`.

### Variables de entorno

| Variable | Descripción | Por defecto |
|---|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | Conexión a PostgreSQL | `localhost:5432/hotelmatch`, `hotelmatch` |
| `JWT_SECRET` | Secreto de firma (mínimo 32 caracteres). **Obligatorio definirlo al desplegar** | valor solo para desarrollo |
| `JWT_EXPIRATION_MINUTES` | Vigencia del token | `120` |
| `ALLOWED_ORIGINS` | Orígenes CORS permitidos (separados por coma) | `http://localhost:4200` |
| `SEED_DEMO_DATA` | Carga hotel, servicios, habitaciones y usuarios demo | `false` (`true` con perfil `h2`) |

## Documentación de los servicios

Con la aplicación levantada: <http://localhost:8080/swagger-ui.html> (JSON en `/v3/api-docs`).
Para probar endpoints protegidos: `POST /api/v1/auth/login`, copiar el `accessToken` y usar **Authorize**.

Usuarios de demostración (solo con datos demo; ver `DemoDataSeeder`): `admin@hotelmatch.dev` (administrador del
hotel demo) y `guest@hotelmatch.dev` (huésped), ambos con la clave definida en `DemoDataSeeder.DEMO_PASSWORD`.

## Endpoints (CRUD del documento de alcance)

| Recurso | Operaciones | Rol |
|---|---|---|
| `/api/v1/auth` | `POST /register`, `POST /login` | Público |
| `/api/v1/users/me` | `GET`, `PUT`, `DELETE` (baja lógica) | Autenticado |
| `/api/v1/hotels/{id}` | `GET`, `PUT`, `DELETE` (baja lógica, `confirm=true`) | `GET` autenticado; resto `ADMIN` del hotel |
| `/api/v1/services` | `POST`, `GET`, `PUT /{id}`, `DELETE /{id}` | `GET` autenticado; resto `ADMIN` |
| `/api/v1/rooms` | `POST`, `GET` (filtros y paginación), `GET /{id}`, `PUT /{id}`, `DELETE /{id}` (lógico, `confirm=true`) | `GET` autenticado; resto `ADMIN` del hotel |
| `/api/v1/rooms/{id}/services` | `POST`, `GET`, `PUT /{serviceId}`, `DELETE /{serviceId}` | `GET` autenticado; resto `ADMIN` |
| `/api/v1/rooms/{id}/availability` | `POST`, `GET ?from&to`, `PUT /{date}`, `DELETE /{date}` | `GET` autenticado; resto `ADMIN` |
| `/api/v1/rooms/{id}/rates` | `POST`, `GET`, `PUT /{rateId}`, `DELETE /{rateId}` | `GET` autenticado; resto `ADMIN` |
| `/api/v1/bookings` | `POST` (huésped), `GET`, `GET /{id}`, `PUT /{id}/status` (admin), `DELETE /{id}` (cancelar, huésped) | Según rol |
| `/api/v1/recommendations` | `POST` (ranking de compatibilidad) | Autenticado |

### Reglas de negocio implementadas

- Un administrador queda asociado a un único hotel, que se crea al registrarlo. Solo opera sobre su hotel (403 si no).
- Número de habitación único por hotel; precio y capacidad mayores a cero; atributos de experiencia de 0 a 5.
- Las bajas (cuenta, hotel, habitación) son lógicas y conservan el historial de reservas.
- Sin registro, un día se considera disponible; lo bloquean un registro `BLOCKED` o una reserva que lo ocupe.
- Las tarifas por fecha no pueden cruzarse; el total de la reserva suma el precio vigente de cada noche.
- Reserva: valida fechas, capacidad y disponibilidad con bloqueo de la fila de la habitación (evita dobles reservas).
  Se cancela solo con más de 48 horas de anticipación (`hotelmatch.cancellation-window-hours`).
- Recomendación: puntaje determinista 0-100 (ver `CompatibilityScorer`) con desglose por criterio y explicación en
  texto; excluye habitaciones inactivas, sin capacidad, ocupadas, bloqueadas o fuera de presupuesto.

Los errores siguen RFC 7807 (`application/problem+json`) con un campo `code` estable para el frontend.

## Pruebas

```bash
./mvnw test
```

Incluye pruebas unitarias del motor de compatibilidad y pruebas de integración (MockMvc sobre H2) de autenticación,
CRUD de habitaciones/servicios/disponibilidad/tarifas y del flujo recomendación, reserva y cancelación.

## Convenciones del equipo

- **GitFlow**: `main` (releases), `develop` (integración) y ramas `feature/<historia>-<descripcion>`.
- **Conventional Commits**: `feat(rooms): ...`, `fix(booking): ...`, `test(auth): ...`, `docs: ...`.
- Estructura por contexto: `iam`, `hotel`, `inventory`, `booking`, `recommendation`; cada uno con
  controlador, servicio, repositorio, entidades y DTOs.
