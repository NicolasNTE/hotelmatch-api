package pe.edu.upc.hotelmatch.hotel;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.AmenityRequest;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.AmenityResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class AmenityService {

    private final AmenityRepository amenities;
    private final RoomAmenityRepository roomAmenities;

    @Transactional(readOnly = true)
    public List<AmenityResponse> list() {
        return amenities.findAllByOrderByNameAsc().stream().map(AmenityResponse::from).toList();
    }

    public AmenityResponse create(AmenityRequest request) {
        String name = request.name().trim();
        if (amenities.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("amenity.nameTaken", name);
        }
        return AmenityResponse.from(amenities.save(new Amenity(name, request.category(), request.icon())));
    }

    public AmenityResponse update(Long id, AmenityRequest request) {
        Amenity amenity = find(id);
        String name = request.name().trim();
        if (amenities.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw ApiException.conflict("amenity.nameTaken", name);
        }
        amenity.setName(name);
        amenity.setCategory(request.category());
        amenity.setIcon(request.icon());
        return AmenityResponse.from(amenity);
    }

    public void delete(Long id) {
        Amenity amenity = find(id);
        if (roomAmenities.existsByAmenityId(id)) {
            throw ApiException.conflict("amenity.inUse", amenity.getName());
        }
        amenities.delete(amenity);
    }

    private Amenity find(Long id) {
        return amenities.findById(id).orElseThrow(() -> ApiException.notFound("amenity.notFound", id));
    }
}
