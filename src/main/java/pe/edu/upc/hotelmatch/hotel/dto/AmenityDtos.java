package pe.edu.upc.hotelmatch.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.upc.hotelmatch.hotel.Amenity;
import pe.edu.upc.hotelmatch.hotel.AmenityCategory;

public final class AmenityDtos {

    private AmenityDtos() {
    }

    public record AmenityRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull AmenityCategory category,
            @Size(max = 60) String icon) {
    }

    public record AmenityResponse(Long id, String name, AmenityCategory category, String icon) {

        public static AmenityResponse from(Amenity a) {
            return new AmenityResponse(a.getId(), a.getName(), a.getCategory(), a.getIcon());
        }
    }

    public record RoomAmenityRequest(@NotNull Long amenityId, @Size(max = 200) String detail) {
    }

    public record RoomAmenityDetailRequest(@Size(max = 200) String detail) {
    }
}
