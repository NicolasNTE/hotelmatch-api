package pe.edu.upc.hotelmatch.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import pe.edu.upc.hotelmatch.inventory.Rate;

public final class RateDtos {

    private RateDtos() {
    }

    public record RateRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal price) {
    }

    public record RateResponse(Long id, Long roomId, String name, LocalDate startDate, LocalDate endDate,
                               BigDecimal price) {

        public static RateResponse from(Rate rate) {
            return new RateResponse(rate.getId(), rate.getRoom().getId(), rate.getName(), rate.getStartDate(),
                    rate.getEndDate(), rate.getPrice());
        }
    }
}
