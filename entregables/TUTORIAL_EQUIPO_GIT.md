# Tutorial: cómo subir tu parte del backend de HotelMatch

Repositorio: https://github.com/NicolasNTE/hotelmatch-api

El backend ya está escrito. Cada integrante sube **su parte** desde su propia terminal, en **su propia rama**, y luego abre un Pull Request (PR) hacia `develop`.

| Parte | Integrante | Rama | Qué sube |
|---|---|---|---|
| #1 | Nicolás Navarro | `feature/iam-security` | Login, JWT, usuarios, hotel (**ya subida**) |
| #2 | JuanDiegoLoo200 | `feature/rooms-amenities` | Habitaciones y servicios |
| #3 | Nicolas Cheel (ncheel2409) | `feature/inventory` | Disponibilidad y tarifas |
| #4 | dantevergaraaguilar159-afk | `feature/booking` | Reservas |
| #5 | JoseLr422 | `feature/recommendation` | Ranking de compatibilidad y datos demo |

**Orden de merge de los PR: #2, #3, #4, #5.** Pueden preparar y subir su rama a la vez, pero cada PR se
aprueba (Merge) solo después del anterior. Así `develop` va compilando a medida que se une el código.

---

## 0. Antes de empezar (una sola vez)

1. Acepta la invitación al repositorio: llega a tu correo, o entra a
   https://github.com/NicolasNTE/hotelmatch-api/invitations
2. Instala Git (https://git-scm.com) si no lo tienes. Comprueba con `git --version`.
3. Configura tu identidad. Usa el mismo correo que tu cuenta de GitHub, así tus commits aparecen a tu nombre:

```bash
git config --global user.name "Tu Nombre"
```

```bash
git config --global user.email "tu-correo@upc.edu.pe"
```

4. Clona el repositorio (en una carpeta fuera de OneDrive es mejor, por ejemplo `C:\dev`):

```bash
git clone https://github.com/NicolasNTE/hotelmatch-api.git
```

```bash
cd hotelmatch-api
```

La primera vez que subas algo, Git abre el navegador para que inicies sesión en GitHub. Autoriza con tu cuenta.

---

## 1. Pasos que repite todo el mundo

Cambia `RAMA`, `RUTAS` y `MENSAJE` por los de tu parte (están en tu sección más abajo).

**a) Parte de `develop` actualizado:**

```bash
git checkout develop
```

```bash
git pull
```

**b) Crea tu rama:**

```bash
git checkout -b RAMA
```

**c) Trae tus archivos desde la rama `prototipo-completo`:**

```bash
git checkout origin/prototipo-completo -- RUTAS
```

**d) Revisa qué quedó preparado:**

```bash
git status
```

Solo deben aparecer archivos de tu parte. Si ves archivos de otra carpeta, avisa antes de continuar.

**e) Haz el commit y sube tu rama:**

```bash
git commit -m "MENSAJE"
```

```bash
git push -u origin RAMA
```

**f) Crea el Pull Request en GitHub:**

1. Abre `https://github.com/NicolasNTE/hotelmatch-api/pull/new/RAMA`
2. Comprueba que arriba diga **base: `develop`** y **compare: tu rama**. Si dice `main`, cámbialo a `develop`.
3. Pon el título (igual que el mensaje del commit) y una descripción corta de lo que incluye.
4. Pulsa **Create pull request**.
5. **No hagas Merge** hasta que sea tu turno (ver el orden arriba). Nicolás Navarro avisará cuándo.

---

## 2. Tu parte

### #2 JuanDiegoLoo200: habitaciones y servicios

- **RAMA:** `feature/rooms-amenities`
- **RUTAS:**
  ```
  src/main/java/pe/edu/upc/hotelmatch/hotel
  ```
- **MENSAJE:** `feat(rooms): add room and amenity management`

Comandos completos:

```bash
git checkout develop
```

```bash
git pull
```

```bash
git checkout -b feature/rooms-amenities
```

```bash
git checkout origin/prototipo-completo -- src/main/java/pe/edu/upc/hotelmatch/hotel
```

```bash
git status
```

```bash
git commit -m "feat(rooms): add room and amenity management"
```

```bash
git push -u origin feature/rooms-amenities
```

Esta carpeta incluye los archivos de hotel que ya subió la parte #1. Son idénticos, así que `git status` solo mostrará los nuevos.

### #3 Nicolas Cheel: disponibilidad y tarifas

- **RAMA:** `feature/inventory`
- **RUTAS:**
  ```
  src/main/java/pe/edu/upc/hotelmatch/inventory
  src/test/java/pe/edu/upc/hotelmatch/hotel
  ```
- **MENSAJE:** `feat(inventory): add availability and rates management`

Comandos completos:

```bash
git checkout develop
```

```bash
git pull
```

```bash
git checkout -b feature/inventory
```

```bash
git checkout origin/prototipo-completo -- src/main/java/pe/edu/upc/hotelmatch/inventory src/test/java/pe/edu/upc/hotelmatch/hotel
```

```bash
git status
```

```bash
git commit -m "feat(inventory): add availability and rates management"
```

```bash
git push -u origin feature/inventory
```

### #4 dantevergaraaguilar159-afk: reservas

- **RAMA:** `feature/booking`
- **RUTAS:**
  ```
  src/main/java/pe/edu/upc/hotelmatch/booking
  src/test/java/pe/edu/upc/hotelmatch/booking
  ```
- **MENSAJE:** `feat(booking): add booking flow with double-booking protection`

Comandos completos:

```bash
git checkout develop
```

```bash
git pull
```

```bash
git checkout -b feature/booking
```

```bash
git checkout origin/prototipo-completo -- src/main/java/pe/edu/upc/hotelmatch/booking src/test/java/pe/edu/upc/hotelmatch/booking
```

```bash
git status
```

```bash
git commit -m "feat(booking): add booking flow with double-booking protection"
```

```bash
git push -u origin feature/booking
```

### #5 JoseLr422: ranking de compatibilidad y datos demo

- **RAMA:** `feature/recommendation`
- **RUTAS:**
  ```
  src/main/java/pe/edu/upc/hotelmatch/recommendation
  src/main/java/pe/edu/upc/hotelmatch/config/DemoDataSeeder.java
  src/test/java/pe/edu/upc/hotelmatch/recommendation
  ```
- **MENSAJE:** `feat(recommendation): add compatibility ranking and demo data`

Comandos completos:

```bash
git checkout develop
```

```bash
git pull
```

```bash
git checkout -b feature/recommendation
```

```bash
git checkout origin/prototipo-completo -- src/main/java/pe/edu/upc/hotelmatch/recommendation src/main/java/pe/edu/upc/hotelmatch/config/DemoDataSeeder.java src/test/java/pe/edu/upc/hotelmatch/recommendation
```

```bash
git status
```

```bash
git commit -m "feat(recommendation): add compatibility ranking and demo data"
```

```bash
git push -u origin feature/recommendation
```

---

## 3. Si algo falla

| Mensaje | Qué hacer |
|---|---|
| `Author identity unknown` | Falta configurar `user.name` y `user.email` (paso 0.3). Hazlo y repite el commit. |
| `Deletion of directory ... failed. Should I try again?` | Escribe `n` y Enter. Es una carpeta vacía bloqueada por OneDrive o el IDE; no afecta nada. |
| `Permission denied` o `403` al hacer push | No aceptaste la invitación (paso 0.1), o iniciaste sesión con otra cuenta de GitHub. |
| `pathspec ... did not match` | Estás en la carpeta equivocada. Entra a `hotelmatch-api` con `cd` y repite. |
| `Your local changes would be overwritten` | Avisa a Nicolás antes de seguir; no uses `--force`. |

No uses `git push --force` ni subas archivos con contraseñas (`application-local.yml`, `.env`). Están ignorados.

## 4. Cuando todo esté unido
Con los 5 PR aprobados, cualquiera puede comprobar que todo compila:

```bash
git checkout develop
```

```bash
git pull
```

```bash
./mvnw test
```

En Windows con PowerShell, usa `.\mvnw.cmd test`. Debe terminar con `BUILD SUCCESS` y 30 pruebas pasadas.
