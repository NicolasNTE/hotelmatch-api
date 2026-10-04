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
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.AmenityRequest;
import pe.edu.upc.hotelmatch.hotel.dto.AmenityDtos.AmenityResponse;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
@Tag(name = "Catálogo de servicios")
public class AmenityController {

    private final AmenityService amenityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar un servicio en el catálogo general")
    public AmenityResponse create(@Valid @RequestBody AmenityRequest request) {
        return amenityService.create(request);
    }

    @GetMapping
    @Operation(summary = "Listar el catálogo completo de servicios")
    public List<AmenityResponse> list() {
        return amenityService.list();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Editar nombre, categoría o ícono de un servicio")
    public AmenityResponse update(@PathVariable Long id, @Valid @RequestBody AmenityRequest request) {
        return amenityService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirar un servicio del catálogo si ninguna habitación lo tiene asociado")
    public void delete(@PathVariable Long id) {
        amenityService.delete(id);
    }
}
