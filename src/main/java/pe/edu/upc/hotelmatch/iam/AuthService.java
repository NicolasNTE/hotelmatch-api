package pe.edu.upc.hotelmatch.iam;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.HotelService;
import pe.edu.upc.hotelmatch.iam.dto.AuthDtos.AuthResponse;
import pe.edu.upc.hotelmatch.iam.dto.AuthDtos.LoginRequest;
import pe.edu.upc.hotelmatch.iam.dto.AuthDtos.RegisterRequest;
import pe.edu.upc.hotelmatch.iam.dto.UserResponse;
import pe.edu.upc.hotelmatch.security.AppUserPrincipal;
import pe.edu.upc.hotelmatch.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final HotelService hotelService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("auth.emailInUse");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        if (request.role() == Role.ADMIN) {
            if (request.hotel() == null) {
                throw ApiException.badRequest("auth.hotelRequired");
            }
            user.setHotel(hotelService.create(request.hotel()));
        }
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        User user = users.findById(principal.getId()).orElseThrow();
        String token = jwtService.generate(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, "Bearer", jwtService.expiresInSeconds(), UserResponse.from(user));
    }
}
