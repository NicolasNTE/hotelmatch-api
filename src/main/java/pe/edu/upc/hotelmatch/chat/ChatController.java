package pe.edu.upc.hotelmatch.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/** Stateless assistant. FAQ fallback is explicitly distinguished from model-generated answers. */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ObjectMapper mapper;
    private final String endpoint;
    private final String model;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    public ChatController(ObjectMapper mapper, @Value("${hotelmatch.chat.ollama-url:}") String endpoint,
            @Value("${hotelmatch.chat.model:llama3.2:3b}") String model) {
        this.mapper = mapper; this.endpoint = endpoint; this.model = model;
    }
    public record Question(@NotBlank @Size(max = 500) String message) {}
    public record Answer(String answer, String mode) {}
    @PostMapping
    public Answer chat(@Valid @RequestBody Question question) {
        if (!endpoint.isBlank()) {
            try {
                String system = "Eres el asistente de HotelMatch. Responde brevemente en el idioma del usuario. "
                        + "Solo orienta sobre HotelMatch. Se busca por fechas, huéspedes y presupuesto; se eligen "
                        + "hasta tres prioridades. El ranking es determinista y explicable. El huésped reserva desde "
                        + "el detalle y cancela en Mis reservas solo con más de 48 horas antes del ingreso. "
                        + "Los administradores gestionan su hotel, habitaciones, servicios, tarifas y disponibilidad, "
                        + "y descargan reportes PDF. No tienes acceso a datos en tiempo real, no ejecutes reservas, "
                        + "no inventes precios, disponibilidad, pagos ni porcentajes. Remite a las pantallas para verificar.";
                String body = mapper.writeValueAsString(Map.of("model", model, "stream", false,
                        "options", Map.of("temperature", 0.2, "num_predict", 220),
                        "messages", List.of(Map.of("role", "system", "content", system),
                                Map.of("role", "user", "content", question.message()))));
                var request = HttpRequest.newBuilder(URI.create(endpoint.replaceAll("/$", "") + "/api/chat"))
                        .timeout(Duration.ofSeconds(20)).header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String answer = mapper.readTree(response.body()).path("message").path("content").asText();
                    if (!answer.isBlank()) return new Answer(answer, "AI");
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            catch (Exception e) { /* The user can still access verified FAQ guidance. */ }
        }
        String text = question.message().toLowerCase(Locale.ROOT);
        String answer = text.contains("cancel") ? "Cancela desde Mis reservas. Deben faltar más de 48 horas para el ingreso."
                : text.contains("report") || text.contains("ingreso") ? "El administrador consulta Reportes y elige un período para descargar el PDF. El valor confirmado no representa pagos cobrados."
                : text.contains("reserv") ? "Busca por fechas y huéspedes, elige una habitación y confirma la reserva. El sistema verifica disponibilidad antes de emitir el código."
                : text.contains("prior") || text.contains("compat") ? "Selecciona de una a tres prioridades y los servicios deseados. El ranking muestra el porcentaje y el desglose por criterio."
                : "Puedo orientarte sobre búsquedas, compatibilidad, reservas, cancelaciones y reportes. Verifica los precios y la disponibilidad en las pantallas de la aplicación.";
        return new Answer(answer, "FAQ");
    }
}
