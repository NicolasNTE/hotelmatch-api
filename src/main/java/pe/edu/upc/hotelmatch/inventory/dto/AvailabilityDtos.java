package pe.edu.upc.hotelmatch.inventory.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import pe.edu.upc.hotelmatch.inventory.AvailabilityStatus;

public final class AvailabilityDtos {

    private AvailabilityDtos() {
    }

    public record AvailabilityRangeRequest(
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull AvailabilityStatus status) {
    }

    public record AvailabilityUpdateRequest(@NotNull AvailabilityStatus status) {
    }

    public record DayAvailabilityResponse(LocalDate date, AvailabilityStatus status) {
    }
}
