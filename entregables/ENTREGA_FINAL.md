# Entrega final (TF, semana 15): estado y pendientes

Fuente de los requisitos: *Enunciado del trabajo final* del curso 1ASI0705 (sección «Evaluación del Trabajo Final»).
Este archivo resume qué está listo en el repositorio y qué debe completar el equipo. Se actualiza en cada entrega.

## 1. Qué exige la entrega TF

| Entregable | Estado | Dónde está / qué falta |
|---|---|---|
| Informe del proyecto (.docx y .pdf) | **Parcial** | Base: [`upc-pre-202620-1ASI0705-HotelMatch-report-TB1.docx`](upc-pre-202620-1ASI0705-HotelMatch-report-TB1.docx) y el [Hito 1 corregido (PDF)](Trabajo_Parcial_-_Hito_1_-_HotelMacth_CORREGIDO.pdf). Faltan el capítulo IV completo (sprints 1 a 4), validaciones, conclusiones finales, bibliografía APA y anexos. |
| Keynote (.pptx y .pdf) | **Parcial** | [`HotelMatch_Exposicion.pptx`](HotelMatch_Exposicion.pptx) (versión del trabajo parcial). Falta la versión final con la diapositiva del equipo (fotos, nombres y carrera) y el export a PDF. |
| Participant Performance Report (.docx y .pdf) | **Pendiente** | Lo redacta el Team Leader con la nota de 0 a 20 por integrante. No se genera automáticamente. |
| .zip con artefactos | **Pendiente** | Armarlo al final: informe, keynote, reporte de participación, videos y este repositorio. |
| Landing page desplegada | **Código listo, sin desplegar** | [`docs/`](../docs). Falta activar GitHub Pages y registrar la URL pública (ver §3). |
| API desplegada | **Pendiente** | El código está en `develop`. Falta un proveedor de nube y PostgreSQL gestionado; definir `JWT_SECRET`, `DB_*` y `ALLOWED_ORIGINS`. |
| Frontend (Angular + Angular Material) desplegado | **Pendiente** | No existe aún. El informe TB1 prevé su propio repositorio. |
| Video About-the-Product y About-the-Team | **Pendiente** | Solo lo puede grabar el equipo (con al menos un testimonio de usuario entrevistado). |
| Entrevistas de validación y evaluación heurística | **Pendiente** | 3 a 5 entrevistas por segmento, con video en YouTube. No se deben inventar. |
| Reportes / dashboard | **Pendiente** | No hay endpoint de reportes en el API (US22 pide exportar PDF). |
| Funcionalidad de IA | **Pendiente** | El puntaje actual es determinista (`CompatibilityScorer`). La interpretación de texto libre con IA y las explicaciones generadas por un modelo aún no existen. |
| Servicio externo de terceros (clima / mapas) | **Pendiente** | Previsto en US14; no implementado. |

## 2. Backend: qué ya está en `develop`

Cinco módulos integrados mediante Pull Requests (GitFlow, Conventional Commits):

| Commit | Autor | Fecha | Mensaje |
|---|---|---|---|
| `20d3f2b` | Nicolas Navarro | 2026-10-03 | chore: project skeleton with common config and error handling |
| `66ceb63` | Nicolas Navarro | 2026-10-03 | feat(iam): add authentication, JWT security and hotel management |
| `05a2b06` | Juan Diego | 2026-10-03 | feat(rooms): add room and amenity management |
| `561c872` | DanteMDG | 2026-10-03 | feat(booking): add booking flow with double-booking protection |
| `fef1f62` | JoseLr422 | 2026-10-04 | feat(recommendation): add compatibility ranking and demo data |
| `4162a0f` | Nicolas Cheel | 2026-10-04 | feat: add inventory module |

PR integrados: #1 (iam), #2 (rooms), #5 (booking), #6 (recommendation), #7 (inventory). Los endpoints están en la tabla
del [README](../README.md#endpoints-crud-del-documento-de-alcance) y la documentación interactiva sale en
`/swagger-ui.html` al ejecutar la aplicación.

Pruebas: el código tiene 30 pruebas (9 de autenticación, 7 de habitaciones, 8 de reservas y 6 del motor de
compatibilidad). **No se ejecutaron al preparar esta entrega** porque el equipo local solo tiene Java 8 y el proyecto
exige Java 17. Antes de entregar, correr `.\mvnw.cmd test` con JDK 17 y anotar el resultado real.

## 3. Landing page: cómo publicarla

1. En GitHub: **Settings > Pages > Build and deployment > Deploy from a branch**.
2. Rama `main`, carpeta `/docs`, **Save**.
3. La URL será `https://nicolasnte.github.io/hotelmatch-api/`. Anotarla en el informe (sección 4.2.X.6) con una captura.
4. Cuando el frontend y el API estén desplegados, cambiar sus URL en [`docs/assets/config.js`](../docs/assets/config.js).

> `main` aún está en el commit inicial. La landing y el backend llegan a `main` cuando `develop` se integre a `main`
> (release). Mientras tanto, Pages también puede apuntar a la rama `develop`.

## 4. Antes de entregar: checklist del equipo

- [ ] Cada integrante tiene commits propios en la landing, el API y el frontend («Team Collaboration Insights»).
- [ ] Informe con la plantilla y fuentes institucionales; tabla de contenidos actualizada (4 niveles).
- [ ] User stories y backlog redactados en el informe (no solo capturas de la herramienta).
- [ ] Términos y condiciones enlazados en el pie de la landing (listo) y de la aplicación Angular (pendiente).
- [ ] i18n `en_US` / `es_419` y ARIA en la aplicación Angular (en la landing ya están).
- [ ] Nombres de archivo según la nomenclatura del enunciado (código de alumno y número de grupo).
