package pe.edu.upc.hotelmatch.recommendation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import pe.edu.upc.hotelmatch.hotel.BedType;
import pe.edu.upc.hotelmatch.hotel.RoomType;
import pe.edu.upc.hotelmatch.recommendation.Priority;
import pe.edu.upc.hotelmatch.recommendation.TripPurpose;

public final class RecommendationDtos {

    private RecommendationDtos() {
    }

    public enum Hint {
        NO_AVAILABILITY,
        NONE_WITHIN_BUDGET
    }

    public record RecommendationRequest(
            @NotNull LocalDate checkIn,
            @NotNull LocalDate checkOut,
            @NotNull @Min(1) Integer guests,
            @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal maxBudgetPerNight,
            @NotNull TripPurpose purpose,
            @NotEmpty @Size(max = 3) Set<Priority> priorities,
            Set<Long> amenityIds,
            @Size(max = 100) String city,
            @Size(max = 500) String freeText) {
    }

    public record CriterionBreakdown(String criterion, String label, int score, double weight, boolean met) {
    }

    public record RecommendedRoom(
            int position,
            int compatibility,
            Long roomId,
            String roomName,
            String roomNumber,
            RoomType type,
            BedType bedType,
            int capacity,
            String imageUrl,
            Long hotelId,
            String hotelName,
            String city,
            boolean sustainabilitySeal,
            BigDecimal pricePerNight,
            BigDecimal totalPrice,
            List<String> amenities,
            List<String> matchedAmenities,
            List<String> missingAmenities,
            List<CriterionBreakdown> breakdown,
            String explanation) {
    }

    public record RecommendationResponse(
            Long profileId,
            int nights,
            Hint hint,
            BigDecimal cheapestAvailablePrice,
            List<RecommendedRoom> results) {
    }
}
