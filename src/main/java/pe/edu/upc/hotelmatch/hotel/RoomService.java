package pe.edu.upc.hotelmatch.hotel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.common.PagedResponse;
import pe.edu.upc.hotelmatch.hotel.dto.RoomDtos.ExperienceAttributes;
import pe.edu.upc.hotelmatch.hotel.dto.RoomDtos.RoomRequest;
import pe.edu.upc.hotelmatch.hotel.dto.RoomDtos.RoomResponse;
import pe.edu.upc.hotelmatch.inventory.AvailabilityService;
import pe.edu.upc.hotelmatch.security.CurrentUser;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository rooms;
    private final HotelRepository hotels;
    private final AmenityRepository amenities;
    private final AvailabilityService availabilityService;
    private final CurrentUser currentUser;

    public RoomResponse create(RoomRequest request) {
        Hotel hotel = hotels.findById(currentUser.adminHotelId()).orElseThrow();
        if (!hotel.isActive()) {
            throw ApiException.unprocessable("hotel.inactive");
        }
        if (rooms.existsByHotelIdAndNumberIgnoreCaseAndStatusNot(hotel.getId(), request.number().trim(),
                RoomStatus.DELETED)) {
            throw ApiException.conflict("room.numberTaken", request.number());
        }
        Room room = new Room();
        room.setHotel(hotel);
        apply(room, request);
        if (request.status() != null) {
            room.setStatus(editableStatus(request.status()));
        }
        syncAmenities(room, request.amenityIds());
        return RoomResponse.from(rooms.save(room));
    }

    @Transactional(readOnly = true)
    public PagedResponse<RoomResponse> list(RoomFilter filter, Pageable pageable) {
        if (filter.checkIn() != null || filter.checkOut() != null) {
            availabilityService.requireValidRange(filter.checkIn(), filter.checkOut());
        }
        Specification<Room> spec = RoomSpecs.all();
        if (currentUser.isAdmin()) {
            spec = spec.and(RoomSpecs.hotelIs(currentUser.adminHotelId()))
                    .and(RoomSpecs.statusNot(RoomStatus.DELETED));
            if (filter.status() != null && filter.status() != RoomStatus.DELETED) {
                spec = spec.and(RoomSpecs.statusIs(filter.status()));
            }
        } else {
            spec = spec.and(RoomSpecs.statusIs(RoomStatus.ACTIVE)).and(RoomSpecs.hotelActive());
        }
        if (filter.q() != null && !filter.q().isBlank()) {
            spec = spec.and(RoomSpecs.nameContains(filter.q()));
        }
        if (filter.type() != null) {
            spec = spec.and(RoomSpecs.typeIs(filter.type()));
        }
        if (filter.guests() != null) {
            spec = spec.and(RoomSpecs.capacityAtLeast(filter.guests()));
        }
        if (filter.checkIn() != null) {
            Set<Long> unavailable = availabilityService.unavailableRoomIds(filter.checkIn(), filter.checkOut());
            if (!unavailable.isEmpty()) {
                spec = spec.and(RoomSpecs.idNotIn(unavailable));
            }
        }
        return PagedResponse.of(rooms.findAll(spec, pageable), RoomResponse::from);
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        Room room = currentUser.isAdmin() ? requireOwned(id) : requireVisible(id);
        return RoomResponse.from(room);
    }

    public RoomResponse update(Long id, RoomRequest request) {
        Room room = requireOwned(id);
        if (rooms.existsByHotelIdAndNumberIgnoreCaseAndStatusNotAndIdNot(room.getHotel().getId(),
                request.number().trim(), RoomStatus.DELETED, id)) {
            throw ApiException.conflict("room.numberTaken", request.number());
        }
        apply(room, request);
        if (request.status() != null) {
            room.setStatus(editableStatus(request.status()));
        }
        if (request.amenityIds() != null) {
            syncAmenities(room, request.amenityIds());
        }
        return RoomResponse.from(room);
    }

    public void delete(Long id, boolean confirm) {
        Room room = requireOwned(id);
        if (!confirm) {
            throw ApiException.badRequest("confirm.required");
        }
        room.setStatus(RoomStatus.DELETED);
    }

    /** Habitación del hotel del administrador autenticado (no eliminada). */
    public Room requireOwned(Long id) {
        Room room = rooms.findById(id).filter(r -> r.getStatus() != RoomStatus.DELETED)
                .orElseThrow(() -> ApiException.notFound("room.notFound", id));
        currentUser.requireOwnership(room.getHotel().getId(), "room.forbidden");
        return room;
    }

    /** Habitación consultable por el rol actual: el administrador ve las propias, el huésped solo las activas. */
    public Room requireVisible(Long id) {
        if (currentUser.isAdmin()) {
            return requireOwned(id);
        }
        return rooms.findById(id)
                .filter(r -> r.getStatus() == RoomStatus.ACTIVE && r.getHotel().isActive())
                .orElseThrow(() -> ApiException.notFound("room.notFound", id));
    }

    private void apply(Room room, RoomRequest request) {
        room.setName(request.name().trim());
        room.setNumber(request.number().trim());
        room.setFloor(request.floor());
        room.setType(request.type());
        room.setCapacity(request.capacity());
        room.setBedType(request.bedType());
        room.setBasePrice(request.basePrice());
        ExperienceAttributes exp = request.experience();
        room.setPrivacyScore(exp.privacy());
        room.setQuietScore(exp.quiet());
        room.setViewScore(exp.view());
        room.setSpaceScore(exp.space());
        room.setDescription(request.description());
        room.setImageUrl(request.imageUrl());
    }

    private RoomStatus editableStatus(RoomStatus status) {
        if (status == RoomStatus.DELETED) {
            throw ApiException.unprocessable("room.invalidStatus");
        }
        return status;
    }

    private void syncAmenities(Room room, Set<Long> amenityIds) {
        if (amenityIds == null) {
            return;
        }
        Map<Long, Amenity> found = amenities.findAllById(amenityIds).stream()
                .collect(Collectors.toMap(Amenity::getId, Function.identity()));
        if (found.size() != amenityIds.size()) {
            Set<Long> missing = new HashSet<>(amenityIds);
            missing.removeAll(found.keySet());
            throw ApiException.notFound("amenity.notFound", missing.iterator().next());
        }
        room.getAmenities().removeIf(ra -> !amenityIds.contains(ra.getAmenity().getId()));
        Set<Long> current = room.getAmenities().stream().map(ra -> ra.getAmenity().getId())
                .collect(Collectors.toSet());
        List<Amenity> toAdd = found.values().stream().filter(a -> !current.contains(a.getId())).toList();
        toAdd.forEach(a -> room.getAmenities().add(new RoomAmenity(room, a, null)));
    }
}
