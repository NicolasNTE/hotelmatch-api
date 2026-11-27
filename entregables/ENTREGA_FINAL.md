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
| Landing page desplegada | **Hecho** | <https://nicolasnte.github.io/hotelmatch-api/> (GitHub Pages, carpeta `docs/`). |
| API desplegada | **Hecho** | <https://hotelmatch-api.vercel.app/swagger-ui.html> (Vercel, PostgreSQL en Neon). |
| Frontend (Angular + Angular Material) desplegado | **Hecho** | <https://hotelmatch-upc.vercel.app>, código en [`web/`](../web). |
| Video About-the-Product y About-the-Team | **Pendiente** | Solo lo puede grabar el equipo (con al menos un testimonio de usuario entrevistado). |
| Entrevistas de validación y evaluación heurística | **Pendiente** | 3 a 5 entrevistas por segmento, con video en YouTube. No se deben inventar. |
| Reportes / dashboard | **Hecho** | `GET /api/v1/reports` y `/reports/pdf` (solo administrador). |
| Funcionalidad de IA | **Hecho en local** | Sugerencia de prioridades desde texto libre y asistente con Ollama; en la nube funcionan por palabras clave y preguntas frecuentes. El puntaje sigue siendo determinista. |
| Servicio externo de terceros (clima / mapas) | **Hecho** | Clima de Open-Meteo y enlace a OpenStreetMap. |

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

Pruebas (27/11/2026, JDK 17): 44, sin fallos, incluidas las migraciones sobre un PostgreSQL real. Se ejecutan en
GitHub Actions junto con la compilación de la web.

## 3. Despliegue

GitHub Pages publica `docs/` desde `main`. La API y la web se publican en Vercel; ver la sección final del
[README](../README.md). Falta anotar estas URL y sus capturas en el informe (sección 4.2.X.6).

## 4. Antes de entregar: checklist del equipo

- [ ] Cada integrante tiene commits propios en la landing, el API y el frontend («Team Collaboration Insights»).
- [ ] Informe con la plantilla y fuentes institucionales; tabla de contenidos actualizada (4 niveles).
- [ ] User stories y backlog redactados en el informe (no solo capturas de la herramienta).
- [x] Términos y condiciones enlazados en el pie de la landing y de la aplicación Angular.
- [x] i18n `en_US` / `es_419` y ARIA en la landing y en la aplicación Angular (revisar con lector de pantalla).
- [ ] Nombres de archivo según la nomenclatura del enunciado (código de alumno y número de grupo).
