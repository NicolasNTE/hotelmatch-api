package pe.edu.upc.hotelmatch.iam;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.iam.dto.UpdateProfileRequest;
import pe.edu.upc.hotelmatch.iam.dto.UserResponse;
import pe.edu.upc.hotelmatch.security.CurrentUser;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(load());
    }

    public UserResponse update(UpdateProfileRequest request) {
        User user = load();

        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        if (request.email() != null) {
            String email = request.email().trim().toLowerCase();
            if (!email.equalsIgnoreCase(user.getEmail()) && users.existsByEmailIgnoreCase(email)) {
                throw ApiException.conflict("auth.emailInUse");
            }
            user.setEmail(email);
        }
        if (request.newPassword() != null) {
            if (request.currentPassword() == null
                    || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw ApiException.unprocessable("user.wrongCurrentPassword");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return UserResponse.from(user);
    }

    public void deactivate() {
        load().setActive(false);
    }

    private User load() {
        return users.findById(currentUser.id()).orElseThrow(() -> ApiException.notFound("user.notFound"));
    }
}
