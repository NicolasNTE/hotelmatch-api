package pe.edu.upc.hotelmatch.inventory;

/** BOOKED solo se calcula a partir de las reservas; no se puede registrar manualmente. */
public enum AvailabilityStatus {
    AVAILABLE,
    BLOCKED,
    BOOKED
}
