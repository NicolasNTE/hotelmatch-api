package pe.edu.upc.hotelmatch.inventory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.Room;
import pe.edu.upc.hotelmatch.hotel.RoomService;
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.AvailabilityRangeRequest;
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.AvailabilityUpdateRequest;
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.DayAvailabilityResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomAvailabilityService {

    static final int MAX_RANGE_DAYS = 366;

    private final RoomService roomService;
    private final AvailabilityService availabilityService;
    private final AvailabilityRepository availability;

    public List<DayAvailabilityResponse> register(Long roomId, AvailabilityRangeRequest request) {
        Room room = roomService.requireOwned(roomId);
        requireManualStatus(request.status());
        requireRange(request.startDate(), request.endDate());
        if (availability.existsByRoomIdAndDateBetween(roomId, request.startDate(), request.endDate())) {
            throw ApiException.conflict("availability.exists");
        }
        List<DayAvailabilityResponse> created = new ArrayList<>();
        for (LocalDate day = request.startDate(); !day.isAfter(request.endDate()); day = day.plusDays(1)) {
            availability.save(new Availability(room, day, request.status()));
            created.add(new DayAvailabilityResponse(day, request.status()));
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<DayAvailabilityResponse> query(Long roomId, LocalDate from, LocalDate to) {
        roomService.requireVisible(roomId);
        requireRange(from, to);
        return availabilityService.days(roomId, from, to);
    }

    public DayAvailabilityResponse update(Long roomId, LocalDate date, AvailabilityUpdateRequest request) {
        roomService.requireOwned(roomId);
        requireManualStatus(request.status());
        Availability record = availability.findByRoomIdAndDate(roomId, date)
                .orElseThrow(() -> ApiException.notFound("availability.notFound", date));
        record.setStatus(request.status());
        return new DayAvailabilityResponse(date, record.getStatus());
    }

    public void delete(Long roomId, LocalDate date) {
        roomService.requireOwned(roomId);
        Availability record = availability.findByRoomIdAndDate(roomId, date)
                .orElseThrow(() -> ApiException.notFound("availability.notFound", date));
        availability.delete(record);
    }

    private void requireManualStatus(AvailabilityStatus status) {
        if (status == AvailabilityStatus.BOOKED) {
            throw ApiException.unprocessable("availability.invalidStatus");
        }
    }

    private void requireRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to) || ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
            throw ApiException.unprocessable("availability.invalidRange", MAX_RANGE_DAYS);
        }
    }
}
