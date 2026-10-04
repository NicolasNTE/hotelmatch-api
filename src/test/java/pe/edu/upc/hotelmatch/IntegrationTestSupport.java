package pe.edu.upc.hotelmatch;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pe.edu.upc.hotelmatch.config.AppConfig;
import pe.edu.upc.hotelmatch.config.DemoDataSeeder;

/** Pruebas de integración sobre H2 con los datos de demostración cargados por DemoDataSeeder. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public abstract class IntegrationTestSupport {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static LocalDate today() {
        return LocalDate.now(AppConfig.BUSINESS_ZONE);
    }

    protected static String unique(String prefix) {
        return prefix + SEQUENCE.incrementAndGet() + "-" + System.nanoTime() % 100000;
    }

    protected String adminToken() throws Exception {
        return login(DemoDataSeeder.ADMIN_EMAIL, DemoDataSeeder.DEMO_PASSWORD);
    }

    protected String guestToken() throws Exception {
        return login(DemoDataSeeder.GUEST_EMAIL, DemoDataSeeder.DEMO_PASSWORD);
    }

    protected String login(String email, String password) throws Exception {
        String body = postJson("/api/v1/auth/login", null, Map.of("email", email, "password", password))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    protected ResultActions send(MockHttpServletRequestBuilder request, String token, Object body)
            throws Exception {
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        }
        return mvc.perform(request);
    }

    protected ResultActions postJson(String url, String token, Object body) throws Exception {
        return send(post(url), token, body);
    }

    protected ResultActions putJson(String url, String token, Object body) throws Exception {
        return send(put(url), token, body);
    }

    protected ResultActions getJson(String url, String token) throws Exception {
        return send(get(url), token, null);
    }

    protected ResultActions deleteJson(String url, String token) throws Exception {
        return send(delete(url), token, null);
    }

    protected static <T> T read(ResultActions result, String path) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
    }

    /** Cuerpo válido de una habitación nueva; el número es único para no chocar entre pruebas. */
    protected Map<String, Object> roomBody(String number, String basePrice, int capacity) {
        return Map.ofEntries(
                Map.entry("name", "Habitación de prueba " + number),
                Map.entry("number", number),
                Map.entry("floor", 5),
                Map.entry("type", "SUPERIOR"),
                Map.entry("capacity", capacity),
                Map.entry("bedType", "QUEEN"),
                Map.entry("basePrice", basePrice),
                Map.entry("experience", Map.of("privacy", 4, "quiet", 4, "view", 3, "space", 3)));
    }
}
