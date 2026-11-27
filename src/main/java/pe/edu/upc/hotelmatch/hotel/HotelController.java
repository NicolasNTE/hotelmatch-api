package pe.edu.upc.hotelmatch.hotel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.hotelmatch.hotel.dto.HotelRequest;
import pe.edu.upc.hotelmatch.hotel.dto.HotelResponse;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Tag(name = "Hoteles")
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/{id}")
    @Operation(summary = "Detalle del hotel (completo para su administrador; datos públicos para el huésped)")
    public HotelResponse get(@PathVariable Long id) {
        return hotelService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar datos e información de sostenibilidad del hotel propio")
    public HotelResponse update(@PathVariable Long id, @Valid @RequestBody HotelRequest request) {
        return hotelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Dar de baja el hotel (eliminación lógica; requiere confirm=true)")
    public void delete(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean confirm) {
        hotelService.delete(id, confirm);
    }
}
