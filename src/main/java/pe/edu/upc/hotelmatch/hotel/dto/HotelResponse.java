package pe.edu.upc.hotelmatch.hotel.dto;

import pe.edu.upc.hotelmatch.hotel.Hotel;

/** legalName y active solo se informan al administrador del hotel; para el huésped quedan nulos. */
public record HotelResponse(
        Long id,
        String legalName,
        String tradeName,
        String address,
        String city,
        String district,
        Double latitude,
        Double longitude,
        boolean sustainabilitySeal,
        String sustainabilityDescription,
        Boolean active) {

    public static HotelResponse full(Hotel h) {
        return new HotelResponse(h.getId(), h.getLegalName(), h.getTradeName(), h.getAddress(), h.getCity(),
                h.getDistrict(), h.getLatitude(), h.getLongitude(), h.isSustainabilitySeal(),
                h.getSustainabilityDescription(), h.isActive());
    }

    public static HotelResponse publicView(Hotel h) {
        return new HotelResponse(h.getId(), null, h.getTradeName(), h.getAddress(), h.getCity(),
                h.getDistrict(), h.getLatitude(), h.getLongitude(), h.isSustainabilitySeal(),
                h.getSustainabilityDescription(), null);
    }
}
