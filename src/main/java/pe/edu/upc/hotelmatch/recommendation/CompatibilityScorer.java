package pe.edu.upc.hotelmatch.recommendation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pe.edu.upc.hotelmatch.hotel.AmenityCategory;

/**
 * Cálculo determinista y auditable del porcentaje de compatibilidad entre un perfil de viaje y una habitación.
 * Cada criterio puntúa entre 0 y 1 y se pondera según el motivo del viaje y las prioridades elegidas.
 */
public final class CompatibilityScorer {

    public enum Criterion {
        PRIVACY, QUIET, VIEW, SPACE, PRICE, COMFORT, ENTERTAINMENT, WORK, LUXURY, AMENITIES, CAPACITY
    }

    public record RoomData(int privacy, int quiet, int view, int space, int capacity, BigDecimal pricePerNight,
                           Set<Long> amenityIds, Map<AmenityCategory, Integer> amenitiesByCategory) {
    }

    public record ProfileData(int guests, BigDecimal budgetPerNight, TripPurpose purpose, Set<Priority> priorities,
                              Set<Long> desiredAmenityIds) {
    }

    public record CriterionResult(Criterion criterion, double score, double weight, boolean met) {
    }

    public record Result(int percentage, List<CriterionResult> criteria) {
    }

    static final double PRIORITY_WEIGHT = 3.0;
    static final double AMENITIES_WEIGHT = 4.0;
    static final double MET_THRESHOLD = 0.6;

    public Result score(RoomData room, ProfileData profile) {
        Map<Criterion, Double> weights = weightsFor(profile);
        List<CriterionResult> results = new ArrayList<>();
        double weighted = 0;
        double totalWeight = 0;
        for (Map.Entry<Criterion, Double> entry : weights.entrySet()) {
            double score = clamp(scoreOf(entry.getKey(), room, profile));
            results.add(new CriterionResult(entry.getKey(), score, entry.getValue(), score >= MET_THRESHOLD));
            weighted += score * entry.getValue();
            totalWeight += entry.getValue();
        }
        int percentage = (int) Math.round(100 * weighted / totalWeight);
        return new Result(Math.max(0, Math.min(100, percentage)), results);
    }

    private Map<Criterion, Double> weightsFor(ProfileData profile) {
        Map<Criterion, Double> weights = new EnumMap<>(Criterion.class);
        switch (profile.purpose()) {
            case BUSINESS -> {
                add(weights, Criterion.QUIET, 2);
                add(weights, Criterion.WORK, 3);
                add(weights, Criterion.PRIVACY, 1);
            }
            case TOURISM -> {
                add(weights, Criterion.VIEW, 2);
                add(weights, Criterion.SPACE, 1);
                add(weights, Criterion.ENTERTAINMENT, 1);
            }
            case COUPLE -> {
                add(weights, Criterion.PRIVACY, 3);
                add(weights, Criterion.VIEW, 2);
                add(weights, Criterion.QUIET, 1);
            }
            case FAMILY -> {
                add(weights, Criterion.SPACE, 3);
                add(weights, Criterion.QUIET, 1);
                add(weights, Criterion.ENTERTAINMENT, 1);
            }
            case EVENT -> {
                add(weights, Criterion.SPACE, 2);
                add(weights, Criterion.ENTERTAINMENT, 2);
                add(weights, Criterion.VIEW, 1);
            }
            case REST -> {
                add(weights, Criterion.QUIET, 3);
                add(weights, Criterion.PRIVACY, 2);
                add(weights, Criterion.VIEW, 1);
            }
            case OTHER -> {
            }
        }
        for (Priority priority : profile.priorities()) {
            add(weights, Criterion.valueOf(priority.name()), PRIORITY_WEIGHT);
        }
        add(weights, Criterion.PRICE, 1);
        add(weights, Criterion.CAPACITY, 1);
        if (!profile.desiredAmenityIds().isEmpty()) {
            add(weights, Criterion.AMENITIES, AMENITIES_WEIGHT);
        }
        return weights;
    }

    private double scoreOf(Criterion criterion, RoomData room, ProfileData profile) {
        return switch (criterion) {
            case PRIVACY -> room.privacy() / 5.0;
            case QUIET -> room.quiet() / 5.0;
            case VIEW -> room.view() / 5.0;
            case SPACE -> room.space() / 5.0;
            case PRICE -> 1 - room.pricePerNight()
                    .divide(profile.budgetPerNight(), 4, RoundingMode.HALF_UP).doubleValue();
            case COMFORT -> 0.5 * (room.space() + room.quiet()) / 10.0
                    + 0.5 * categoryScore(room, AmenityCategory.COMFORT);
            case ENTERTAINMENT -> categoryScore(room, AmenityCategory.ENTERTAINMENT);
            case WORK -> categoryScore(room, AmenityCategory.WORK);
            case LUXURY -> 0.5 * categoryScore(room, AmenityCategory.WELLNESS) + 0.5 * room.view() / 5.0;
            case AMENITIES -> {
                long matched = profile.desiredAmenityIds().stream().filter(room.amenityIds()::contains).count();
                yield (double) matched / profile.desiredAmenityIds().size();
            }
            case CAPACITY -> Math.min(1.0, (double) profile.guests() / room.capacity());
        };
    }

    private double categoryScore(RoomData room, AmenityCategory category) {
        return Math.min(1.0, room.amenitiesByCategory().getOrDefault(category, 0) / 2.0);
    }

    private void add(Map<Criterion, Double> weights, Criterion criterion, double weight) {
        weights.merge(criterion, weight, Double::sum);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
