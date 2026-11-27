package pe.edu.upc.hotelmatch.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.hotel.AmenityCategory;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.Criterion;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.ProfileData;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.Result;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.RoomData;

class CompatibilityScorerTest {

    private final CompatibilityScorer scorer = new CompatibilityScorer();

    private RoomData room(int privacy, int quiet, int view, int space, int capacity, String price,
            Set<Long> amenityIds, Map<AmenityCategory, Integer> byCategory) {
        return new RoomData(privacy, quiet, view, space, capacity, new BigDecimal(price), amenityIds, byCategory);
    }

    private ProfileData profile(int guests, String budget, TripPurpose purpose, Set<Priority> priorities,
            Set<Long> desired) {
        return new ProfileData(guests, new BigDecimal(budget), purpose, priorities, desired);
    }

    @Test
    @DisplayName("La habitación que cumple lo que el huésped prioriza obtiene mayor porcentaje")
    void roomMatchingPriorityScoresHigher() {
        ProfileData couple = profile(2, "300", TripPurpose.COUPLE, Set.of(Priority.PRIVACY), Set.of());
        RoomData private_ = room(5, 3, 4, 3, 2, "200", Set.of(), Map.of());
        RoomData exposed = room(1, 3, 4, 3, 2, "200", Set.of(), Map.of());

        assertThat(scorer.score(private_, couple).percentage())
                .isGreaterThan(scorer.score(exposed, couple).percentage());
    }

    @Test
    @DisplayName("El porcentaje siempre queda entre 0 y 100")
    void percentageIsBounded() {
        ProfileData profile = profile(2, "100", TripPurpose.REST, Set.of(Priority.QUIET, Priority.VIEW), Set.of());
        Result best = scorer.score(room(5, 5, 5, 5, 2, "1", Set.of(), Map.of()), profile);
        Result worst = scorer.score(room(0, 0, 0, 0, 10, "100", Set.of(), Map.of()), profile);

        assertThat(best.percentage()).isBetween(0, 100);
        assertThat(worst.percentage()).isBetween(0, 100);
        assertThat(best.percentage()).isGreaterThan(worst.percentage());
    }

    @Test
    @DisplayName("Los servicios solicitados puntúan por la proporción que la habitación ofrece")
    void amenitiesScoreIsProportional() {
        ProfileData profile = profile(2, "300", TripPurpose.OTHER, Set.of(Priority.COMFORT), Set.of(1L, 2L, 3L, 4L));
        RoomData twoOfFour = room(3, 3, 3, 3, 2, "150", Set.of(1L, 2L), Map.of());

        double amenitiesScore = scorer.score(twoOfFour, profile).criteria().stream()
                .filter(c -> c.criterion() == Criterion.AMENITIES).findFirst().orElseThrow().score();

        assertThat(amenitiesScore).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Sin servicios solicitados el criterio de servicios no participa del cálculo")
    void amenitiesCriterionOmittedWhenNoneRequested() {
        ProfileData profile = profile(2, "300", TripPurpose.TOURISM, Set.of(Priority.VIEW), Set.of());

        Result result = scorer.score(room(3, 3, 3, 3, 2, "150", Set.of(), Map.of()), profile);

        assertThat(result.criteria()).noneMatch(c -> c.criterion() == Criterion.AMENITIES);
    }

    @Test
    @DisplayName("Cada prioridad elegida suma peso a su criterio y el motivo del viaje define pesos base")
    void priorityAndPurposeAddWeight() {
        ProfileData profile = profile(2, "300", TripPurpose.COUPLE, Set.of(Priority.PRIVACY), Set.of());

        double privacyWeight = scorer.score(room(3, 3, 3, 3, 2, "150", Set.of(), Map.of()), profile).criteria()
                .stream().filter(c -> c.criterion() == Criterion.PRIVACY).findFirst().orElseThrow().weight();

        assertThat(privacyWeight).isEqualTo(3.0 + CompatibilityScorer.PRIORITY_WEIGHT);
    }

    @Test
    @DisplayName("Una habitación mucho más grande que el grupo reduce el ajuste de capacidad")
    void oversizedRoomLowersCapacityFit() {
        ProfileData profile = profile(2, "300", TripPurpose.OTHER, Set.of(Priority.SPACE), Set.of());

        double fit = scorer.score(room(3, 3, 3, 3, 4, "150", Set.of(), Map.of()), profile).criteria().stream()
                .filter(c -> c.criterion() == Criterion.CAPACITY).findFirst().orElseThrow().score();

        assertThat(fit).isEqualTo(0.5);
    }
}
