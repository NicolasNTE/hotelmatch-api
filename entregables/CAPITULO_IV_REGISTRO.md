# Cap�tulo IV � Implementaci�n, validaci�n y despliegue: registro de evidencias

Estado al 11 de noviembre de 2026. Este archivo es material de trabajo para el informe; no acredita una validaci�n final ni reemplaza los anexos exigidos por el curso.

## 4.1 Software Configuration Management

Repositorio de Web Services y landing: https://github.com/NicolasNTE/hotelmatch-api. El historial local registra los PR #1 (identidad), #2 (habitaciones), #5 (reservas), #6 (recomendaci�n) y #7 (inventario), integrados en `develop`. La landing est� en `feature/landing-page`. El cliente Angular est� en el directorio hermano `hotelmatch-web`, a�n sin repositorio propio confirmado. Faltan capturas de la configuraci�n de ramas y anal�ticas de colaboraci�n.

## 4.2 Evidencias de sprints

| Apartado exigido | Evidencia disponible | Estado por completar |
|---|---|---|
| Sprint Backlog | Historias y backlog del trabajo parcial | Faltan tableros, URL y asignaci�n fechada por sprint. |
| Development Evidence | Commits y PR #1, #2, #5, #6, #7; c�digo de API y landing | Asignar commits a cada sprint con fechas reales; incorporar frontend cuando se publique. |
| Testing Suite Evidence | C�digo de pruebas en `src/test`; GitHub Actions `Verify API` ejecut� `./mvnw --batch-mode verify` correctamente en el run 43136830763 | Falta descargar el reporte detallado y asociar casos a historias por sprint. La ejecuci�n local a�n falla por dependencias inaccesibles. |
| Execution Evidence | Guion de demo y mockups | Faltan capturas de la aplicaci�n ejecut�ndose y video de navegaci�n por sprint. |
| Services Documentation | OpenAPI disponible localmente en `/swagger-ui.html` al ejecutar la API | Faltan capturas de respuestas reales y URL p�blica. |
| Software Deployment | Dockerfiles y workflow CI preparados localmente | No hay despliegue ni ejecuci�n Docker acreditados. |
| Team Collaboration Insights | Autor�as y PR visibles en Git | Faltan capturas de anal�ticas de GitHub y participaci�n en cada producto. |

Los reportes JSON/PDF, el chat, clima y frontend son trabajo local posterior. No se les asigna retroactivamente un sprint sin registro del equipo. Ver `VALIDACION_2026-11-11.md` para comandos y l�mites observados.

## 4.3 Entrevistas de validaci�n

**Dise�o.** Segmentos: viajeros nacionales y administradores de hoteles independientes. Se propone que cada participante interact�e con la landing, complete tareas pertinentes en la app y explique qu� entendi� y qu� le dificult� avanzar. Registrar consentimiento, tareas, observaciones y respuestas sin inducirlas. El enunciado exige de 3 a 5 entrevistas por segmento.

**Registro.** Al revisar este espacio de trabajo no se encontraron videos, URL de YouTube, fotogramas, tiempos ni fichas individuales de validaci�n. El archivo `REGISTRO_VALIDACIONES.csv` contiene �nicamente la estructura para capturarlos. No se deben completar resultados, porcentajes ni testimonios sin las sesiones reales.

**Evaluaci�n heur�stica.** En cada sesi�n registrar problema observable, tarea y pantalla, heur�stica de usabilidad, arquitectura de informaci�n o dise�o inclusivo, severidad, evidencia visual y mejora propuesta. Consolidar hallazgos solo despu�s de recibir las fichas o videos.

## Pendientes para cerrar el cap�tulo

1. Recibir tableros y asignaci�n de historias por sprint, adem�s de capturas y videos de ejecuci�n.
2. Obtener una ejecuci�n satisfactoria de pruebas API, build Angular y evidencia de despliegue.
3. Adjuntar 3 a 5 entrevistas por segmento con datos, capturas, URL, inicio, duraci�n y resumen individual.
4. Incorporar evaluaciones heur�sticas sustentadas en sesiones y anal�ticas de colaboraci�n por sprint.
5. Transferir este contenido al informe institucional y exportar DOCX y PDF finales.

