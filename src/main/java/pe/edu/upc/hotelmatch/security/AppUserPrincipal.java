package pe.edu.upc.hotelmatch.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.edu.upc.hotelmatch.iam.Role;
import pe.edu.upc.hotelmatch.iam.User;

public class AppUserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final Long hotelId;
    private final boolean active;

    public AppUserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.hotelId = user.getHotel() != null ? user.getHotel().getId() : null;
        this.active = user.isActive();
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public Long getHotelId() {
        return hotelId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
