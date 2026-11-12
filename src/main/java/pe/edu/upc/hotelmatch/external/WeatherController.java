package pe.edu.upc.hotelmatch.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.hotelmatch.hotel.HotelService;

@RestController
@RequestMapping("/api/v1/hotels/{hotelId}/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final HotelService hotels;
    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    public record Weather(boolean available, String source, String message, JsonNode current, JsonNode daily) {}
    @GetMapping
    public Weather get(@PathVariable Long hotelId) {
        var hotel = hotels.get(hotelId);
        if (hotel.latitude() == null || hotel.longitude() == null)
            return new Weather(false, "Open-Meteo", "Hotel coordinates are not configured.", null, null);
        try {
            URI uri = URI.create("https://api.open-meteo.com/v1/forecast?latitude=" + hotel.latitude()
                    + "&longitude=" + hotel.longitude()
                    + "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&forecast_days=7&timezone=auto");
            var response = client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return unavailable();
            JsonNode data = mapper.readTree(response.body());
            return new Weather(true, "Open-Meteo", "Forecast for the next seven days, not necessarily your stay dates.",
                    data.get("current"), data.get("daily"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); return unavailable();
        } catch (Exception e) { return unavailable(); }
    }
    private Weather unavailable() { return new Weather(false, "Open-Meteo", "Weather is temporarily unavailable.", null, null); }
}
