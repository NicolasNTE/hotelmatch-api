package pe.edu.upc.hotelmatch.recommendation;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.common.ApiException;
import pe.edu.upc.hotelmatch.hotel.Amenity;
import pe.edu.upc.hotelmatch.hotel.AmenityCategory;
import pe.edu.upc.hotelmatch.hotel.AmenityRepository;
import pe.edu.upc.hotelmatch.hotel.Room;
import pe.edu.upc.hotelmatch.hotel.RoomAmenity;
import pe.edu.upc.hotelmatch.hotel.RoomRepository;
import pe.edu.upc.hotelmatch.hotel.RoomStatus;
import pe.edu.upc.hotelmatch.iam.UserRepository;
import pe.edu.upc.hotelmatch.inventory.AvailabilityService;
import pe.edu.upc.hotelmatch.inventory.PricingService;
import pe.edu.upc.hotelmatch.inventory.PricingService.Quote;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.Criterion;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.CriterionResult;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.ProfileData;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.Result;
import pe.edu.upc.hotelmatch.recommendation.CompatibilityScorer.RoomData;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.CriterionBreakdown;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.Hint;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.RecommendationRequest;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.RecommendationResponse;
import pe.edu.upc.hotelmatch.recommendation.dto.RecommendationDtos.RecommendedRoom;
import pe.edu.upc.hotelmatch.security.CurrentUser;

@Service
@Transactional
@RequiredArgsConstructor
public class RecommendationService {

    static final int MAX_RESULTS = 20;

    private final RoomRepository rooms;
    private final AmenityRepository amenities;
    private final UserRepository users;
    private final TravelProfileRepository profiles;
    private final RecommendationRepository recommendations;
    private final AvailabilityService availabilityService;
    private final PricingService pricingService;
    private final MessageSource messages;
    private final CurrentUser currentUser;
    private final Clock clock;
    private final CompatibilityScorer scorer = new CompatibilityScorer();

    private record Scored(Room room, Quote quote, Result result) {
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        if (request.checkIn().isBefore(LocalDate.now(clock))) {
            throw ApiException.unprocessable("booking.pastDate");
        }
        availabilityService.requireValidRange(request.checkIn(), request.checkOut());

        Set<Long> desiredIds = request.amenityIds() != null ? request.amenityIds() : Set.of();
        Map<Long, Amenity> desired = amenities.findAllById(desiredIds).stream()
                .collect(Collectors.toMap(Amenity::getId, Function.identity()));
        if (desired.size() != desiredIds.size()) {
            Set<Long> missing = new HashSet<>(desiredIds);
            missing.removeAll(desired.keySet());
            throw ApiException.notFound("amenity.notFound", missing.iterator().next());
        }

        Set<Long> unavailable = availabilityService.unavailableRoomIds(request.checkIn(), request.checkOut());
        List<Room> candidates = rooms.findBookableCandidates(RoomStatus.ACTIVE, request.guests()).stream()
                .filter(r -> !unavailable.contains(r.getId()))
                .filter(r -> request.city() == null || request.city().isBlank()
                        || r.getHotel().getCity().equalsIgnoreCase(request.city().trim()))
                .toList();

        ProfileData profile = new ProfileData(request.guests(), request.maxBudgetPerNight(), request.purpose(),
                request.priorities(), desiredIds);

        List<Scored> scored = new ArrayList<>();
        BigDecimal cheapest = null;
        for (Room room : candidates) {
            Quote quote = pricingService.quote(room, request.checkIn(), request.checkOut());
            if (cheapest == null || quote.averagePerNight().compareTo(cheapest) < 0) {
                cheapest = quote.averagePerNight();
            }
            if (quote.averagePerNight().compareTo(request.maxBudgetPerNight()) > 0) {
                continue;
            }
            scored.add(new Scored(room, quote, scorer.score(toRoomData(room, quote), profile)));
        }
        scored.sort(Comparator.<Scored>comparingInt(s -> -s.result().percentage())
                .thenComparing(s -> s.quote().averagePerNight())
                .thenComparing(s -> s.room().getId()));
        List<Scored> top = scored.stream().limit(MAX_RESULTS).toList();

        Hint hint = candidates.isEmpty() ? Hint.NO_AVAILABILITY : scored.isEmpty() ? Hint.NONE_WITHIN_BUDGET : null;
        TravelProfile saved = saveProfile(request, desiredIds);
        List<RecommendedRoom> results = buildResults(top, desired, saved);

        int nights = (int) ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        return new RecommendationResponse(saved.getId(), nights, hint,
                hint == Hint.NONE_WITHIN_BUDGET ? cheapest : null, results);
    }

    private TravelProfile saveProfile(RecommendationRequest request, Set<Long> desiredIds) {
        TravelProfile profile = new TravelProfile();
        profile.setUser(users.getReferenceById(currentUser.id()));
        profile.setCheckIn(request.checkIn());
        profile.setCheckOut(request.checkOut());
        profile.setGuests(request.guests());
        profile.setMaxBudgetPerNight(request.maxBudgetPerNight());
        profile.setPurpose(request.purpose());
        profile.setCity(request.city());
        profile.setFreeText(request.freeText());
        profile.setPriorities(new LinkedHashSet<>(request.priorities()));
        profile.setDesiredAmenityIds(new LinkedHashSet<>(desiredIds));
        return profiles.save(profile);
    }

    private List<RecommendedRoom> buildResults(List<Scored> top, Map<Long, Amenity> desired, TravelProfile profile) {
        Locale locale = LocaleContextHolder.getLocale();
        List<RecommendedRoom> results = new ArrayList<>();
        List<Recommendation> rows = new ArrayList<>();
        int position = 1;
        for (Scored s : top) {
            Room room = s.room();
            Set<Long> owned = room.getAmenities().stream().map(ra -> ra.getAmenity().getId())
                    .collect(Collectors.toSet());
            List<String> matched = desired.values().stream().filter(a -> owned.contains(a.getId()))
                    .map(Amenity::getName).sorted().toList();
            List<String> missing = desired.values().stream().filter(a -> !owned.contains(a.getId()))
                    .map(Amenity::getName).sorted().toList();
            List<CriterionBreakdown> breakdown = s.result().criteria().stream()
                    .sorted(Comparator.comparingDouble(CriterionResult::weight).reversed())
                    .map(c -> toBreakdown(c, matched.size(), desired.size(), locale))
                    .toList();
            String explanation = explain(s.result().percentage(), breakdown, locale);

            Recommendation row = new Recommendation();
            row.setProfile(profile);
            row.setRoom(room);
            row.setCompatibility(s.result().percentage());
            row.setPosition(position);
            row.setExplanation(explanation);
            rows.add(row);

            results.add(new RecommendedRoom(position, s.result().percentage(), room.getId(), room.getName(),
                    room.getNumber(), room.getType(), room.getBedType(), room.getCapacity(), room.getImageUrl(),
                    room.getHotel().getId(), room.getHotel().getTradeName(), room.getHotel().getCity(),
                    room.getHotel().isSustainabilitySeal(), s.quote().averagePerNight(), s.quote().total(),
                    room.getAmenities().stream().map(ra -> ra.getAmenity().getName()).sorted().toList(),
                    matched, missing, breakdown, explanation));
            position++;
        }
        recommendations.saveAll(rows);
        return results;
    }

    private CriterionBreakdown toBreakdown(CriterionResult c, int matched, int desired, Locale locale) {
        String label = messages.getMessage("criterion." + c.criterion().name(), null, c.criterion().name(), locale);
        if (c.criterion() == Criterion.AMENITIES) {
            label = label + " (" + matched + "/" + desired + ")";
        }
        return new CriterionBreakdown(c.criterion().name(), label, (int) Math.round(c.score() * 100), c.weight(),
                c.met());
    }

    private String explain(int percentage, List<CriterionBreakdown> breakdown, Locale locale) {
        String met = breakdown.stream().filter(CriterionBreakdown::met).limit(4)
                .map(CriterionBreakdown::label).collect(Collectors.joining(", "));
        String unmet = breakdown.stream().filter(b -> !b.met()).limit(3)
                .map(CriterionBreakdown::label).collect(Collectors.joining(", "));
        StringBuilder text = new StringBuilder(
                messages.getMessage("recommendation.explanation.head", new Object[]{percentage}, locale));
        if (!met.isEmpty()) {
            text.append(' ').append(messages.getMessage("recommendation.explanation.met", new Object[]{met}, locale));
        }
        if (!unmet.isEmpty()) {
            text.append(' ')
                    .append(messages.getMessage("recommendation.explanation.unmet", new Object[]{unmet}, locale));
        }
        return text.toString();
    }

    private RoomData toRoomData(Room room, Quote quote) {
        Map<AmenityCategory, Integer> byCategory = new EnumMap<>(AmenityCategory.class);
        Set<Long> ids = new HashSet<>();
        for (RoomAmenity ra : room.getAmenities()) {
            ids.add(ra.getAmenity().getId());
            byCategory.merge(ra.getAmenity().getCategory(), 1, Integer::sum);
        }
        return new RoomData(room.getPrivacyScore(), room.getQuietScore(), room.getViewScore(), room.getSpaceScore(),
                room.getCapacity(), quote.averagePerNight(), ids, byCategory);
    }
}
