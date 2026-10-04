package pe.edu.upc.hotelmatch.hotel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.RoomAmenityDetailRequest;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.RoomAmenityRequest;
import pe.edu.upc.hotelmatch.hotel.dto.RoomDtos.RoomAmenityResponse;

@RestController
@RequestMapping("/api/v1/rooms/{roomId}/services")
@RequiredArgsConstructor
@Tag(name = "Servicios de habitación")
public class RoomAmenityController {

    private final RoomAmenityService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Asociar un servicio del catálogo a la habitación")
    public RoomAmenityResponse add(@PathVariable Long roomId, @Valid @RequestBody RoomAmenityRequest request) {
        return service.add(roomId, request);
    }

    @GetMapping
    @Operation(summary = "Listar los servicios de la habitación")
    public List<RoomAmenityResponse> list(@PathVariable Long roomId) {
        return service.list(roomId);
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el detalle de la asociación servicio-habitación")
    public RoomAmenityResponse updateDetail(@PathVariable Long roomId, @PathVariable Long serviceId,
            @Valid @RequestBody RoomAmenityDetailRequest request) {
        return service.updateDetail(roomId, serviceId, request);
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desasociar un servicio de la habitación")
    public void remove(@PathVariable Long roomId, @PathVariable Long serviceId) {
        service.remove(roomId, serviceId);
    }
}
