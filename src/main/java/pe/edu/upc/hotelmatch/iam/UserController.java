package pe.edu.upc.hotelmatch.iam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.hotelmatch.iam.dto.UpdateProfileRequest;
import pe.edu.upc.hotelmatch.iam.dto.UserResponse;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Consultar el perfil propio")
    public UserResponse me() {
        return userService.me();
    }

    @PutMapping
    @Operation(summary = "Actualizar nombre, correo o contraseña propios")
    public UserResponse update(@Valid @RequestBody UpdateProfileRequest request) {
        return userService.update(request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Dar de baja la cuenta propia (eliminación lógica)")
    public void delete() {
        userService.deactivate();
    }
}
