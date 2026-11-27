package pe.edu.upc.hotelmatch.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import pe.edu.upc.hotelmatch.booking.BookingStatus;

public final class BookingDtos {

    private BookingDtos() {
    }

    public record CreateBookingRequest(
            @NotNull Long roomId,
            @NotNull LocalDate checkIn,
            @NotNull LocalDate checkOut,
            @NotNull @Min(1) Integer guests) {
    }

    public record UpdateBookingStatusRequest(@NotNull BookingStatus status) {
    }

    public record BookingResponse(
            Long id,
            String code,
            BookingStatus status,
            Long roomId,
            String roomName,
            String roomNumber,
            Long hotelId,
            String hotelName,
            Long guestId,
            String guestName,
            LocalDate checkIn,
            LocalDate checkOut,
            int nights,
            int guests,
            BigDecimal totalAmount,
            boolean cancellable,
            Instant createdAt,
            Instant cancelledAt) {
    }
}
