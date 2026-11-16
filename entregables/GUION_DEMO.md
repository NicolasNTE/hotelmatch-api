# Guion de demostración: HotelMatch API (5 minutos)

## Preparación (hazla hoy, no mañana)

El proyecto en tu computador está ahora en la rama `feature/iam-security`, que solo tiene tu parte. Para la demo
necesitas el código completo, que hoy está en la rama `prototipo-completo`.

1. En la terminal de IntelliJ (carpeta `hotelmatch-api`):

```bash
git checkout prototipo-completo
```

(Si sale "Deletion of directory ... failed", responde `n`.)

2. Ejecuta la aplicación con el perfil **`h2`**: abre `HotelMatchApplication`, ve a
   **Edit Configurations > Active profiles** y escribe `h2` (reemplaza `local` si lo tenías).
   Usa `h2` para la exposición: arranca con datos limpios cada vez, no depende de PostgreSQL y no tiene reservas
   viejas que estorben. Si prefieres mostrar PostgreSQL real, usa `local`.
3. Ejecuta y espera el mensaje `Started HotelMatchApplication` y la línea `Datos de demostración cargados`.
4. Abre `http://localhost:8080/swagger-ui.html`.
5. Haz un ensayo completo una vez. Al reiniciar con `h2`, todo vuelve a empezar de cero.

Datos de demostración (solo para la demo):

| Rol | Correo | Clave |
|---|---|---|
| Huésped | `guest@hotelmatch.dev` | `HotelMatch2026!` |
| Administrador | `admin@hotelmatch.dev` | `HotelMatch2026!` |

## Qué decir al empezar (30 segundos)

"HotelMatch recomienda habitaciones según el perfil de viaje del huésped, con un porcentaje de compatibilidad.
Este es el backend: una API REST con Spring Boot, PostgreSQL, seguridad con JWT y documentación en Swagger.
Voy a mostrar el flujo completo: ingresar, pedir una recomendación, reservar y cancelar."

## Paso 1. Iniciar sesión (huésped)

En Swagger: **Auth > POST /api/v1/auth/login > Try it out**. Cuerpo:

```json
{
  "email": "guest@hotelmatch.dev",
  "password": "HotelMatch2026!"
}
```

Pulsa **Execute**. Copia el valor de `accessToken` (solo el texto largo, sin comillas).
Sube al botón **Authorize** (arriba a la derecha), pega el token y pulsa **Authorize** y luego **Close**.

Qué decir: "La API usa un token JWT firmado. Sin él, los endpoints protegidos responden 401."

## Paso 2. Ver las habitaciones

**Rooms > GET /api/v1/rooms > Execute**. Salen las 8 habitaciones del hotel de demostración.

## Paso 3. Pedir la recomendación (el punto fuerte)

**Recommendations > POST /api/v1/recommendations > Try it out**. Cambia las fechas a unas que sean posteriores
a hoy, con al menos 3 días de anticipación:

```json
{
  "checkIn": "2026-10-20",
  "checkOut": "2026-10-22",
  "guests": 2,
  "maxBudgetPerNight": 400,
  "purpose": "COUPLE",
  "priorities": ["PRIVACY", "VIEW", "QUIET"]
}
```

Execute. Muestra:
- El ranking ordenado por `compatibility` (porcentaje).
- `breakdown`: el desglose por criterio, con `met` verdadero o falso.
- `explanation`: la explicación en texto de por qué esa habitación.

Qué decir: "El puntaje es determinista, no es una caja negra: cada criterio tiene un peso según el propósito
del viaje y las prioridades que eligió el huésped. Las habitaciones fuera del presupuesto se excluyen."

## Paso 4. Reservar la mejor opción

**Bookings > POST /api/v1/bookings**. Usa el `roomId` de la primera habitación del ranking
(cuéntalo en el JSON de respuesta del paso anterior) y las mismas fechas:

```json
{
  "roomId": 7,
  "checkIn": "2026-10-20",
  "checkOut": "2026-10-22",
  "guests": 2
}
```

Respuesta esperada: **201** con `status: CONFIRMED`, un `code` como `HM-XXXXXXXX` y el `totalAmount`.

## Paso 5. Probar la regla anti doble reserva

Pulsa **Execute** otra vez con el mismo cuerpo. Respuesta esperada: **409** con
`code: booking.roomUnavailable`.

Qué decir: "La habitación se bloquea con un candado a nivel de base de datos, así dos personas no pueden
reservar lo mismo a la vez."

## Paso 6. Volver a pedir el ranking

Repite el paso 3 con las mismas fechas. La habitación reservada **ya no aparece**.

## Paso 7. Cancelar la reserva

**Bookings > DELETE /api/v1/bookings/{id}**, con el `id` de la reserva (por ejemplo `1`).
Respuesta esperada: **200** con `status: CANCELLED`. Solo se puede cancelar con más de 48 horas de anticipación.
Si repites el ranking, la habitación vuelve a aparecer.

## Paso 8 (opcional). Roles

Cierra sesión con **Authorize > Logout**, inicia sesión con `admin@hotelmatch.dev` y vuelve a autorizar.
Muestra que el administrador puede crear habitaciones (`POST /api/v1/rooms`), mientras que el huésped recibe **403**.

## Cierre y pruebas automáticas (30 segundos)

Para mostrar calidad, ejecuta en la terminal de IntelliJ:

```bash
.\mvnw.cmd test
```

Debe terminar con `BUILD SUCCESS` y **30 pruebas pasadas**. Esto tarda cerca de un minuto, así que corre el comando
antes de la exposición y muestra la captura si no hay tiempo.

## Frases clave para las preguntas

- **Stack:** Spring Boot 3.5, Java 17, PostgreSQL, JWT, OpenAPI/Swagger.
- **Metodología:** GitFlow con ramas `feature/*`, Pull Requests a `develop`, Conventional Commits.
- **Trabajo en equipo:** cada integrante subió su parte (seguridad, habitaciones, inventario, reservas,
  recomendación) desde su propia rama.
- **IA:** el cálculo de compatibilidad es determinista; la IA está prevista solo para interpretar texto libre y
  redactar explicaciones, como recomendó el informe TB1.
- **Qué falta:** el frontend en Angular, la landing page y el panel de reportes.

## Si algo falla en vivo

| Problema | Solución |
|---|---|
| 401 en un endpoint | El token no está puesto o expiró (dura 2 horas). Repite el login y **Authorize**. |
| 422 o 400 en recomendación | Revisa que las fechas sean futuras y `checkOut` sea posterior a `checkIn`. |
| El ranking sale vacío | El presupuesto es muy bajo o ya reservaste todo. Sube `maxBudgetPerNight` o reinicia la app. |
| La app no arranca | Comprueba que el puerto 8080 esté libre y que el perfil activo sea `h2`. |
| Quedó todo en un estado raro | Detén la app y vuelve a ejecutarla: con `h2` los datos se reinician. |
