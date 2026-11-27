package pe.edu.upc.hotelmatch.inventory;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.Room;
import pe.edu.upc.hotelmatch.hotel.RoomService;
import pe.edu.upc.hotelmatch.inventory.dto.RateDtos.RateRequest;
import pe.edu.upc.hotelmatch.inventory.dto.RateDtos.RateResponse;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomRateService {

    private static final LocalDate MIN_DATE = LocalDate.of(1900, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(2999, 12, 31);

    private final RoomService roomService;
    private final RateRepository rates;

    public RateResponse create(Long roomId, RateRequest request) {
        Room room = roomService.requireOwned(roomId);
        requireValid(roomId, null, request);
        Rate rate = new Rate();
        rate.setRoom(room);
        apply(rate, request);
        return RateResponse.from(rates.save(rate));
    }

    @Transactional(readOnly = true)
    public List<RateResponse> list(Long roomId, LocalDate from, LocalDate to) {
        roomService.requireVisible(roomId);
        LocalDate start = from != null ? from : MIN_DATE;
        LocalDate end = to != null ? to : MAX_DATE;
        if (start.isAfter(end)) {
            throw ApiException.unprocessable("dates.invalidRange");
        }
        return rates.findOverlapping(roomId, start, end).stream().map(RateResponse::from).toList();
    }

    public RateResponse update(Long roomId, Long rateId, RateRequest request) {
        roomService.requireOwned(roomId);
        Rate rate = find(roomId, rateId);
        requireValid(roomId, rateId, request);
        apply(rate, request);
        return RateResponse.from(rate);
    }

    public void delete(Long roomId, Long rateId) {
        roomService.requireOwned(roomId);
        rates.delete(find(roomId, rateId));
    }

    private Rate find(Long roomId, Long rateId) {
        return rates.findByIdAndRoomId(rateId, roomId)
                .orElseThrow(() -> ApiException.notFound("rate.notFound", rateId));
    }

    private void requireValid(Long roomId, Long ignoreRateId, RateRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw ApiException.unprocessable("dates.invalidRange");
        }
        boolean overlaps = rates.findOverlapping(roomId, request.startDate(), request.endDate()).stream()
                .anyMatch(r -> !r.getId().equals(ignoreRateId));
        if (overlaps) {
            throw ApiException.conflict("rate.overlap");
        }
    }

    private void apply(Rate rate, RateRequest request) {
        rate.setName(request.name().trim());
        rate.setStartDate(request.startDate());
        rate.setEndDate(request.endDate());
        rate.setPrice(request.price());
    }
}
