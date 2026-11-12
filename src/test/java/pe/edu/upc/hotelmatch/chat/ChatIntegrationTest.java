package pe.edu.upc.hotelmatch.chat;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class ChatIntegrationTest extends IntegrationTestSupport {
    @Test void faqAnswersCancellationWithoutModel() throws Exception {
        postJson("/api/v1/chat", guestToken(), Map.of("message", "Como cancelo mi reserva?"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mode").value("FAQ"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("48 horas")));
    }
    @Test void rejectsEmptyMessage() throws Exception {
        postJson("/api/v1/chat", guestToken(), Map.of("message", ""))
                .andExpect(status().isBadRequest());
    }
}
