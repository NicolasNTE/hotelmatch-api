package pe.edu.upc.hotelmatch.recommendation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.RecommendationRequest;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.RecommendationResponse;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recomendaciones")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final FreeTextInterpreter interpreter;

    public record FreeTextRequest(@NotBlank @Size(max = 500) String text) {
    }

    @PostMapping("/interpret")
    @Operation(summary = "Sugerir prioridades y motivo del viaje a partir de texto libre (el huésped los confirma)")
    public FreeTextInterpreter.Suggestion interpret(@Valid @RequestBody FreeTextRequest request) {
        return interpreter.interpret(request.text());
    }

    @PostMapping
    @Operation(summary = "Calcular el ranking de habitaciones por porcentaje de compatibilidad "
            + "a partir del perfil de viaje")
    public RecommendationResponse recommend(@Valid @RequestBody RecommendationRequest request) {
        return recommendationService.recommend(request);
    }
}
