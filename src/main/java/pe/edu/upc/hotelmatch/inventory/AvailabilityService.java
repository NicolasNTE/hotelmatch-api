package pe.edu.upc.hotelmatch.inventory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.booking.Booking;
import pe.edu.upc.hotelmatch.booking.BookingRepository;
import pe.edu.upc.hotelmatch.booking.BookingStatus;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.DayAvailabilityResponse;

/** Consultas de disponibilidad: sin registro un día se considera disponible salvo que lo ocupe una reserva. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availability;
    private final BookingRepository bookings;

    public void requireValidRange(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw ApiException.unprocessable("dates.invalidRange");
        }
    }

    /** Noches [checkIn, checkOut): habitaciones con algún día bloqueado o con una reserva que las ocupe. */
    public Set<Long> unavailableRoomIds(LocalDate checkIn, LocalDate checkOut) {
        requireValidRange(checkIn, checkOut);
        Set<Long> ids = new HashSet<>(
                availability.findRoomIdsWithStatusInRange(AvailabilityStatus.BLOCKED, checkIn, checkOut));
        ids.addAll(bookings.findBookedRoomIds(checkIn, checkOut, BookingStatus.OCCUPYING));
        return ids;
    }

    public boolean isAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        requireValidRange(checkIn, checkOut);
        return !availability.existsInRange(roomId, AvailabilityStatus.BLOCKED, checkIn, checkOut)
                && !bookings.existsOverlap(roomId, checkIn, checkOut, BookingStatus.OCCUPYING);
    }

    /** Estado día por día entre from y to (ambos inclusive). */
    public List<DayAvailabilityResponse> days(Long roomId, LocalDate from, LocalDate to) {
        Map<LocalDate, AvailabilityStatus> registered = availability
                .findByRoomIdAndDateBetweenOrderByDate(roomId, from, to).stream()
                .collect(Collectors.toMap(Availability::getDate, Availability::getStatus));
        List<Booking> overlapping = bookings.findOverlapping(roomId, from, to.plusDays(1), BookingStatus.OCCUPYING);

        List<DayAvailabilityResponse> result = new ArrayList<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            LocalDate current = day;
            boolean booked = overlapping.stream()
                    .anyMatch(b -> !current.isBefore(b.getCheckIn()) && current.isBefore(b.getCheckOut()));
            AvailabilityStatus status = booked ? AvailabilityStatus.BOOKED
                    : registered.getOrDefault(day, AvailabilityStatus.AVAILABLE);
            result.add(new DayAvailabilityResponse(day, status));
        }
        return result;
    }
}
