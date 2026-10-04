package pe.edu.upc.hotelmatch.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.iam.Role;

@Component
public class CurrentUser {

    public AppUserPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw ApiException.forbidden("error.forbidden");
        }
        return principal;
    }

    public Long id() {
        return principal().getId();
    }

    public boolean isAdmin() {
        return principal().getRole() == Role.ADMIN;
    }

    /** Hotel del administrador autenticado; falla si el usuario no es administrador de un hotel. */
    public Long adminHotelId() {
        AppUserPrincipal principal = principal();
        if (principal.getRole() != Role.ADMIN || principal.getHotelId() == null) {
            throw ApiException.forbidden("error.forbidden");
        }
        return principal.getHotelId();
    }

    public void requireOwnership(Long hotelId, String errorCode) {
        if (!adminHotelId().equals(hotelId)) {
            throw ApiException.forbidden(errorCode);
        }
    }
}
