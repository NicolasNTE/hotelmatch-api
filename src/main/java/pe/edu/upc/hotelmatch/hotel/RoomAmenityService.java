package pe.edu.upc.hotelmatch.hotel;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.RoomAmenityDetailRequest;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.RoomAmenityRequest;
import pe.edu.upc.hotelmatch.hotel.dto.RoomDtos.RoomAmenityResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomAmenityService {

    private final RoomService roomService;
    private final AmenityRepository amenities;
    private final RoomAmenityRepository roomAmenities;

    @Transactional(readOnly = true)
    public List<RoomAmenityResponse> list(Long roomId) {
        Room room = roomService.requireVisible(roomId);
        return room.getAmenities().stream()
                .map(RoomAmenityResponse::from)
                .sorted(Comparator.comparing(RoomAmenityResponse::name))
                .toList();
    }

    public RoomAmenityResponse add(Long roomId, RoomAmenityRequest request) {
        Room room = roomService.requireOwned(roomId);
        Amenity amenity = amenities.findById(request.amenityId())
                .orElseThrow(() -> ApiException.notFound("amenity.notFound", request.amenityId()));
        if (roomAmenities.findByRoomIdAndAmenityId(roomId, amenity.getId()).isPresent()) {
            throw ApiException.conflict("roomAmenity.exists", amenity.getName());
        }
        RoomAmenity association = new RoomAmenity(room, amenity, request.detail());
        room.getAmenities().add(association);
        roomAmenities.save(association);
        return RoomAmenityResponse.from(association);
    }

    public RoomAmenityResponse updateDetail(Long roomId, Long amenityId, RoomAmenityDetailRequest request) {
        roomService.requireOwned(roomId);
        RoomAmenity association = find(roomId, amenityId);
        association.setDetail(request.detail());
        return RoomAmenityResponse.from(association);
    }

    public void remove(Long roomId, Long amenityId) {
        Room room = roomService.requireOwned(roomId);
        RoomAmenity association = find(roomId, amenityId);
        room.getAmenities().remove(association);
    }

    private RoomAmenity find(Long roomId, Long amenityId) {
        return roomAmenities.findByRoomIdAndAmenityId(roomId, amenityId)
                .orElseThrow(() -> ApiException.notFound("roomAmenity.notFound", amenityId));
    }
}
