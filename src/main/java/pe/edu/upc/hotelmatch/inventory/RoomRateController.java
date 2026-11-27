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
import pe.edu.upc.hotelmatch.inventory.dto.RateDtos.RateRequest;
import pe.edu.upc.hotelmatch.inventory.dto.RateDtos.RateResponse;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/rates")
@RequiredArgsConstructor
@Tag(name = "Tarifas por fecha")
public class RoomRateController {

    private final RoomRateService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un precio especial para una fecha o temporada")
    public RateResponse create(@PathVariable Long roomId, @Valid @RequestBody RateRequest request) {
        return service.create(roomId, request);
    }

    @GetMapping
    @Operation(summary = "Consultar las tarifas especiales que se cruzan con un rango de fechas")
    public List<RateResponse> list(@PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.list(roomId, from, to);
    }

    @PutMapping("/{rateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar una tarifa existente")
    public RateResponse update(@PathVariable Long roomId, @PathVariable Long rateId,
            @Valid @RequestBody RateRequest request) {
        return service.update(roomId, rateId, request);
    }

    @DeleteMapping("/{rateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar la tarifa especial y restituir el precio base")
    public void delete(@PathVariable Long roomId, @PathVariable Long rateId) {
        service.delete(roomId, rateId);
    }
}
