package pe.edu.upc.hotelmatch.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI hotelMatchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HotelMatch API")
                        .version("v1")
                        .description("RESTful API de HotelMatch: gestión de hoteles, habitaciones, reservas y "
                                + "ranking de habitaciones por porcentaje de compatibilidad con el perfil de viaje."))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
