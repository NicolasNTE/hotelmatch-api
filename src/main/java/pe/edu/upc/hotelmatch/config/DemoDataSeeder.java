package pe.edu.upc.hotelmatch.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.hotelmatch.hotel.Amenity;
import pe.edu.upc.hotelmatch.hotel.AmenityCategory;
import pe.edu.upc.hotelmatch.hotel.AmenityRepository;
import pe.edu.upc.hotelmatch.hotel.BedType;
import pe.edu.upc.hotelmatch.hotel.Hotel;
import pe.edu.upc.hotelmatch.hotel.HotelRepository;
import pe.edu.upc.hotelmatch.hotel.Room;
import pe.edu.upc.hotelmatch.hotel.RoomAmenity;
import pe.edu.upc.hotelmatch.hotel.RoomRepository;
import pe.edu.upc.hotelmatch.hotel.RoomType;
import pe.edu.upc.hotelmatch.iam.Role;
import pe.edu.upc.hotelmatch.iam.User;
import pe.edu.upc.hotelmatch.iam.UserRepository;

/** Datos de demostración para desarrollo local (SEED_DEMO_DATA=true o perfil h2). No se usa en producción. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "hotelmatch", name = "seed-demo-data", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    public static final String DEMO_PASSWORD = "HotelMatch2026!";
    public static final String ADMIN_EMAIL = "admin@hotelmatch.dev";
    public static final String GUEST_EMAIL = "guest@hotelmatch.dev";

    private final AmenityRepository amenities;
    private final HotelRepository hotels;
    private final RoomRepository rooms;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (amenities.count() > 0 || users.count() > 0) {
            return;
        }
        Map<String, Amenity> catalog = amenities.saveAll(List.of(
                new Amenity("Jacuzzi", AmenityCategory.WELLNESS, "hot_tub"),
                new Amenity("Bañera", AmenityCategory.WELLNESS, "bathtub"),
                new Amenity("Balcón", AmenityCategory.COMFORT, "balcony"),
                new Amenity("Minibar", AmenityCategory.COMFORT, "local_bar"),
                new Amenity("Aire acondicionado", AmenityCategory.COMFORT, "ac_unit"),
                new Amenity("Escritorio", AmenityCategory.WORK, "desk"),
                new Amenity("Wi-Fi rápido", AmenityCategory.WORK, "wifi"),
                new Amenity("Smart TV", AmenityCategory.ENTERTAINMENT, "tv"),
                new Amenity("Netflix", AmenityCategory.ENTERTAINMENT, "movie"),
                new Amenity("Desayuno incluido", AmenityCategory.FOOD, "free_breakfast"),
                new Amenity("Room service", AmenityCategory.FOOD, "room_service"),
                new Amenity("Estacionamiento", AmenityCategory.TRANSPORT, "local_parking")))
                .stream().collect(Collectors.toMap(Amenity::getName, Function.identity()));

        Hotel hotel = new Hotel();
        hotel.setLegalName("Mirador Hospitality S.A.C.");
        hotel.setTradeName("Hotel Boutique Mirador");
        hotel.setAddress("Av. Larco 1234");
        hotel.setCity("Lima");
        hotel.setDistrict("Miraflores");
        hotel.setLatitude(-12.1219);
        hotel.setLongitude(-77.0297);
        hotel.setSustainabilitySeal(true);
        hotel.setSustainabilityDescription("Reservas sin papel, ahorro de agua y energía en todas las habitaciones.");
        hotels.save(hotel);

        users.save(user("Administrador Demo", ADMIN_EMAIL, Role.ADMIN, hotel));
        users.save(user("Huésped Demo", GUEST_EMAIL, Role.GUEST, null));

        room(hotel, catalog, "Estándar Jardín", "101", 1, RoomType.STANDARD, 2, BedType.DOUBLE, "150.00",
                2, 4, 1, 2, "Wi-Fi rápido", "Smart TV", "Aire acondicionado");
        room(hotel, catalog, "Estándar Interior", "102", 1, RoomType.STANDARD, 2, BedType.TWIN, "130.00",
                2, 5, 0, 2, "Wi-Fi rápido", "Aire acondicionado");
        room(hotel, catalog, "Superior Ciudad", "201", 2, RoomType.SUPERIOR, 2, BedType.QUEEN, "190.00",
                3, 3, 3, 3, "Wi-Fi rápido", "Smart TV", "Aire acondicionado", "Escritorio", "Room service");
        room(hotel, catalog, "Superior Familiar", "202", 2, RoomType.SUPERIOR, 4, BedType.DOUBLE, "230.00",
                2, 3, 2, 4, "Wi-Fi rápido", "Smart TV", "Aire acondicionado", "Desayuno incluido");
        room(hotel, catalog, "Deluxe Terraza", "301", 3, RoomType.DELUXE, 2, BedType.KING, "280.00",
                4, 3, 5, 4, "Balcón", "Smart TV", "Netflix", "Minibar", "Aire acondicionado", "Wi-Fi rápido");
        room(hotel, catalog, "Deluxe Silencio", "302", 3, RoomType.DELUXE, 2, BedType.KING, "260.00",
                5, 5, 2, 4, "Bañera", "Minibar", "Aire acondicionado", "Wi-Fi rápido", "Escritorio");
        room(hotel, catalog, "Suite Jacuzzi", "401", 4, RoomType.SUITE, 2, BedType.KING, "340.00",
                5, 4, 4, 5, "Jacuzzi", "Balcón", "Smart TV", "Netflix", "Minibar", "Room service",
                "Aire acondicionado", "Wi-Fi rápido");
        room(hotel, catalog, "Suite Ejecutiva", "402", 4, RoomType.EXECUTIVE_SUITE, 3, BedType.KING, "420.00",
                4, 4, 5, 5, "Escritorio", "Wi-Fi rápido", "Desayuno incluido", "Room service", "Minibar",
                "Aire acondicionado", "Smart TV", "Estacionamiento");

        log.info("Datos de demostración cargados: hotel, 12 servicios, 8 habitaciones y 2 usuarios ({}, {})",
                ADMIN_EMAIL, GUEST_EMAIL);
    }

    private User user(String name, String email, Role role, Hotel hotel) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        user.setRole(role);
        user.setHotel(hotel);
        return user;
    }

    private void room(Hotel hotel, Map<String, Amenity> catalog, String name, String number, int floor,
            RoomType type, int capacity, BedType bed, String price, int privacy, int quiet, int view, int space,
            String... amenityNames) {
        Room room = new Room();
        room.setHotel(hotel);
        room.setName(name);
        room.setNumber(number);
        room.setFloor(floor);
        room.setType(type);
        room.setCapacity(capacity);
        room.setBedType(bed);
        room.setBasePrice(new BigDecimal(price));
        room.setPrivacyScore(privacy);
        room.setQuietScore(quiet);
        room.setViewScore(view);
        room.setSpaceScore(space);
        for (String amenityName : amenityNames) {
            room.getAmenities().add(new RoomAmenity(room, catalog.get(amenityName), null));
        }
        rooms.save(room);
    }
}
