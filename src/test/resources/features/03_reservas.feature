# language: es
Característica: Reservas del huésped (EP04)

  Antecedentes:
    Dado un hotel con habitaciones de 150, 220 y 400 soles por noche
    Y que soy un huésped

  Escenario: US15-E1 Confirmación de la reserva
    Cuando reservo la primera habitación dentro de 30 días
    Entonces la respuesta es 201
    Y la reserva tiene código y aparece en mi cuenta

  Escenario: US15-E2 Habitación ya tomada
    Dado que reservé la primera habitación dentro de 30 días
    Cuando reservo la primera habitación dentro de 30 días
    Entonces la respuesta es 409

  Escenario: US17-E1 Cancelación con más de 48 horas
    Dado que reservé la primera habitación dentro de 30 días
    Cuando cancelo la reserva
    Entonces la reserva queda cancelada
    Y la habitación se puede volver a reservar en las mismas fechas

  Escenario: US17-E2 Plazo de cancelación vencido
    Dado que reservé la primera habitación dentro de 1 días
    Cuando cancelo la reserva
    Entonces la respuesta es 422
