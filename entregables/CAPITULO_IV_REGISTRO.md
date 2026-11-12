# Capítulo IV — Implementación, validación y despliegue: registro de evidencias

Estado al 11 de noviembre de 2026. Este archivo es material de trabajo para el informe; no acredita una validación final ni reemplaza los anexos exigidos por el curso.

## 4.1 Software Configuration Management

Repositorio de Web Services y landing: https://github.com/NicolasNTE/hotelmatch-api. El historial local registra los PR #1 (identidad), #2 (habitaciones), #5 (reservas), #6 (recomendación) y #7 (inventario), integrados en `develop`. La landing está en `feature/landing-page`. El cliente Angular está en el directorio hermano `hotelmatch-web`, aún sin repositorio propio confirmado. Faltan capturas de la configuración de ramas y analíticas de colaboración.

## 4.2 Evidencias de sprints

| Apartado exigido | Evidencia disponible | Estado por completar |
|---|---|---|
| Sprint Backlog | Historias y backlog del trabajo parcial | Faltan tableros, URL y asignación fechada por sprint. |
| Development Evidence | Commits y PR #1, #2, #5, #6, #7; código de API y landing | Asignar commits a cada sprint con fechas reales; incorporar frontend cuando se publique. |
| Testing Suite Evidence | Código de pruebas en `src/test` | `mvnw.cmd test` no terminó correctamente en este entorno; falta salida de una ejecución aprobada. |
| Execution Evidence | Guion de demo y mockups | Faltan capturas de la aplicación ejecutándose y video de navegación por sprint. |
| Services Documentation | OpenAPI disponible localmente en `/swagger-ui.html` al ejecutar la API | Faltan capturas de respuestas reales y URL pública. |
| Software Deployment | Dockerfiles y workflow CI preparados localmente | No hay despliegue ni ejecución Docker acreditados. |
| Team Collaboration Insights | Autorías y PR visibles en Git | Faltan capturas de analíticas de GitHub y participación en cada producto. |

Los reportes JSON/PDF, el chat, clima y frontend son trabajo local posterior. No se les asigna retroactivamente un sprint sin registro del equipo. Ver `VALIDACION_2026-11-11.md` para comandos y límites observados.

## 4.3 Entrevistas de validación

**Diseño.** Segmentos: viajeros nacionales y administradores de hoteles independientes. Se propone que cada participante interactúe con la landing, complete tareas pertinentes en la app y explique qué entendió y qué le dificultó avanzar. Registrar consentimiento, tareas, observaciones y respuestas sin inducirlas. El enunciado exige de 3 a 5 entrevistas por segmento.

**Registro.** Al revisar este espacio de trabajo no se encontraron videos, URL de YouTube, fotogramas, tiempos ni fichas individuales de validación. El archivo `REGISTRO_VALIDACIONES.csv` contiene únicamente la estructura para capturarlos. No se deben completar resultados, porcentajes ni testimonios sin las sesiones reales.

**Evaluación heurística.** En cada sesión registrar problema observable, tarea y pantalla, heurística de usabilidad, arquitectura de información o diseño inclusivo, severidad, evidencia visual y mejora propuesta. Consolidar hallazgos solo después de recibir las fichas o videos.

## Pendientes para cerrar el capítulo

1. Recibir tableros y asignación de historias por sprint, además de capturas y videos de ejecución.
2. Obtener una ejecución satisfactoria de pruebas API, build Angular y evidencia de despliegue.
3. Adjuntar 3 a 5 entrevistas por segmento con datos, capturas, URL, inicio, duración y resumen individual.
4. Incorporar evaluaciones heurísticas sustentadas en sesiones y analíticas de colaboración por sprint.
5. Transferir este contenido al informe institucional y exportar DOCX y PDF finales.
