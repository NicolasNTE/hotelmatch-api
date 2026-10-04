package pe.edu.upc.hotelmatch.booking;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.booking.dto.BookingDtos.BookingResponse;
import pe.edu.upc.hotelmatch.booking.dto.BookingDtos.CreateBookingRequest;
import pe.edu.upc.hotelmatch.booking.dto.BookingDtos.UpdateBookingStatusRequest;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.common.PagedResponse;
import pe.edu.upc.hotelmatch.config.HotelMatchProperties;
import pe.edu.upc.hotelmatch.hotel.Room;
import pe.edu.upc.hotelmatch.hotel.RoomRepository;
import pe.edu.upc.hotelmatch.hotel.RoomStatus;
import pe.edu.upc.hotelmatch.iam.UserRepository;
import pe.edu.upc.hotelmatch.inventory.AvailabilityService;
import pe.edu.upc.hotelmatch.inventory.PricingService;
import pe.edu.upc.hotelmatch.inventory.PricingService.Quote;
import pe.edu.upc.hotelmatch.security.CurrentUser;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 8;

    private final BookingRepository bookings;
    private final RoomRepository rooms;
    private final UserRepository users;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final CurrentUser currentUser;
    private final HotelMatchProperties properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public BookingResponse create(CreateBookingRequest request) {
        if (request.checkIn().isBefore(LocalDate.now(clock))) {
            throw ApiException.unprocessable("booking.pastDate");
        }
        availabilityService.requireValidRange(request.checkIn(), request.checkOut());

        Room room = rooms.findByIdForUpdate(request.roomId())
                .filter(r -> r.getStatus() == RoomStatus.ACTIVE && r.getHotel().isActive())
                .orElseThrow(() -> ApiException.notFound("room.notFound", request.roomId()));
        if (request.guests() > room.getCapacity()) {
            throw ApiException.unprocessable("booking.capacityExceeded", room.getCapacity());
        }
        if (!availabilityService.isAvailable(room.getId(), request.checkIn(), request.checkOut())) {
            throw ApiException.conflict("booking.roomUnavailable");
        }

        Quote quote = pricingService.quote(room, request.checkIn(), request.checkOut());
        Booking booking = new Booking();
        booking.setCode(newCode());
        booking.setGuest(users.getReferenceById(currentUser.id()));
        booking.setRoom(room);
        booking.setCheckIn(request.checkIn());
        booking.setCheckOut(request.checkOut());
        booking.setGuests(request.guests());
        booking.setTotalAmount(quote.total());
        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponse(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> list(BookingStatus status, LocalDate from, LocalDate to,
            Pageable pageable) {
        Specification<Booking> spec = currentUser.isAdmin()
                ? (root, q, cb) -> cb.equal(root.get("room").get("hotel").get("id"), currentUser.adminHotelId())
                : (root, q, cb) -> cb.equal(root.get("guest").get("id"), currentUser.id());
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThan(root.get("checkOut"), from));
        }
        if (to != null) {
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("checkIn"), to));
        }
        return PagedResponse.of(bookings.findAll(spec, pageable), this::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
        Booking booking = find(id);
        requireParticipant(booking);
        return toResponse(booking);
    }

    public BookingResponse updateStatus(Long id, UpdateBookingStatusRequest request) {
        Booking booking = find(id);
        currentUser.requireOwnership(booking.getRoom().getHotel().getId(), "booking.forbidden");
        if (request.status() == BookingStatus.CANCELLED) {
            throw ApiException.unprocessable("booking.invalidStatus");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw ApiException.unprocessable("booking.alreadyCancelled");
        }
        booking.setStatus(request.status());
        return toResponse(booking);
    }

    public BookingResponse cancel(Long id) {
        Booking booking = find(id);
        if (!booking.getGuest().getId().equals(currentUser.id())) {
            throw ApiException.forbidden("booking.forbidden");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw ApiException.unprocessable("booking.alreadyCancelled");
        }
        if (!withinCancellationWindow(booking)) {
            throw ApiException.unprocessable("booking.cancelWindowExpired",
                    properties.cancellationWindowHours());
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now(clock));
        return toResponse(booking);
    }

    private Booking find(Long id) {
        return bookings.findById(id).orElseThrow(() -> ApiException.notFound("booking.notFound", id));
    }

    private void requireParticipant(Booking booking) {
        boolean isGuest = booking.getGuest().getId().equals(currentUser.id());
        boolean isHotelAdmin = currentUser.isAdmin()
                && booking.getRoom().getHotel().getId().equals(currentUser.adminHotelId());
        if (!isGuest && !isHotelAdmin) {
            throw ApiException.forbidden("booking.forbidden");
        }
    }

    private boolean withinCancellationWindow(Booking booking) {
        Instant checkInStart = booking.getCheckIn().atStartOfDay(clock.getZone()).toInstant();
        Duration remaining = Duration.between(Instant.now(clock), checkInStart);
        return remaining.compareTo(Duration.ofHours(properties.cancellationWindowHours())) > 0;
    }

    private String newCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("HM-");
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (bookings.existsByCode(code));
        return code;
    }

    private BookingResponse toResponse(Booking b) {
        Room room = b.getRoom();
        boolean cancellable = b.getStatus() != BookingStatus.CANCELLED && withinCancellationWindow(b);
        return new BookingResponse(b.getId(), b.getCode(), b.getStatus(), room.getId(), room.getName(),
                room.getNumber(), room.getHotel().getId(), room.getHotel().getTradeName(), b.getGuest().getId(),
                b.getGuest().getName(), b.getCheckIn(), b.getCheckOut(),
                (int) ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut()), b.getGuests(),
                b.getTotalAmount(), cancellable, b.getCreatedAt(), b.getCancelledAt());
    }
}
