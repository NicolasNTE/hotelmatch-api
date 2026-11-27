package pe.edu.upc.hotelmatch.inventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.hotel.Room;

/** Precio vigente por noche: la tarifa especial que cubre la fecha o, si no hay, el precio base de la habitación. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PricingService {

    private final RateRepository rates;

    public record Quote(int nights, BigDecimal total, BigDecimal averagePerNight) {
    }

    /** Cotiza las noches [checkIn, checkOut). */
    public Quote quote(Room room, LocalDate checkIn, LocalDate checkOut) {
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        List<Rate> overlapping = rates.findOverlapping(room.getId(), checkIn, checkOut.minusDays(1));
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate day = checkIn; day.isBefore(checkOut); day = day.plusDays(1)) {
            total = total.add(priceOn(room, overlapping, day));
        }
        BigDecimal average = total.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP);
        return new Quote(nights, total, average);
    }

    public BigDecimal priceOn(Room room, LocalDate day) {
        return priceOn(room, rates.findOverlapping(room.getId(), day, day), day);
    }

    private BigDecimal priceOn(Room room, List<Rate> candidates, LocalDate day) {
        return candidates.stream()
                .filter(r -> !day.isBefore(r.getStartDate()) && !day.isAfter(r.getEndDate()))
                .max(Comparator.comparing(Rate::getId))
                .map(Rate::getPrice)
                .orElse(room.getBasePrice());
    }
}
