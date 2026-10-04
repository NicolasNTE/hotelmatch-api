package pe.edu.upc.hotelmatch.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hotelmatch")
public record HotelMatchProperties(boolean seedDemoData, int cancellationWindowHours, Security security) {

    public record Security(String jwtSecret, int jwtExpirationMinutes, List<String> allowedOrigins) {
    }
}
