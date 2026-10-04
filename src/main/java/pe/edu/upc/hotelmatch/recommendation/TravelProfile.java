package pe.edu.upc.hotelmatch.recommendation;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upc.hotelmatch.iam.User;

@Entity
@Table(name = "travel_profiles")
@Getter
@Setter
@NoArgsConstructor
public class TravelProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private int guests;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal maxBudgetPerNight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripPurpose purpose;

    @Column(length = 100)
    private String city;

    @Column(length = 500)
    private String freeText;

    @ElementCollection
    @CollectionTable(name = "travel_profile_priorities", joinColumns = @JoinColumn(name = "profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Set<Priority> priorities = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "travel_profile_amenities", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "amenity_id")
    private Set<Long> desiredAmenityIds = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
