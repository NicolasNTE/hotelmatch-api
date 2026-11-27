package pe.edu.upc.hotelmatch.hotel.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record HotelRequest(
        @NotBlank @Size(max = 150) String legalName,
        @NotBlank @Size(max = 150) String tradeName,
        @NotBlank @Size(max = 250) String address,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String district,
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @DecimalMin("-180") @DecimalMax("180") Double longitude,
        Boolean sustainabilitySeal,
        @Size(max = 1000) String sustainabilityDescription) {
}
