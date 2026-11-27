package pe.edu.upc.hotelmatch.inventory;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pe.edu.upc.hotelmatch.IntegrationTestSupport;

/** Disponibilidad y tarifas por fecha: bloqueos, solapes y permisos por rol. */
class AvailabilityRateIntegrationTest extends IntegrationTestSupport {

    private long newRoom(String adminToken) throws Exception {
        Number id = read(postJson("/api/v1/rooms", adminToken, roomBody(unique("T"), "200.00", 2))
                .andExpect(status().isCreated()), "$.id");
        return id.longValue();
    }

    private Map<String, Object> range(LocalDate from, LocalDate to, String status) {
        return Map.of("startDate", from.toString(), "endDate", to.toString(), "status", status);
    }

    private Map<String, Object> rate(LocalDate from, LocalDate to, String price) {
        return Map.of("name", "Temporada de prueba", "startDate", from.toString(), "endDate", to.toString(),
                "price", price);
    }

    @Test
    void blockedDatesPreventBookingAndCannotBeRegisteredTwice() throws Exception {
        String admin = adminToken();
        long room = newRoom(admin);
        LocalDate from = today().plusDays(200);
        String url = "/api/v1/rooms/" + room + "/availability";

        postJson(url, admin, range(from, from.plusDays(2), "BLOCKED")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(3));
        postJson(url, admin, range(from, from, "BLOCKED")).andExpect(status().isConflict());
        postJson("/api/v1/bookings", guestToken(), Map.of("roomId", room, "checkIn", from.toString(),
                "checkOut", from.plusDays(2).toString(), "guests", 2)).andExpect(status().isConflict());
        deleteJson(url + "/" + from, admin).andExpect(status().isNoContent());
    }

    @Test
    void bookedStatusAndReversedRangeAreRejected() throws Exception {
        String admin = adminToken();
        String url = "/api/v1/rooms/" + newRoom(admin) + "/availability";
        LocalDate from = today().plusDays(210);

        postJson(url, admin, range(from, from, "BOOKED")).andExpect(status().isUnprocessableEntity());
        postJson(url, admin, range(from.plusDays(3), from, "BLOCKED")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void ratesCannotOverlapAndGuestCannotManageThem() throws Exception {
        String admin = adminToken();
        String url = "/api/v1/rooms/" + newRoom(admin) + "/rates";
        LocalDate from = today().plusDays(220);

        postJson(url, admin, rate(from, from.plusDays(5), "260.00")).andExpect(status().isCreated());
        postJson(url, admin, rate(from.plusDays(4), from.plusDays(8), "270.00")).andExpect(status().isConflict());
        postJson(url, guestToken(), rate(from.plusDays(20), from.plusDays(21), "10.00"))
                .andExpect(status().isForbidden());
    }
}
