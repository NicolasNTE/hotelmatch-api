# HotelMatch Web

Cliente Angular (standalone) con Angular Material para la API HotelMatch. Interfaz en es-419 y en-US.

## Ejecutar

Con Node 24 y la API en `localhost:8080`:

```powershell
npm install
npm start
```

Abre `http://localhost:4200`. El proxy de desarrollo redirige `/api` a la API.

Para servir la versión compilada: `npm run build` y luego `node serve.mjs` (`http://127.0.0.1:4200`).

## Notas

- El token se guarda en `sessionStorage` y solo se envía a la API. La autorización efectiva la hace el backend.
- Las fotos de `public/assets` son imágenes generadas con IA, usadas como ilustración de demostración.
- En hosting estático, reenviar `/api/*` a la API y dejar `index.html` como respaldo de las demás rutas.
