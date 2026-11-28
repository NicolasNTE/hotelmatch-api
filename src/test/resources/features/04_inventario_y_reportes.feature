# language: es
Característica: Inventario del hotel y reportes (EP05 y EP06)

  Antecedentes:
    Dado un hotel con habitaciones de 150, 220 y 400 soles por noche

  Escenario: US18-E1 Una habitación registrada participa en las recomendaciones
    Cuando registro una habitación de 180 soles
    Entonces aparece en el ranking de mi ciudad

  Escenario: US18-E2 Campo obligatorio vacío
    Cuando registro una habitación sin nombre
    Entonces la respuesta es 400

  Escenario: US19-E1 La edición se refleja en las búsquedas
    Cuando cambio el precio de la primera habitación a 175 soles
    Entonces el ranking muestra esa habitación a 175 soles

  Escenario: US19-E2 La eliminación conserva el historial
    Dado que un huésped reservó la primera habitación
    Cuando elimino la primera habitación
    Entonces deja de aparecer en el ranking
    Y la reserva histórica se conserva

  Escenario: US20-E1 Precio especial por fecha
    Cuando registro una tarifa de 999 soles para mis fechas
    Entonces el ranking usa 999 soles para la primera habitación

  Escenario: US20-E2 Rango de fechas inválido
    Cuando registro una tarifa que termina antes de empezar
    Entonces la respuesta es 422

  Escenario: US22-E1 Exportación del reporte en PDF
    Cuando exporto el reporte del periodo
    Entonces descargo un archivo PDF
