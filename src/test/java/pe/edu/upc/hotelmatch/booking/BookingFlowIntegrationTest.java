package pe.edu.upc.hotelmatch.booking;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;

class BookingFlowIntegrationTest extends IntegrationTestSupport {

    private int newRoom(String admin, String price, int capacity) throws Exception {
        return read(postJson("/api/v1/rooms", admin, roomBody(unique("B"), price, capacity))
                .andExpect(status().isCreated()), "$.id");
    }

    private Map<String, Object> booking(int roomId, int fromDays, int toDays, int guests) {
        return Map.of("roomId", roomId, "checkIn", today().plusDays(fromDays).toString(),
                "checkOut", today().plusDays(toDays).toString(), "guests", guests);
    }

    @Test
    @DisplayName("US15: confirma la reserva con código y total según las tarifas por fecha")
    void confirmsBookingWithRateAwareTotal() throws Exception {
        String admin = adminToken();
        String guest = guestToken();
        int roomId = newRoom(admin, "100.00", 2);
        postJson("/api/v1/rooms/" + roomId + "/rates", admin, Map.of("name", "Feriado",
                "startDate", today().plusDays(40).toString(), "endDate", today().plusDays(40).toString(),
                "price", "150.00")).andExpect(status().isCreated());

        postJson("/api/v1/bookings", guest, booking(roomId, 40, 42, 2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", startsWith("HM-")))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.nights").value(2))
                .andExpect(jsonPath("$.totalAmount").value(250.00));
    }

    @Test
    @DisplayName("US15 E2: no permite reservar una habitación ocupada y valida capacidad, fechas y rol")
    void rejectsInvalidBookings() throws Exception {
        String admin = adminToken();
        String guest = guestToken();
        int roomId = newRoom(admin, "100.00", 2);

        postJson("/api/v1/bookings", guest, booking(roomId, 50, 53, 2)).andExpect(status().isCreated());
        postJson("/api/v1/bookings", guest, booking(roomId, 52, 55, 2)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("booking.roomUnavailable"));
        postJson("/api/v1/bookings", guest, booking(roomId, 53, 55, 2)).andExpect(status().isCreated());

        postJson("/api/v1/bookings", guest, booking(roomId, 60, 62, 3)).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("booking.capacityExceeded"));
        postJson("/api/v1/bookings", guest, booking(roomId, 62, 62, 2)).andExpect(status().isUnprocessableEntity());
        postJson("/api/v1/bookings", guest, booking(roomId, -1, 2, 2)).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("booking.pastDate"));
        postJson("/api/v1/bookings", admin, booking(roomId, 70, 72, 2)).andExpect(status().isForbidden());
        postJson("/api/v1/bookings", guest, booking(999999, 70, 72, 2)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Una habitación con fechas bloqueadas no se puede reservar")
    void blockedDatesPreventBooking() throws Exception {
        String admin = adminToken();
        int roomId = newRoom(admin, "100.00", 2);
        postJson("/api/v1/rooms/" + roomId + "/availability", admin, Map.of("startDate",
                today().plusDays(80).toString(), "endDate", today().plusDays(81).toString(), "status", "BLOCKED"))
                .andExpect(status().isCreated());

        postJson("/api/v1/bookings", guestToken(), booking(roomId, 81, 83, 2)).andExpect(status().isConflict());
        postJson("/api/v1/bookings", guestToken(), booking(roomId, 82, 84, 2)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("US16/US17: lista las reservas por rol, cancela dentro del plazo y libera la habitación")
    void listsAndCancels() throws Exception {
        String admin = adminToken();
        String guest = guestToken();
        int roomId = newRoom(admin, "100.00", 2);

        int bookingId = read(postJson("/api/v1/bookings", guest, booking(roomId, 90, 92, 2))
                .andExpect(status().isCreated()), "$.id");

        getJson("/api/v1/bookings?status=CONFIRMED&size=50", guest).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + bookingId + ")]", hasSize(1)));
        getJson("/api/v1/bookings?size=50", admin).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + bookingId + ")]", hasSize(1)));
        getJson("/api/v1/bookings?size=50&from=" + today().plusDays(200) + "&to=" + today().plusDays(210), admin)
                .andExpect(jsonPath("$.content[?(@.id == " + bookingId + ")]", hasSize(0)));
        getJson("/api/v1/bookings/" + bookingId, guest).andExpect(jsonPath("$.cancellable").value(true));

        deleteJson("/api/v1/bookings/" + bookingId, admin).andExpect(status().isForbidden());
        deleteJson("/api/v1/bookings/" + bookingId, guest).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        deleteJson("/api/v1/bookings/" + bookingId, guest).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("booking.alreadyCancelled"));

        postJson("/api/v1/bookings", guest, booking(roomId, 90, 92, 2)).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("US17 E2: no se puede cancelar con menos de 48 horas de anticipación")
    void cancellationWindowIsEnforced() throws Exception {
        String admin = adminToken();
        String guest = guestToken();
        int roomId = newRoom(admin, "100.00", 2);
        int bookingId = read(postJson("/api/v1/bookings", guest, booking(roomId, 1, 2, 2))
                .andExpect(status().isCreated()), "$.id");

        deleteJson("/api/v1/bookings/" + bookingId, guest).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("booking.cancelWindowExpired"));
        getJson("/api/v1/bookings/" + bookingId, guest).andExpect(jsonPath("$.cancellable").value(false));
    }

    @Test
    @DisplayName("El administrador actualiza el estado de la reserva de su hotel; el huésped no puede")
    void adminUpdatesBookingStatus() throws Exception {
        String admin = adminToken();
        String guest = guestToken();
        int roomId = newRoom(admin, "100.00", 2);
        int bookingId = read(postJson("/api/v1/bookings", guest, booking(roomId, 100, 101, 2))
                .andExpect(status().isCreated()), "$.id");

        putJson("/api/v1/bookings/" + bookingId + "/status", admin, Map.of("status", "PENDING_PAYMENT"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));
        putJson("/api/v1/bookings/" + bookingId + "/status", admin, Map.of("status", "CANCELLED"))
                .andExpect(status().isUnprocessableEntity());
        putJson("/api/v1/bookings/" + bookingId + "/status", guest, Map.of("status", "CONFIRMED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("US10-US12: el ranking sale ordenado, con desglose, explicación y sin habitaciones fuera de presupuesto")
    void ranksRoomsByCompatibility() throws Exception {
        String guest = guestToken();
        Map<String, Object> request = Map.of(
                "checkIn", today().plusDays(120).toString(), "checkOut", today().plusDays(121).toString(),
                "guests", 2, "maxBudgetPerNight", "300", "purpose", "COUPLE",
                "priorities", List.of("PRIVACY", "VIEW"), "freeText", "Aniversario tranquilo");

        String body = postJson("/api/v1/recommendations", guest, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results.length()", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.results[0].position").value(1))
                .andExpect(jsonPath("$.results[0].explanation", startsWith("Compatibilidad del")))
                .andExpect(jsonPath("$.results[0].breakdown.length()", greaterThanOrEqualTo(3)))
                .andReturn().getResponse().getContentAsString();

        List<Integer> scores = com.jayway.jsonpath.JsonPath.read(body, "$.results[*].compatibility");
        List<Number> prices = com.jayway.jsonpath.JsonPath.read(body, "$.results[*].pricePerNight");
        assertTrue(scores.stream().allMatch(s -> s >= 0 && s <= 100));
        assertEquals(new ArrayList<>(scores).stream().sorted((a, b) -> b - a).toList(), scores);
        assertTrue(prices.stream().allMatch(p -> p.doubleValue() <= 300.0));
    }

    @Test
    @DisplayName("US11 E2: sin habitaciones dentro del presupuesto sugiere ampliarlo; las reservadas no aparecen")
    void hintsWhenNothingFits() throws Exception {
        String admin = adminToken();
        String guest = guestToken();
        postJson("/api/v1/recommendations", guest, Map.of(
                "checkIn", today().plusDays(130).toString(), "checkOut", today().plusDays(131).toString(),
                "guests", 2, "maxBudgetPerNight", "10", "purpose", "REST", "priorities", List.of("QUIET")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results", hasSize(0)))
                .andExpect(jsonPath("$.hint").value("NONE_WITHIN_BUDGET"))
                .andExpect(jsonPath("$.cheapestAvailablePrice").isNumber());

        int roomId = newRoom(admin, "55.00", 9);
        postJson("/api/v1/bookings", guest, booking(roomId, 140, 141, 9)).andExpect(status().isCreated());
        postJson("/api/v1/recommendations", guest, Map.of(
                "checkIn", today().plusDays(140).toString(), "checkOut", today().plusDays(141).toString(),
                "guests", 9, "maxBudgetPerNight", "500", "purpose", "EVENT", "priorities", List.of("SPACE")))
                .andExpect(jsonPath("$.hint").value("NO_AVAILABILITY"));

        postJson("/api/v1/recommendations", guest, Map.of(
                "checkIn", today().plusDays(140).toString(), "checkOut", today().plusDays(141).toString(),
                "guests", 2, "maxBudgetPerNight", "500", "purpose", "EVENT",
                "priorities", List.of("SPACE", "VIEW", "QUIET", "PRIVACY"))).andExpect(status().isBadRequest());
    }
}
