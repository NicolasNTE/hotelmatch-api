package pe.edu.upc.hotelmatch.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

/** Respaldo por palabras clave y validación de la salida del modelo; no necesita Ollama. */
class FreeTextInterpreterTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void keywordsInSpanishSuggestPrioritiesAndPurpose() {
        var suggestion = FreeTextInterpreter.fromKeywords(
                "Es nuestro aniversario, quiero algo tranquilo, íntimo y con vista al mar");
        assertThat(suggestion.priorities()).containsExactly(Priority.QUIET, Priority.PRIVACY, Priority.VIEW);
        assertThat(suggestion.purpose()).isEqualTo(TripPurpose.COUPLE);
        assertThat(suggestion.mode()).isEqualTo(FreeTextInterpreter.Mode.KEYWORDS);
    }

    @Test
    void unrelatedTextSuggestsNothing() {
        var suggestion = FreeTextInterpreter.fromKeywords("Llego el martes en marzo");
        assertThat(suggestion.priorities()).isEmpty();
        assertThat(suggestion.mode()).isEqualTo(FreeTextInterpreter.Mode.NONE);
    }

    @Test
    void modelOutputOutsideTheEnumsIsDiscarded() throws Exception {
        var suggestion = FreeTextInterpreter.parse(mapper.readTree(
                "{\"priorities\":[\"VIEW\",\"VIEW\",\"POOL\",\"QUIET\",\"WORK\",\"PRICE\"],\"purpose\":\"HACK\"}"))
                .orElseThrow();
        assertThat(suggestion.priorities()).containsExactly(Priority.VIEW, Priority.QUIET, Priority.WORK);
        assertThat(suggestion.purpose()).isNull();
        assertThat(FreeTextInterpreter.parse(mapper.readTree("{\"priorities\":[\"POOL\"]}"))).isEmpty();
    }
}
