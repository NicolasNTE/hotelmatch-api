package pe.edu.upc.hotelmatch.report;

import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportIntegrationTest extends IntegrationTestSupport {
    private String query() { return "?from=" + today() + "&to=" + today().plusDays(30); }
    @Test void guestCannotReadReports() throws Exception {
        getJson("/api/v1/reports" + query(), guestToken()).andExpect(status().isForbidden());
    }
    @Test void adminCanReadSummaryAndPdf() throws Exception {
        String token = adminToken();
        getJson("/api/v1/reports" + query(), token).andExpect(status().isOk())
                .andExpect(jsonPath("$.capacityNights").isNumber());
        getJson("/api/v1/reports/pdf" + query(), token).andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }
    @Test void anotherHotelSeesNoneOfTheDemoData() throws Exception {
        String email = unique("otro") + "@hotelmatch.dev";
        postJson("/api/v1/auth/register", null, java.util.Map.of(
                "name", "Otro administrador", "email", email, "password", "Prueba12345!", "role", "ADMIN",
                "hotel", java.util.Map.of("legalName", "Otro S.A.C.", "tradeName", "Otro Hotel",
                        "address", "Av. Prueba 1", "city", "Cusco")))
                .andExpect(status().isCreated());
        String token = login(email, "Prueba12345!");
        getJson("/api/v1/reports" + query(), token).andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings").value(0))
                .andExpect(jsonPath("$.capacityNights").value(0));
    }
    @Test void rejectsReversedRange() throws Exception {
        getJson("/api/v1/reports?from=2030-02-01&to=2030-01-01", adminToken())
                .andExpect(status().isBadRequest());
    }
}
