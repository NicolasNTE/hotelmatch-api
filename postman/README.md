# Pruebas en Postman

`HotelMatch.postman_collection.json`: 20 peticiones con verificaciones (login, 401, 403, ranking, clima, reserva 201,
doble reserva 409, fechas inválidas 422, cancelación, tarifas, reportes JSON/PDF y asistente).

1. Levanta la API (perfil `local` o `h2`).
2. En Postman: **Import** y elige el archivo.
3. Abre la colección y pulsa **Run**. Ejecútala completa y en orden: las peticiones se pasan los tokens e identificadores.

Usa las cuentas de demostración (variables de la colección). Las fechas de reserva se eligen al azar en cada corrida,
y la reserva creada se cancela al final.

Corrida del 27/11/2026 contra PostgreSQL local: 32 de 32 verificaciones correctas.
