package pe.edu.upc.hotelmatch.iam;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;
import pe.edu.upc.hotelmatch.config.DemoDataSeeder;

class AuthIntegrationTest extends IntegrationTestSupport {

    private Map<String, Object> guest(String email, String password) {
        return Map.of("name", "Valeria Ríos", "email", email, "password", password, "role", "GUEST");
    }

    @Test
    @DisplayName("US01: registra un huésped, normaliza el correo y no expone la contraseña")
    void registersGuest() throws Exception {
        String email = unique("Valeria.") + "@Example.com";

        postJson("/api/v1/auth/register", null, guest(email, "Contraseña123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.role").value("GUEST"))
                .andExpect(content().string(not(containsString("Contraseña123"))))
                .andExpect(content().string(not(containsString("passwordHash"))));
    }

    @Test
    @DisplayName("US01 E2: un correo ya registrado devuelve 409 y no crea el registro")
    void rejectsDuplicateEmail() throws Exception {
        String email = unique("dup.") + "@example.com";
        postJson("/api/v1/auth/register", null, guest(email, "Contraseña123")).andExpect(status().isCreated());

        postJson("/api/v1/auth/register", null, guest(email.toUpperCase(), "Contraseña123"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("auth.emailInUse"));
    }

    @Test
    @DisplayName("Valida contraseña mínima y formato de correo con detalle por campo")
    void validatesRegistration() throws Exception {
        postJson("/api/v1/auth/register", null, guest("no-es-correo", "corta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Un administrador debe registrar los datos de su hotel")
    void adminRequiresHotel() throws Exception {
        postJson("/api/v1/auth/register", null, Map.of("name", "Miguel Ponce", "email", unique("adm.") + "@example.com",
                "password", "Contraseña123", "role", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("auth.hotelRequired"));
    }

    @Test
    @DisplayName("Un administrador se registra junto con su hotel")
    void registersAdminWithHotel() throws Exception {
        Map<String, Object> hotel = Map.of("legalName", "Hotel Nuevo S.A.C.", "tradeName", "Hotel Nuevo",
                "address", "Calle Los Pinos 100", "city", "Cusco");

        postJson("/api/v1/auth/register", null, Map.of("name", "Elena Quispe", "email", unique("hotel.") + "@example.com",
                "password", "Contraseña123", "role", "ADMIN", "hotel", hotel))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.hotel.tradeName").value("Hotel Nuevo"));
    }

    @Test
    @DisplayName("US02: el inicio de sesión emite un token y las credenciales erróneas dan un mensaje genérico")
    void loginIssuesTokenOrFailsGenerically() throws Exception {
        postJson("/api/v1/auth/login", null, Map.of("email", DemoDataSeeder.GUEST_EMAIL,
                "password", DemoDataSeeder.DEMO_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("GUEST"));

        postJson("/api/v1/auth/login", null, Map.of("email", DemoDataSeeder.GUEST_EMAIL, "password", "incorrecta"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos."));

        postJson("/api/v1/auth/login", null, Map.of("email", "nadie@example.com", "password", "incorrecta"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Correo o contraseña incorrectos."));
    }

    @Test
    @DisplayName("Los mensajes de error se devuelven en el idioma de Accept-Language")
    void localizesErrors() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/v1/auth/login").header("Accept-Language", "en")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "nadie@example.com", "password", "incorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Incorrect email or password."));
    }

    @Test
    @DisplayName("Sin token se responde 401 y con token el perfil propio")
    void protectedResourcesNeedToken() throws Exception {
        getJson("/api/v1/users/me", null).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
        getJson("/api/v1/users/me", "token.invalido.xx").andExpect(status().isUnauthorized());

        getJson("/api/v1/users/me", guestToken())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(DemoDataSeeder.GUEST_EMAIL));
    }

    @Test
    @DisplayName("Actualiza el perfil, exige la contraseña actual para cambiarla y da de baja la cuenta")
    void updatesAndDeactivatesAccount() throws Exception {
        String email = unique("perfil.") + "@example.com";
        postJson("/api/v1/auth/register", null, guest(email, "Contraseña123")).andExpect(status().isCreated());
        String token = login(email, "Contraseña123");

        putJson("/api/v1/users/me", token, Map.of("name", "Nombre Nuevo"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Nombre Nuevo"));

        putJson("/api/v1/users/me", token, Map.of("newPassword", "OtraClave456"))
                .andExpect(status().isUnprocessableEntity());
        putJson("/api/v1/users/me", token, Map.of("newPassword", "OtraClave456", "currentPassword", "Contraseña123"))
                .andExpect(status().isOk());

        putJson("/api/v1/users/me", token, Map.of("email", DemoDataSeeder.ADMIN_EMAIL))
                .andExpect(status().isConflict());

        deleteJson("/api/v1/users/me", token).andExpect(status().isNoContent());
        getJson("/api/v1/users/me", token).andExpect(status().isUnauthorized());
        postJson("/api/v1/auth/login", null, Map.of("email", email, "password", "OtraClave456"))
                .andExpect(status().isUnauthorized());
    }
}
