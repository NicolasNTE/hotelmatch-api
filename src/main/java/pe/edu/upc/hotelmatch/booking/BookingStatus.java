package pe.edu.upc.hotelmatch.booking;

import java.util.Set;

public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED;

    /** Estados que ocupan la habitación en sus fechas. */
    public static final Set<BookingStatus> OCCUPYING = Set.of(PENDING_PAYMENT, CONFIRMED);
}
