# Evidencia de ejecuci�n y estado t�cnico

## Continuaci�n de la revisi�n

En GitHub Actions, el workflow `Verify API` del commit `965b0ad` termin� con
conclusi�n `success` (run 43136830763). El paso `./mvnw --batch-mode verify`
tambi�n concluy� correctamente. Esto acredita el build y las pruebas de ese
commit en CI; todav�a falta registrar el n�mero de casos a partir del reporte
de pruebas y probar el frontend con el backend en navegador.

Las capturas de `../mockups-figma` se usaron para crear la landing p�blica
responsive del cliente Angular. El ranking mostrado en ella est� rotulado como
ejemplo. La autenticaci�n y el buscador real siguen conectados al flujo existente.
`ngc -p tsconfig.json --noEmit` termin� con c�digo 0, por lo que se verificaron
TypeScript y plantillas Angular. `npm run build` no termin�: los paquetes de
`node_modules` son enlaces a otra carpeta y el empaquetador devolvi�
`Access is denied` al resolverlos. Falta una instalaci�n local limpia y la
prueba visual en navegador. Este resultado reemplaza la afirmaci�n anterior de
que no se hab�a compilado el cliente en absoluto.

Fecha: 11 de noviembre de 2026. Esta revisi�n no acredita una entrega final aprobada.

## Cambios implementados en esta revisi�n

Se agreg� `GET /api/v1/reports?from=YYYY-MM-DD&to=YYYY-MM-DD`, restringido
al administrador autenticado y a su hotel. El intervalo es semiabierto y admite
hasta 366 d�as. Incluye reservas que cruzan el per�odo, canceladas, noches
ocupadas, capacidad actual y valor proporcional de reservas confirmadas en PEN.
`GET /api/v1/reports/pdf` exporta el resumen a un PDF descargable.

El valor confirmado no demuestra pagos recibidos. La capacidad se calcula con
habitaciones activas actuales; no hay historial de inventario para reconstruir
ocupaci�n hist�rica. Se a�adieron tres pruebas de integraci�n para autorizaci�n,
respuesta JSON/PDF y rango inv�lido; faltan casos de aislamiento entre hoteles
y c�lculo con reservas parcialmente contenidas en el per�odo.

El directorio hermano `hotelmatch-web` contiene un cliente Angular con Material,
autenticaci�n, b�squeda, ranking, detalle, reservas, cancelaci�n, reportes y
editor t�cnico de inventario. Es una implementaci�n inicial sin compilaci�n
verificada. Faltan formularios administrativos de negocio, traducci�n completa,
paginaci�n navegable y pruebas de interacci�n.

Se prepararon Dockerfiles y un workflow de pruebas para el backend. No se
ejecutaron en Docker ni se publicaron en GitHub.

## Comandos y resultados reales

JDK seleccionado: `C:\Users\nicon\.jdks\graalvm-jdk-17.0.12`.

1. `mvnw.cmd test`: falla el wrapper con `Cannot index into a null array`.
2. Maven 3.9.9 instalado, modo offline, cach� local expl�cita, `test`:
   BUILD FAILURE en testCompile al acceder a `spring-boot-test-3.5.16.jar`.
3. Mismo Maven, `compile` tras agregar reportes: BUILD FAILURE al acceder
   a `jakarta.activation-api-2.1.4.jar` en el repositorio Maven del usuario.
4. `npm install --cache .npm-cache --no-audit --no-fund --fetch-retries=0`:
   EACCES al descargar `@angular/animations` de registry.npmjs.org.

No hubo una ejecuci�n de pruebas satisfactoria ni se conoce el n�mero real de
pruebas aprobadas. No se realiz� verificaci�n en navegador.

## Ejecuci�n reproducible en un entorno con acceso a dependencias

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

Demo local: `guest@hotelmatch.dev` y `admin@hotelmatch.dev`, contrase�a
`HotelMatch2026!`. Usar estas cuentas solo con el perfil de demostraci�n H2.
Validar: registro, login, b�squeda, detalle, reserva, segunda reserva conflictiva,
cancelaci�n fuera y dentro de la ventana, autorizaci�n por hotel y PDF.

## Pendientes que siguen abiertos

IA para interpretar texto libre, servicio externo, migraciones Flyway y
restricci�n PostgreSQL de solapamiento no se implementaron en esta revisi�n.
No se modificaron pesos del ranking sin un conjunto de validaci�n que lo sustente.

Los PR, merges, publicaci�n de Pages, borrado de ramas y correos verificados
no se ejecutaron ni se acreditan aqu�. Falta elegir proveedor y configurar
base de datos y secretos antes de desplegar API y frontend.

El informe institucional, keynote final, notas de participaci�n, entrevistas,
videos y testimonios siguen pendientes. Los archivos parciales originales se
conservan. No hay evidencia suficiente para redactar sprints ejecutados,
validaciones de usuarios o notas individuales como hechos consumados.

El ZIP de esta revisi�n es un paquete de fuentes para revisi�n, no el paquete
acad�mico final. Debe sustituirse tras completar las verificaciones y evidencias.

