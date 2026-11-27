package pe.edu.upc.hotelmatch.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.hotelmatch.booking.*;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.*;
import pe.edu.upc.hotelmatch.security.CurrentUser;

@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportController {
    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final CurrentUser user;

    public record Summary(LocalDate from, LocalDate to, long bookings, long cancelled,
            long occupiedNights, long capacityNights, BigDecimal occupancyPercent,
            BigDecimal confirmedBookingValue) {}

    @GetMapping
    public Summary summary(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to);
        if (days <= 0 || days > 366) throw ApiException.badRequest("report.invalidRange");
        Long hotelId = user.adminHotelId();
        List<Booking> rows = bookings.findAll((root, q, cb) -> cb.and(
                cb.equal(root.get("room").get("hotel").get("id"), hotelId),
                cb.lessThan(root.get("checkIn"), to), cb.greaterThan(root.get("checkOut"), from)));
        long capacity = rooms.count((root, q, cb) -> cb.and(
                cb.equal(root.get("hotel").get("id"), hotelId),
                cb.equal(root.get("status"), RoomStatus.ACTIVE))) * days;
        long occupied = 0, cancelled = 0;
        BigDecimal value = BigDecimal.ZERO;
        for (Booking b : rows) {
            if (b.getStatus() == BookingStatus.CANCELLED) { cancelled++; continue; }
            long nights = ChronoUnit.DAYS.between(b.getCheckIn().isBefore(from) ? from : b.getCheckIn(),
                    b.getCheckOut().isAfter(to) ? to : b.getCheckOut());
            occupied += nights;
            if (b.getStatus() == BookingStatus.CONFIRMED) {
                // Allocate booked value proportionally; this is not collected cash revenue.
                value = value.add(b.getTotalAmount().multiply(BigDecimal.valueOf(nights))
                        .divide(BigDecimal.valueOf(ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut())), 2, RoundingMode.HALF_UP));
            }
        }
        BigDecimal percent = capacity == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(occupied * 100)
                .divide(BigDecimal.valueOf(capacity), 2, RoundingMode.HALF_UP);
        return new Summary(from, to, rows.size(), cancelled, occupied, capacity, percent, value);
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        Summary s = summary(from, to);
        byte[] pdf = SimplePdf.create(List.of("HotelMatch - Hotel " + user.adminHotelId(),
                "Period [from, to): " + from + " / " + to,
                "Bookings: " + s.bookings() + "    Cancelled: " + s.cancelled(),
                "Occupied nights: " + s.occupiedNights(), "Current active room capacity nights: " + s.capacityNights(),
                "Occupancy (current inventory): " + s.occupancyPercent() + "%",
                "Allocated confirmed booking value (PEN): " + s.confirmedBookingValue(),
                "Value is prorated across stay nights; it is not collected revenue.",
                "Capacity uses current active rooms; historical inventory is not tracked."));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=hotelmatch-report.pdf").body(pdf);
    }
}
