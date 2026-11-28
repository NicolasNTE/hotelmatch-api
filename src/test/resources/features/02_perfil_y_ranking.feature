# language: es
Característica: Perfil de viaje y ranking de compatibilidad (EP02 y EP03)

  Antecedentes:
    Dado un hotel con habitaciones de 150, 220 y 400 soles por noche
    Y que soy un huésped

  Escenario: US05-E1 El ranking respeta el presupuesto
    Cuando pido el ranking con un presupuesto de 250 soles
    Entonces ninguna habitación supera 250 soles por noche

  Escenario: US05-E2 Presupuesto no válido
    Cuando pido el ranking con un presupuesto de 0 soles
    Entonces la respuesta es 400

  Escenario: US06-E1 Sin motivo de estadía
    Cuando pido el ranking sin motivo de estadía
    Entonces la respuesta es 400

  Escenario: US06-E2 El motivo pondera sus atributos
    Cuando pido el ranking con el motivo "COUPLE"
    Entonces el desglose incluye los criterios "PRIVACY" y "VIEW"

  Escenario: US07-E1 No se admite una cuarta prioridad
    Cuando pido el ranking con 4 prioridades
    Entonces la respuesta es 400

  Escenario: US07-E2 Se requiere al menos una prioridad
    Cuando pido el ranking con 0 prioridades
    Entonces la respuesta es 400

  Escenario: US08-E1 Los servicios deseados llegan al motor
    Cuando pido el ranking marcando dos servicios
    Entonces cada resultado informa cuáles de los dos servicios tiene

  Escenario: US08-E2 Sin servicios deseados
    Cuando pido el ranking con un presupuesto de 500 soles
    Entonces la respuesta es 200
    Y recibo 3 habitaciones

  Escenario: US09-E1 Texto libre con señales de preferencia
    Cuando describo mi viaje como "Es nuestro aniversario y queremos silencio y vista"
    Entonces se detectan las prioridades "QUIET" y "VIEW"

  Escenario: US09-E2 Texto libre sin señales útiles
    Cuando describo mi viaje como "Llego el martes"
    Entonces no se detecta ninguna preferencia

  Escenario: US10-E1 Cada habitación recibe un puntaje
    Cuando pido el ranking con un presupuesto de 500 soles
    Entonces cada habitación tiene un puntaje entre 0 y 100

  Escenario: US10-E2 Empate de puntaje
    Dado dos habitaciones iguales con precios distintos
    Cuando pido el ranking con un presupuesto de 500 soles
    Entonces ante igual puntaje aparece primero la de menor precio

  Escenario: US11-E1 Orden descendente
    Cuando pido el ranking con un presupuesto de 500 soles
    Entonces los resultados están ordenados de mayor a menor compatibilidad

  Escenario: US11-E2 Ninguna cumple el presupuesto
    Cuando pido el ranking con un presupuesto de 50 soles
    Entonces no hay resultados y se informa el precio disponible más bajo

  Escenario: US12-E1 Explicación de la recomendación
    Cuando pido el ranking con un presupuesto de 500 soles
    Entonces cada resultado trae una explicación y su desglose

  Escenario: US14-E2 Servicio externo no disponible
    Cuando consulto el clima de un hotel sin coordenadas
    Entonces la respuesta es 200
    Y se informa que el clima no está disponible
