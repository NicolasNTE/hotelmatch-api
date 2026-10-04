package pe.edu.upc.hotelmatch.iam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** Todos los campos son opcionales; solo se modifican los informados. */
public record UpdateProfileRequest(
        @Size(min = 1, max = 120) String name,
        @Email @Size(max = 180) String email,
        @Size(min = 8, max = 72) String newPassword,
        String currentPassword) {
}
