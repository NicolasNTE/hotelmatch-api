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
    @Test void rejectsReversedRange() throws Exception {
        getJson("/api/v1/reports?from=2030-02-01&to=2030-01-01", adminToken())
                .andExpect(status().isBadRequest());
    }
}
