package pe.edu.upc.hotelmatch.hotel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.dto.HotelRequest;
import pe.edu.upc.hotelmatch.hotel.dto.HotelResponse;
import pe.edu.upc.hotelmatch.iam.Role;
import pe.edu.upc.hotelmatch.security.AppUserPrincipal;
import pe.edu.upc.hotelmatch.security.CurrentUser;

@Service
@Transactional
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotels;
    private final CurrentUser currentUser;

    public Hotel create(HotelRequest request) {
        Hotel hotel = new Hotel();
        apply(hotel, request);
        return hotels.save(hotel);
    }

    @Transactional(readOnly = true)
    public HotelResponse get(Long id) {
        Hotel hotel = hotels.findById(id).orElseThrow(() -> ApiException.notFound("hotel.notFound", id));
        if (isOwner(hotel)) {
            return HotelResponse.full(hotel);
        }
        if (!hotel.isActive()) {
            throw ApiException.notFound("hotel.notFound", id);
        }
        return HotelResponse.publicView(hotel);
    }

    public HotelResponse update(Long id, HotelRequest request) {
        Hotel hotel = ownedHotel(id);
        apply(hotel, request);
        return HotelResponse.full(hotel);
    }

    public void delete(Long id, boolean confirm) {
        Hotel hotel = ownedHotel(id);
        if (!confirm) {
            throw ApiException.badRequest("confirm.required");
        }
        hotel.setActive(false);
    }

    private Hotel ownedHotel(Long id) {
        Hotel hotel = hotels.findById(id).orElseThrow(() -> ApiException.notFound("hotel.notFound", id));
        currentUser.requireOwnership(hotel.getId(), "hotel.forbidden");
        return hotel;
    }

    private boolean isOwner(Hotel hotel) {
        AppUserPrincipal principal = currentUser.principal();
        return principal.getRole() == Role.ADMIN && hotel.getId().equals(principal.getHotelId());
    }

    private void apply(Hotel hotel, HotelRequest request) {
        hotel.setLegalName(request.legalName().trim());
        hotel.setTradeName(request.tradeName().trim());
        hotel.setAddress(request.address().trim());
        hotel.setCity(request.city().trim());
        hotel.setDistrict(request.district());
        hotel.setLatitude(request.latitude());
        hotel.setLongitude(request.longitude());
        hotel.setSustainabilitySeal(Boolean.TRUE.equals(request.sustainabilitySeal()));
        hotel.setSustainabilityDescription(request.sustainabilityDescription());
    }
}
