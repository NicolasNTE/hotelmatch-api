package pe.edu.upc.hotelmatch.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.hotelmatch.booking.dto.BookingDtos.BookingResponse;
import pe.edu.upc.hotelmatch.booking.dto.BookingDtos.CreateBookingRequest;
import pe.edu.upc.hotelmatch.booking.dto.BookingDtos.UpdateBookingStatusRequest;
import pe.edu.upc.hotelmatch.common.PagedResponse;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Reservas")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Confirmar la reserva de una habitación para un rango de fechas")
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar las reservas (huésped: las propias; administrador: las de su hotel por periodo)")
    public PagedResponse<BookingResponse> list(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return bookingService.list(status, from, to, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una reserva")
    public BookingResponse get(@PathVariable Long id) {
        return bookingService.get(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar el estado de la reserva (confirmada o pendiente de pago)")
    public BookingResponse updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        return bookingService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GUEST')")
    @Operation(summary = "Cancelar la reserva dentro del plazo definido por la política")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
