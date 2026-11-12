# Evidencia de ejecución y estado técnico

## Continuación de la revisión

Las capturas de `../mockups-figma` se usaron para crear la landing pública
responsive del cliente Angular. El ranking mostrado en ella está rotulado como
ejemplo. La autenticación y el buscador real siguen conectados al flujo existente.
`ngc -p tsconfig.json --noEmit` terminó con código 0, por lo que se verificaron
TypeScript y plantillas Angular. `npm run build` no terminó: los paquetes de
`node_modules` son enlaces a otra carpeta y el empaquetador devolvió
`Access is denied` al resolverlos. Falta una instalación local limpia y la
prueba visual en navegador. Este resultado reemplaza la afirmación anterior de
que no se había compilado el cliente en absoluto.

Fecha: 11 de noviembre de 2026. Esta revisión no acredita una entrega final aprobada.

## Cambios implementados en esta revisión

Se agregó `GET /api/v1/reports?from=YYYY-MM-DD&to=YYYY-MM-DD`, restringido
al administrador autenticado y a su hotel. El intervalo es semiabierto y admite
hasta 366 días. Incluye reservas que cruzan el período, canceladas, noches
ocupadas, capacidad actual y valor proporcional de reservas confirmadas en PEN.
`GET /api/v1/reports/pdf` exporta el resumen a un PDF descargable.

El valor confirmado no demuestra pagos recibidos. La capacidad se calcula con
habitaciones activas actuales; no hay historial de inventario para reconstruir
ocupación histórica. Se añadieron tres pruebas de integración para autorización,
respuesta JSON/PDF y rango inválido; faltan casos de aislamiento entre hoteles
y cálculo con reservas parcialmente contenidas en el período.

El directorio hermano `hotelmatch-web` contiene un cliente Angular con Material,
autenticación, búsqueda, ranking, detalle, reservas, cancelación, reportes y
editor técnico de inventario. Es una implementación inicial sin compilación
verificada. Faltan formularios administrativos de negocio, traducción completa,
paginación navegable y pruebas de interacción.

Se prepararon Dockerfiles y un workflow de pruebas para el backend. No se
ejecutaron en Docker ni se publicaron en GitHub.

## Comandos y resultados reales

JDK seleccionado: `C:\Users\nicon\.jdks\graalvm-jdk-17.0.12`.

1. `mvnw.cmd test`: falla el wrapper con `Cannot index into a null array`.
2. Maven 3.9.9 instalado, modo offline, caché local explícita, `test`:
   BUILD FAILURE en testCompile al acceder a `spring-boot-test-3.5.16.jar`.
3. Mismo Maven, `compile` tras agregar reportes: BUILD FAILURE al acceder
   a `jakarta.activation-api-2.1.4.jar` en el repositorio Maven del usuario.
4. `npm install --cache .npm-cache --no-audit --no-fund --fetch-retries=0`:
   EACCES al descargar `@angular/animations` de registry.npmjs.org.

No hubo una ejecución de pruebas satisfactoria ni se conoce el número real de
pruebas aprobadas. No se realizó verificación en navegador.

## Ejecución reproducible en un entorno con acceso a dependencias

```powershell
# API, con JAVA_HOME apuntando a JDK 17
cd hotelmatch-api
.\mvnw.cmd verify
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=h2'

# Otra terminal
cd hotelmatch-web
npm install
npm run build
npm start
```

Demo local: `guest@hotelmatch.dev` y `admin@hotelmatch.dev`, contraseña
`HotelMatch2026!`. Usar estas cuentas solo con el perfil de demostración H2.
Validar: registro, login, búsqueda, detalle, reserva, segunda reserva conflictiva,
cancelación fuera y dentro de la ventana, autorización por hotel y PDF.

## Pendientes que siguen abiertos

IA para interpretar texto libre, servicio externo, migraciones Flyway y
restricción PostgreSQL de solapamiento no se implementaron en esta revisión.
No se modificaron pesos del ranking sin un conjunto de validación que lo sustente.

Los PR, merges, publicación de Pages, borrado de ramas y correos verificados
no se ejecutaron ni se acreditan aquí. Falta elegir proveedor y configurar
base de datos y secretos antes de desplegar API y frontend.

El informe institucional, keynote final, notas de participación, entrevistas,
videos y testimonios siguen pendientes. Los archivos parciales originales se
conservan. No hay evidencia suficiente para redactar sprints ejecutados,
validaciones de usuarios o notas individuales como hechos consumados.

El ZIP de esta revisión es un paquete de fuentes para revisión, no el paquete
académico final. Debe sustituirse tras completar las verificaciones y evidencias.
