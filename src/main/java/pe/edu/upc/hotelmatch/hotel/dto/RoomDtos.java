package pe.edu.upc.hotelmatch.hotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import pe.edu.upc.hotelmatch.hotel.BedType;
import pe.edu.upc.hotelmatch.hotel.Room;
import pe.edu.upc.hotelmatch.hotel.RoomAmenity;
import pe.edu.upc.hotelmatch.hotel.RoomStatus;
import pe.edu.upc.hotelmatch.hotel.RoomType;

public final class RoomDtos {

    private RoomDtos() {
    }

    /** Atributos de experiencia (0 a 5) que alimentan el motor de compatibilidad. */
    public record ExperienceAttributes(
            @NotNull @Min(0) @Max(5) Integer privacy,
            @NotNull @Min(0) @Max(5) Integer quiet,
            @NotNull @Min(0) @Max(5) Integer view,
            @NotNull @Min(0) @Max(5) Integer space) {
    }

    public record RoomRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 20) String number,
            @NotNull @Min(0) Integer floor,
            @NotNull RoomType type,
            @NotNull @Min(1) Integer capacity,
            @NotNull BedType bedType,
            @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal basePrice,
            @NotNull @Valid ExperienceAttributes experience,
            @Size(max = 500) String description,
            @Size(max = 500) String imageUrl,
            Set<Long> amenityIds,
            RoomStatus status) {
    }

    public record RoomAmenityResponse(Long amenityId, String name, String category, String icon, String detail) {

        public static RoomAmenityResponse from(RoomAmenity ra) {
            return new RoomAmenityResponse(ra.getAmenity().getId(), ra.getAmenity().getName(),
                    ra.getAmenity().getCategory().name(), ra.getAmenity().getIcon(), ra.getDetail());
        }
    }

    public record RoomResponse(
            Long id,
            Long hotelId,
            String hotelName,
            String name,
            String number,
            int floor,
            RoomType type,
            int capacity,
            BedType bedType,
            BigDecimal basePrice,
            ExperienceAttributes experience,
            String description,
            String imageUrl,
            RoomStatus status,
            List<RoomAmenityResponse> amenities) {

        public static RoomResponse from(Room room) {
            List<RoomAmenityResponse> amenities = room.getAmenities().stream()
                    .map(RoomAmenityResponse::from)
                    .sorted(Comparator.comparing(RoomAmenityResponse::name))
                    .toList();
            return new RoomResponse(room.getId(), room.getHotel().getId(), room.getHotel().getTradeName(),
                    room.getName(), room.getNumber(), room.getFloor(), room.getType(), room.getCapacity(),
                    room.getBedType(), room.getBasePrice(),
                    new ExperienceAttributes(room.getPrivacyScore(), room.getQuietScore(), room.getViewScore(),
                            room.getSpaceScore()),
                    room.getDescription(), room.getImageUrl(), room.getStatus(), amenities);
        }
    }
}
