package pe.edu.upc.hotelmatch.inventory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.AvailabilityRangeRequest;
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.AvailabilityUpdateRequest;
import pe.edu.upc.hotelmatch.inventory.dto.AvailabilityDtos.DayAvailabilityResponse;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/availability")
@RequiredArgsConstructor
@Tag(name = "Disponibilidad")
public class RoomAvailabilityController {

    private final RoomAvailabilityService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar el estado de la habitación para una fecha o rango")
    public List<DayAvailabilityResponse> register(@PathVariable Long roomId,
            @Valid @RequestBody AvailabilityRangeRequest request) {
        return service.register(roomId, request);
    }

    @GetMapping
    @Operation(summary = "Consultar la disponibilidad día por día en un rango de fechas")
    public List<DayAvailabilityResponse> query(@PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.query(roomId, from, to);
    }

    @PutMapping("/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modificar el estado de una fecha ya registrada")
    public DayAvailabilityResponse update(@PathVariable Long roomId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody AvailabilityUpdateRequest request) {
        return service.update(roomId, date, request);
    }

    @DeleteMapping("/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirar un bloqueo de disponibilidad")
    public void delete(@PathVariable Long roomId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        service.delete(roomId, date);
    }
}
